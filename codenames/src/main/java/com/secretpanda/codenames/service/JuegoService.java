package com.secretpanda.codenames.service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.secretpanda.codenames.dto.juego.*;
import com.secretpanda.codenames.exception.*;
import com.secretpanda.codenames.mapper.juego.*;
import com.secretpanda.codenames.model.*;
import com.secretpanda.codenames.model.TableroCarta.*;
import com.secretpanda.codenames.model.JugadorPartida.*;
import com.secretpanda.codenames.repository.*;

@Service
public class JuegoService {

    // Distribución estándar de Codenames en tablero 4×5 (20 cartas)
    // El equipo que empieza tiene 9 cartas, el otro 8, 1 asesino, 2 civiles
    private static final int CARTAS_ROJO_EMPIEZA   = 9;
    private static final int CARTAS_AZUL_EMPIEZA   = 8;
    private static final int CARTAS_ASESINO        = 1;
    private static final int CARTAS_CIVIL          = 20 - CARTAS_ROJO_EMPIEZA - CARTAS_AZUL_EMPIEZA - CARTAS_ASESINO;
    private static final int TOTAL_CARTAS          = 20; // 4 filas × 5 columnas

    private final PartidaRepository          partidaRepository;
    private final JugadorPartidaRepository   jugadorPartidaRepository;
    private final TableroCartaRepository     tableroCartaRepository;
    private final TurnoRepository            turnoRepository;
    private final VotoCartaRepository        votoCartaRepository;
    private final PalabraTemaRepository      palabraTemaRepository;
    private final JugadorRepository          jugadorRepository;
    private final SimpMessagingTemplate      messagingTemplate;
    private final TemporizadorService        temporizadorService;

    public JuegoService(PartidaRepository partidaRepository,
                        JugadorPartidaRepository jugadorPartidaRepository,
                        TableroCartaRepository tableroCartaRepository,
                        TurnoRepository turnoRepository,
                        VotoCartaRepository votoCartaRepository,
                        PalabraTemaRepository palabraTemaRepository,
                        JugadorRepository jugadorRepository,
                        SimpMessagingTemplate messagingTemplate,
                        TemporizadorService temporizadorService) {
        this.partidaRepository        = partidaRepository;
        this.jugadorPartidaRepository = jugadorPartidaRepository;
        this.tableroCartaRepository   = tableroCartaRepository;
        this.turnoRepository          = turnoRepository;
        this.votoCartaRepository      = votoCartaRepository;
        this.palabraTemaRepository    = palabraTemaRepository;
        this.jugadorRepository        = jugadorRepository;
        this.messagingTemplate        = messagingTemplate;
        this.temporizadorService      = temporizadorService;
    }

    // ─── Inicializar partida ──────────────────────────────────────────────────
    // Llamado desde LobbyService al iniciar la partida.

    @Transactional
    public void inicializarPartida(Partida partida, List<JugadorPartida> jugadores) {
        // Determinar qué equipo empieza (el que tiene más cartas)
        // Alternar aleatoriamente quién empieza (rojo o azul)
        boolean rojoEmpieza = new Random().nextBoolean();

        int cartasEquipoA = rojoEmpieza ? CARTAS_ROJO_EMPIEZA : CARTAS_AZUL_EMPIEZA;
        int cartasEquipoB = rojoEmpieza ? CARTAS_AZUL_EMPIEZA : CARTAS_ROJO_EMPIEZA;

        // Obtener 20 palabras aleatorias del tema
        List<PalabraTema> palabras = palabraTemaRepository
                .findPalabrasAleatoriasPorTema(partida.getTema().getIdTema(), TOTAL_CARTAS);

        if (palabras.size() < TOTAL_CARTAS) {
            throw new GameLogicException(
                    "El tema no tiene suficientes palabras. Necesita al menos " + TOTAL_CARTAS + ".");
        }

        // Construir lista de tipos de carta y mezclarla
        List<TipoCarta> tipos = new ArrayList<>();
        for (int i = 0; i < cartasEquipoA;      i++) tipos.add(rojoEmpieza ? TipoCarta.rojo : TipoCarta.azul);
        for (int i = 0; i < cartasEquipoB;      i++) tipos.add(rojoEmpieza ? TipoCarta.azul : TipoCarta.rojo);
        for (int i = 0; i < CARTAS_ASESINO;     i++) tipos.add(TipoCarta.asesino);
        for (int i = 0; i < CARTAS_CIVIL;       i++) tipos.add(TipoCarta.civil);
        Collections.shuffle(tipos);

        // Crear las 20 cartas del tablero
        List<TableroCarta> cartas = new ArrayList<>();
        int idx = 0;
        for (int fila = 0; fila < 4; fila++) {
            for (int col = 0; col < 5; col++) {
                TableroCarta carta = new TableroCarta();
                carta.setPartida(partida);
                carta.setPalabra(palabras.get(idx));
                carta.setFila(fila);
                carta.setColumna(col);
                carta.setTipo(tipos.get(idx));
                carta.setEstado(EstadoCarta.oculta);
                cartas.add(carta);
                idx++;
            }
        }
        tableroCartaRepository.saveAll(cartas);

        // Emitir estado inicial del juego a todos los jugadores
        broadcastEstado(partida.getIdPartida());
    }

    // ─── Dar pista (Jefe) ─────────────────────────────────────────────────────

    @Transactional
    public void darPista(Integer idPartida, String palabraPista,
                          int pistaNumero, String idGoogle) {
        Partida partida = requireEnCurso(idPartida);
        JugadorPartida jp = requireJugadorEnPartida(idGoogle, idPartida);

        // Solo el líder puede dar pista
        if (!Rol.lider.equals(jp.getRol())) {
            throw new GameLogicException("Solo el jefe de espías puede dar una pista.");
        }

        // Validar que es el turno de su equipo
        validarTurnoEquipo(partida, jp.getEquipo());

        // Validar la pista (RF-15: 1 palabra, máx 20 chars, sin espacios, pistaNumero 1-8)
        String pistaLimpia = palabraPista.trim();
        if (pistaLimpia.isBlank() || pistaLimpia.contains(" ") || pistaLimpia.length() > 20) {
            throw new BadRequestException("La pista debe ser una sola palabra de máximo 20 caracteres.");
        }
        if (pistaNumero < 1 || pistaNumero > 8) {
            throw new BadRequestException("El número de la pista debe estar entre 1 y 8.");
        }

        // Calcular número de turno
        int numTurno = turnoRepository
                .findFirstByPartida_IdPartidaOrderByNumTurnoDesc(idPartida)
                .map(t -> t.getNumTurno() + 1)
                .orElse(1);

        Turno turno = new Turno();
        turno.setPartida(partida);
        turno.setJugadorPartida(jp);
        turno.setNumTurno(numTurno);
        turno.setPalabraPista(pistaLimpia);
        turno.setPistaNumero(pistaNumero);
        turnoRepository.save(turno);

        // Iniciar temporizador de votación
        temporizadorService.iniciarTemporizador(idPartida, partida.getTiempoEspera(),
                () -> forzarFinTurno(idPartida));

        // Broadcast de la pista a los agentes
        PistaDTO pistaDTO = new PistaDTO(turno);
        messagingTemplate.convertAndSend("/topic/partidas/" + idPartida + "/pista", pistaDTO);

        // Broadcast del estado general
        broadcastEstado(idPartida);
    }

    // ─── Votar carta (Agente) ─────────────────────────────────────────────────

    @Transactional
    public VotoRecibidoDTO votar(Integer idPartida, Integer idCartaTablero,
                                  Integer idTurno, String idGoogle) {
        Partida partida = requireEnCurso(idPartida);
        JugadorPartida jp = requireJugadorEnPartida(idGoogle, idPartida);

        if (!Rol.agente.equals(jp.getRol())) {
            throw new GameLogicException("Solo los agentes pueden votar.");
        }

        Turno turno = turnoRepository.findById(idTurno)
                .orElseThrow(() -> new NotFoundException("Turno no encontrado."));

        if (!turno.getPartida().getIdPartida().equals(idPartida)) {
            throw new BadRequestException("El turno no pertenece a esta partida.");
        }

        // Validar que es el turno del equipo del agente
        if (!turno.getJugadorPartida().getEquipo().equals(jp.getEquipo())) {
            throw new GameLogicException("No es el turno de tu equipo.");
        }

        TableroCarta carta = tableroCartaRepository.findById(idCartaTablero)
                .orElseThrow(() -> new NotFoundException("Carta no encontrada."));

        if (!EstadoCarta.oculta.equals(carta.getEstado())) {
            throw new GameLogicException("Esa carta ya ha sido revelada.");
        }

        // El jugador puede cambiar su voto: borrar el anterior si existe
        votoCartaRepository.findByTurno_IdTurnoAndJugadorPartida_IdJugadorPartida(
                        idTurno, jp.getIdJugadorPartida())
                .ifPresent(votoCartaRepository::delete);

        // Insertar nuevo voto
        VotoCarta voto = new VotoCarta();
        voto.setTurno(turno);
        voto.setJugadorPartida(jp);
        voto.setCartaTablero(carta);
        votoCartaRepository.save(voto);

        // Calcular votos actuales por carta
        List<VotoCarta> todosVotos = votoCartaRepository.findByTurno_IdTurno(idTurno);

        // Construir DTO de respuesta con votos actuales
        VotoRecibidoDTO respuesta = buildVotoRecibidoDTO(idTurno, todosVotos);

        // Broadcast de votos en tiempo real
        messagingTemplate.convertAndSend("/topic/partidas/" + idPartida + "/estado",
                buildGameState(partida, idGoogle));

        // Comprobar si todos los agentes activos del equipo ya votaron
        List<JugadorPartida> agentesActivos = jugadorPartidaRepository
                .findByPartida_IdPartidaAndAbandonoFalse(idPartida).stream()
                .filter(a -> Rol.agente.equals(a.getRol())
                          && a.getEquipo().equals(jp.getEquipo()))
                .collect(Collectors.toList());

        boolean todosVotaron = agentesActivos.stream()
                .allMatch(a -> todosVotos.stream()
                        .anyMatch(v -> v.getJugadorPartida().getIdJugadorPartida()
                                .equals(a.getIdJugadorPartida())));

        if (todosVotaron) {
            resolverVotacion(partida, turno, jp.getEquipo());
        }

        return respuesta;
    }

    // ─── Resolver votación ────────────────────────────────────────────────────

    @Transactional
    public void resolverVotacion(Partida partida, Turno turno, Equipo equipoVotante) {
        List<VotoCarta> votos = votoCartaRepository.findByTurno_IdTurno(turno.getIdTurno());

        // Mayoría simple: carta con más votos
        TableroCarta cartaGanadora = votos.stream()
                .collect(Collectors.groupingBy(v -> v.getCartaTablero().getIdCartaTablero(),
                        Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(e -> tableroCartaRepository.findById(e.getKey()).orElseThrow())
                .orElseThrow(() -> new GameLogicException("No hay votos para resolver."));

        // Revelar carta
        cartaGanadora.setEstado(EstadoCarta.revelada);
        tableroCartaRepository.save(cartaGanadora);

        // Actualizar estadísticas de los agentes
        actualizarEstadisticasAgentes(votos, equipoVotante, cartaGanadora);

        // Cancelar temporizador activo
        temporizadorService.cancelarTemporizador(partida.getIdPartida());

        // Comprobar condición de victoria/derrota
        boolean continua = comprobarCondicionFin(partida, cartaGanadora, equipoVotante, turno);

        if (continua) {
            // Comprobar si puede seguir votando (más cartas del número de pista)
            long cartasReveladasEsteTurno = votos.stream()
                    .map(v -> v.getCartaTablero().getIdCartaTablero())
                    .distinct().count(); // simplificación: 1 carta por resolución

            boolean cartaCorrecta = esCartaDelEquipo(cartaGanadora.getTipo(), equipoVotante);
            boolean puedeVotarMas = cartaCorrecta &&
                    cartasReveladasEsteTurno < turno.getPistaNumero();

            if (!cartaCorrecta || !puedeVotarMas) {
                // Pasar turno al equipo contrario
                broadcastEstado(partida.getIdPartida());
            } else {
                // Puede votar una carta más; el temporizador se reinicia
                temporizadorService.iniciarTemporizador(partida.getIdPartida(),
                        partida.getTiempoEspera(), () -> forzarFinTurno(partida.getIdPartida()));
                broadcastEstado(partida.getIdPartida());
            }
        }
    }

    // ─── GameState broadcast ──────────────────────────────────────────────────

    public void broadcastEstado(Integer idPartida) {
        Partida partida = partidaRepository.findById(idPartida).orElseThrow();
        List<TableroCarta> cartas = tableroCartaRepository.findByPartida_IdPartida(idPartida);
        Turno turnoActual = turnoRepository
                .findFirstByPartida_IdPartidaOrderByNumTurnoDesc(idPartida).orElse(null);
        List<VotoCarta> votos = turnoActual != null
                ? votoCartaRepository.findByTurno_IdTurno(turnoActual.getIdTurno())
                : List.of();

        // Enviar versión para LÍDERES (con tipos de carta visibles)
        GameStateDTO estadoLider = GameStateMapper.toDTO(partida, cartas, turnoActual, votos, true);
        // Enviar versión para AGENTES (sin tipos de cartas ocultas)
        GameStateDTO estadoAgente = GameStateMapper.toDTO(partida, cartas, turnoActual, votos, false);

        // Separar por rol
        List<JugadorPartida> jugadores = jugadorPartidaRepository.findByPartida_IdPartida(idPartida);
        for (JugadorPartida jp : jugadores) {
            boolean esLider = Rol.lider.equals(jp.getRol());
            GameStateDTO estado = esLider ? estadoLider : estadoAgente;
            // Envío personalizado por usuario
            messagingTemplate.convertAndSendToUser(
                    jp.getJugador().getIdGoogle(),
                    "/queue/partidas/" + idPartida + "/estado",
                    estado);
        }

        // También broadcast general (los clientes usarán el que les corresponda)
        messagingTemplate.convertAndSend("/topic/partidas/" + idPartida + "/estado", estadoAgente);
    }

    // ─── Forzar fin de turno (timeout) ────────────────────────────────────────

    @Transactional
    public void forzarFinTurno(Integer idPartida) {
        Partida partida = requireEnCurso(idPartida);
        broadcastEstado(idPartida);
        // El turno simplemente expira; el siguiente broadcastEstado ya refleja el nuevo turno
        // El frontend detecta que cambia el equipo activo y se adapta
    }

    // ─── GameState para un jugador concreto ───────────────────────────────────

    @Transactional(readOnly = true)
    public GameStateDTO getGameState(Integer idPartida, String idGoogle) {
        Partida partida = requireEnCurso(idPartida);
        JugadorPartida jp = requireJugadorEnPartida(idGoogle, idPartida);

        List<TableroCarta> cartas = tableroCartaRepository.findByPartida_IdPartida(idPartida);
        Turno turnoActual = turnoRepository
                .findFirstByPartida_IdPartidaOrderByNumTurnoDesc(idPartida).orElse(null);
        List<VotoCarta> votos = turnoActual != null
                ? votoCartaRepository.findByTurno_IdTurno(turnoActual.getIdTurno())
                : List.of();

        boolean esLider = Rol.lider.equals(jp.getRol());
        return GameStateMapper.toDTO(partida, cartas, turnoActual, votos, esLider);
    }

    // ─── Helpers privados ─────────────────────────────────────────────────────

    private Partida requireEnCurso(Integer idPartida) {
        Partida p = partidaRepository.findById(idPartida)
                .orElseThrow(() -> new NotFoundException("Partida no encontrada."));
        if (!Partida.EstadoPartida.en_curso.equals(p.getEstado())) {
            throw new GameLogicException("La partida no está en curso.");
        }
        return p;
    }

    private JugadorPartida requireJugadorEnPartida(String idGoogle, Integer idPartida) {
        return jugadorPartidaRepository
                .findByJugador_IdGoogleAndPartida_IdPartida(idGoogle, idPartida)
                .orElseThrow(() -> new BadRequestException("No perteneces a esta partida."));
    }

    private void validarTurnoEquipo(Partida partida, Equipo equipoJugador) {
        Optional<Turno> ultimoTurno = turnoRepository
                .findFirstByPartida_IdPartidaOrderByNumTurnoDesc(partida.getIdPartida());

        if (ultimoTurno.isEmpty()) {
            // Primera pista: el equipo que tiene más cartas empieza
            long cartasRojo = tableroCartaRepository.countByPartida_IdPartidaAndTipoAndEstado(
                    partida.getIdPartida(), TipoCarta.rojo, EstadoCarta.oculta);
            long cartasAzul = tableroCartaRepository.countByPartida_IdPartidaAndTipoAndEstado(
                    partida.getIdPartida(), TipoCarta.azul, EstadoCarta.oculta);
            Equipo equipoInicial = (cartasRojo >= cartasAzul) ? Equipo.rojo : Equipo.azul;
            if (!equipoJugador.equals(equipoInicial)) {
                throw new GameLogicException("No es el turno de tu equipo.");
            }
            return;
        }

        // El turno actual pertenece al equipo del último turno. ¿Ha pasado ya la votación?
        Turno ultimo = ultimoTurno.get();
        Equipo equipoUltimoTurno = ultimo.getJugadorPartida().getEquipo();

        // Si el equipo actual es el mismo que el del último turno, le toca al contrario
        // (esto se determina comprobando si hay votos que ya resolvieron la carta)
        // Lógica simplificada: si el último turno ya tiene una carta revelada más que antes, pasó.
        // En producción esto se controla con un campo de estado en el turno.
        // Por ahora, comprobamos que el equipo que pide dar pista es distinto al del último turno.
        if (equipoJugador.equals(equipoUltimoTurno)) {
            // Puede que aún tenga derecho a votar más cartas (si acertó y le quedan intentos)
            // Esta lógica se maneja en resolverVotacion; aquí dejamos pasar.
        }
    }

    private boolean comprobarCondicionFin(Partida partida, TableroCarta carta,
                                           Equipo equipoVotante, Turno turno) {
        Integer idPartida = partida.getIdPartida();

        // Condición de derrota: carta asesino
        if (TipoCarta.asesino.equals(carta.getTipo())) {
            boolean rojoGana = Equipo.rojo.equals(
                    Equipo.rojo.equals(equipoVotante) ? Equipo.azul : Equipo.rojo);
            finalizarPartida(partida, !Equipo.rojo.equals(equipoVotante));
            return false;
        }

        // Condición de victoria: todas las cartas del equipo reveladas
        long rojasOcultas = tableroCartaRepository.countByPartida_IdPartidaAndTipoAndEstado(
                idPartida, TipoCarta.rojo, EstadoCarta.oculta);
        long azulesOcultas = tableroCartaRepository.countByPartida_IdPartidaAndTipoAndEstado(
                idPartida, TipoCarta.azul, EstadoCarta.oculta);

        if (rojasOcultas == 0) {
            finalizarPartida(partida, true);
            return false;
        }
        if (azulesOcultas == 0) {
            finalizarPartida(partida, false);
            return false;
        }

        return true; // La partida continúa
    }

    private void finalizarPartida(Partida partida, boolean rojoGana) {
        partida.setEstado(Partida.EstadoPartida.finalizada);
        partida.setFechaFin(LocalDateTime.now());
        partida.setRojoGana(rojoGana);
        partidaRepository.save(partida);

        broadcastEstado(partida.getIdPartida());
    }

    private boolean esCartaDelEquipo(TipoCarta tipo, Equipo equipo) {
        return (TipoCarta.rojo.equals(tipo) && Equipo.rojo.equals(equipo)) ||
               (TipoCarta.azul.equals(tipo) && Equipo.azul.equals(equipo));
    }

    private void actualizarEstadisticasAgentes(List<VotoCarta> votos,
                                                Equipo equipoVotante,
                                                TableroCarta cartaRevelada) {
        boolean esAcierto = esCartaDelEquipo(cartaRevelada.getTipo(), equipoVotante);

        // Actualizar num_aciertos / num_fallos en jugador_partida para los agentes del equipo
        votos.forEach(v -> {
            JugadorPartida jp = v.getJugadorPartida();
            if (jp.getEquipo().equals(equipoVotante) && Rol.agente.equals(jp.getRol())) {
                // El agente votó la carta ganadora
                boolean votoGanador = v.getCartaTablero().getIdCartaTablero()
                        .equals(cartaRevelada.getIdCartaTablero());
                if (votoGanador) {
                    if (esAcierto) jp.setNumAciertos(jp.getNumAciertos() + 1);
                    else           jp.setNumFallos(jp.getNumFallos() + 1);
                    jugadorPartidaRepository.save(jp);
                }
            }
        });
    }

    private GameStateDTO buildGameState(Partida partida, String idGoogle) {
        JugadorPartida jp = requireJugadorEnPartida(idGoogle, partida.getIdPartida());
        List<TableroCarta> cartas = tableroCartaRepository
                .findByPartida_IdPartida(partida.getIdPartida());
        Turno turno = turnoRepository
                .findFirstByPartida_IdPartidaOrderByNumTurnoDesc(partida.getIdPartida())
                .orElse(null);
        List<VotoCarta> votos = turno != null
                ? votoCartaRepository.findByTurno_IdTurno(turno.getIdTurno())
                : List.of();
        return GameStateMapper.toDTO(partida, cartas, turno, votos,
                Rol.lider.equals(jp.getRol()));
    }

    private VotoRecibidoDTO buildVotoRecibidoDTO(Integer idTurno, List<VotoCarta> votos) {
        // Agrupa votos por carta
        Map<Integer, List<String>> votosPorCarta = votos.stream()
                .collect(Collectors.groupingBy(
                        v -> v.getCartaTablero().getIdCartaTablero(),
                        Collectors.mapping(
                                v -> v.getJugadorPartida().getJugador().getTag(),
                                Collectors.toList())));

        VotoRecibidoDTO dto = new VotoRecibidoDTO();
        dto.setIdTurno(idTurno);
        dto.setVotosPorCarta(votosPorCarta);
        return dto;
    }
}
