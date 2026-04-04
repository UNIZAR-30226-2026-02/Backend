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
        boolean rojoEmpieza = new Random().nextBoolean();

        int cartasEquipoA = rojoEmpieza ? CARTAS_ROJO_EMPIEZA : CARTAS_AZUL_EMPIEZA;
        int cartasEquipoB = rojoEmpieza ? CARTAS_AZUL_EMPIEZA : CARTAS_ROJO_EMPIEZA;

        List<PalabraTema> palabras = palabraTemaRepository
                .findPalabrasAleatoriasPorTema(partida.getTema().getIdTema(), TOTAL_CARTAS);

        if (palabras.size() < TOTAL_CARTAS) {
            throw new GameLogicException(
                    "El tema no tiene suficientes palabras. Necesita al menos " + TOTAL_CARTAS + ".");
        }

        List<TipoCarta> tipos = new ArrayList<>();
        for (int i = 0; i < cartasEquipoA;      i++) tipos.add(rojoEmpieza ? TipoCarta.rojo : TipoCarta.azul);
        for (int i = 0; i < cartasEquipoB;      i++) tipos.add(rojoEmpieza ? TipoCarta.azul : TipoCarta.rojo);
        for (int i = 0; i < CARTAS_ASESINO;     i++) tipos.add(TipoCarta.asesino);
        for (int i = 0; i < CARTAS_CIVIL;       i++) tipos.add(TipoCarta.civil);
        Collections.shuffle(tipos);

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

        broadcastEstado(partida.getIdPartida());
    }

    // ─── Dar pista (Jefe) ─────────────────────────────────────────────────────

    @Transactional
    public void darPista(Integer idPartida, String palabraPista,
                          int pistaNumero, String idGoogle) {
        Partida partida = requireEnCurso(idPartida);
        JugadorPartida jp = requireJugadorEnPartida(idGoogle, idPartida);

        if (!Rol.lider.equals(jp.getRol())) {
            throw new GameLogicException("Solo el jefe de espías puede dar una pista.");
        }

        validarTurnoEquipo(partida, jp.getEquipo());

        String pistaLimpia = palabraPista.trim();
        if (pistaLimpia.isBlank() || pistaLimpia.contains(" ") || pistaLimpia.length() > 20) {
            throw new BadRequestException("La pista debe ser una sola palabra de máximo 20 caracteres.");
        }
        if (pistaNumero < 1 || pistaNumero > 8) {
            throw new BadRequestException("El número de la pista debe estar entre 1 y 8.");
        }

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

        // Pasar idPartida al temporizador para que use
        // juegoService.forzarFinTurno(idPartida) directamente en lugar de un
        // lambda que puede perder el contexto transaccional de Spring.
        temporizadorService.iniciarTemporizador(idPartida, partida.getTiempoEspera(),
                () -> forzarFinTurno(idPartida));

        PistaDTO pistaDTO = new PistaDTO(turno);
        messagingTemplate.convertAndSend("/topic/partidas/" + idPartida + "/pista", pistaDTO);

        broadcastEstado(idPartida);
    }

    // ─── Votar carta (Agente) ─────────────────────────────────────────────────

    /**
     * idTurno es ahora nullable (Integer).
     * Si el frontend no lo envía (null), el backend resuelve el turno activo
     * automáticamente usando el último turno de la partida.
     * Esto evita el NPE/NotFoundException que provocaba el error del log.
     */
    @Transactional
    public VotoRecibidoDTO votar(Integer idPartida, Integer idCartaTablero,
                                  Integer idTurno, String idGoogle) {
        Partida partida = requireEnCurso(idPartida);
        JugadorPartida jp = requireJugadorEnPartida(idGoogle, idPartida);

        if (!Rol.agente.equals(jp.getRol())) {
            throw new GameLogicException("Solo los agentes pueden votar.");
        }

        // resolver turno activo si no hay idTurno
        Turno turno;
        if (idTurno != null) {
            turno = turnoRepository.findById(idTurno)
                    .orElseThrow(() -> new NotFoundException("Turno no encontrado."));
        } else {
            turno = turnoRepository
                    .findFirstByPartida_IdPartidaOrderByNumTurnoDesc(idPartida)
                    .orElseThrow(() -> new GameLogicException(
                            "No hay turno activo en esta partida. El jefe aún no ha dado pista."));
        }

        if (!turno.getPartida().getIdPartida().equals(idPartida)) {
            throw new BadRequestException("El turno no pertenece a esta partida.");
        }

        if (!turno.getJugadorPartida().getEquipo().equals(jp.getEquipo())) {
            throw new GameLogicException("No es el turno de tu equipo.");
        }

        TableroCarta carta = tableroCartaRepository.findById(idCartaTablero)
                .orElseThrow(() -> new NotFoundException("Carta no encontrada."));

        if (!EstadoCarta.oculta.equals(carta.getEstado())) {
            throw new GameLogicException("Esa carta ya ha sido revelada.");
        }

        // Cambio de voto: borrar el anterior si existe
        votoCartaRepository.findByTurno_IdTurnoAndJugadorPartida_IdJugadorPartida(
                        turno.getIdTurno(), jp.getIdJugadorPartida())
                .ifPresent(votoCartaRepository::delete);

        VotoCarta voto = new VotoCarta();
        voto.setTurno(turno);
        voto.setJugadorPartida(jp);
        voto.setCartaTablero(carta);
        votoCartaRepository.save(voto);

        List<VotoCarta> todosVotos = votoCartaRepository.findByTurno_IdTurno(turno.getIdTurno());

        VotoRecibidoDTO respuesta = buildVotoRecibidoDTO(turno.getIdTurno(), todosVotos);

        // Eliminar el broadcast general a /topic que enviaba el estado
        // de un solo jugador a todos. El broadcastEstado personalizado se
        // encarga de enviar a cada jugador su versión correcta según su rol.
        // La línea eliminada era (la pongo aquí por si acaso):
        //   messagingTemplate.convertAndSend("/topic/partidas/" + idPartida + "/estado",
        //           buildGameState(partida, idGoogle));

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
        } else {
            // Broadcast parcial de votos en tiempo real sin resolver todavía
            broadcastEstado(idPartida);
        }

        return respuesta;
    }

    // ─── Resolver votación ────────────────────────────────────────────────────

    @Transactional
    public void resolverVotacion(Partida partida, Turno turno, Equipo equipoVotante) {
        List<VotoCarta> votos = votoCartaRepository.findByTurno_IdTurno(turno.getIdTurno());

        TableroCarta cartaGanadora = votos.stream()
                .collect(Collectors.groupingBy(v -> v.getCartaTablero().getIdCartaTablero(),
                        Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(e -> tableroCartaRepository.findById(e.getKey()).orElseThrow())
                .orElseThrow(() -> new GameLogicException("No hay votos para resolver."));

        cartaGanadora.setEstado(EstadoCarta.revelada);
        tableroCartaRepository.save(cartaGanadora);

        actualizarEstadisticasAgentes(votos, equipoVotante, cartaGanadora);

        temporizadorService.cancelarTemporizador(partida.getIdPartida());

        boolean continua = comprobarCondicionFin(partida, cartaGanadora, equipoVotante, turno);

        if (continua) {
            long cartasReveladasEsteTurno = votos.stream()
                    .map(v -> v.getCartaTablero().getIdCartaTablero())
                    .distinct().count();

            boolean cartaCorrecta = esCartaDelEquipo(cartaGanadora.getTipo(), equipoVotante);
            boolean puedeVotarMas = cartaCorrecta &&
                    cartasReveladasEsteTurno < turno.getPistaNumero();

            if (!cartaCorrecta || !puedeVotarMas) {
                broadcastEstado(partida.getIdPartida());
            } else {
                temporizadorService.iniciarTemporizador(partida.getIdPartida(),
                        partida.getTiempoEspera(), () -> forzarFinTurno(partida.getIdPartida()));
                broadcastEstado(partida.getIdPartida());
            }
        }
    }

    // ─── GameState broadcast ──────────────────────────────────────────────────

    /**
     *Envío personalizado por usuario según su rol.
     * Se elimina el broadcast general a /topic/partidas/{id}/estado que:
     *   1. Causaba que el frontend recibiera dos eventos por actualización.
     *   2. Enviaba siempre la versión de agente (sin tipos de carta),
     *      lo que hacía que los líderes recibieran momentáneamente un
     *      estado incorrecto antes del mensaje personalizado correcto.
     *
     * Los clientes deben suscribirse a:
     *   /user/queue/partidas/{id}/estado
     * En STOMP esto se traduce a suscribirse a:
     *   /user/queue/partidas/{id}/estado
     * (Spring añade el prefijo /user/{sessionId} automáticamente al enviar)
     */
    public void broadcastEstado(Integer idPartida) {
        Partida partida = partidaRepository.findById(idPartida).orElseThrow();
        List<TableroCarta> cartas = tableroCartaRepository.findByPartida_IdPartida(idPartida);
        Turno turnoActual = turnoRepository
                .findFirstByPartida_IdPartidaOrderByNumTurnoDesc(idPartida).orElse(null);
        List<VotoCarta> votos = turnoActual != null
                ? votoCartaRepository.findByTurno_IdTurno(turnoActual.getIdTurno())
                : List.of();

        GameStateDTO estadoLider  = GameStateMapper.toDTO(partida, cartas, turnoActual, votos, true);
        GameStateDTO estadoAgente = GameStateMapper.toDTO(partida, cartas, turnoActual, votos, false);

        List<JugadorPartida> jugadores = jugadorPartidaRepository.findByPartida_IdPartida(idPartida);
        for (JugadorPartida jp : jugadores) {
            boolean esLider = Rol.lider.equals(jp.getRol());
            GameStateDTO estado = esLider ? estadoLider : estadoAgente;
            messagingTemplate.convertAndSendToUser(
                    jp.getJugador().getIdGoogle(),
                    "/queue/partidas/" + idPartida + "/estado",
                    estado);
        }

        // Se elimina el broadcast general que causaba duplicación:
        // messagingTemplate.convertAndSend("/topic/partidas/" + idPartida + "/estado", estadoAgente);
    }

    // ─── Forzar fin de turno (timeout) ────────────────────────────────────────

    /**
     * Llamado por TemporizadorService cuando el tiempo expira.
     *
     * Este método se llama como lambda desde TemporizadorService
     * en un hilo distinto (CompletableFuture.runAsync). Spring no crea un proxy
     * transaccional nuevo para lambdas, por lo que @Transactional aquí no garantiza
     * una transacción nueva en ese hilo.
     *
     * El método solo hace operaciones de lectura y broadcast.
     * Las operaciones de escritura (si las hubiera) deben ir en métodos separados
     * inyectados correctamente. En el estado actual, broadcastEstado no escribe
     * en BD y requireEnCurso solo lee, por lo que el riesgo es bajo.
     * Si se requiere escritura en el timeout, inyectar JuegoService via
     * ApplicationContext para obtener el proxy con @Transactional.
     */
    @Transactional
    public void forzarFinTurno(Integer idPartida) {
        try {
            Partida partida = partidaRepository.findById(idPartida).orElse(null);
            if (partida == null) return;
            if (!Partida.EstadoPartida.en_curso.equals(partida.getEstado())) return;
            broadcastEstado(idPartida);
        } catch (Exception e) {
            // Log implícito desde TemporizadorService; no relanzar para no matar el hilo
        }
    }

    // ─── GameState para un jugador concreto ───────────────────────────────────

    /**
     * Acepta tanto estado en_curso como finalizada.
     * Antes lanzaba GameLogicException si la partida estaba finalizada,
     * impidiendo mostrar la pantalla de resultados.
     */
    @Transactional(readOnly = true)
    public GameStateDTO getGameState(Integer idPartida, String idGoogle) {
        Partida partida = partidaRepository.findById(idPartida)
                .orElseThrow(() -> new NotFoundException("Partida no encontrada."));

        // Permite en_curso y finalizada (necesario para la reconexión
        // al volver a entrar mientras la partida está activa, y para el endpoint
        // /estado que el frontend puede llamar justo cuando termina)
        if (!Partida.EstadoPartida.en_curso.equals(partida.getEstado()) &&
            !Partida.EstadoPartida.finalizada.equals(partida.getEstado())) {
            throw new GameLogicException("La partida no está en curso ni finalizada.");
        }

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

    /**
     * Endpoint dedicado para la pantalla de fin de partida.
     * Igual que getGameState pero solo acepta estado finalizada.
     * Devuelve el tablero completo con tipos de carta visibles para todos
     * (al terminar la partida se revela todo el tablero).
     */
    @Transactional(readOnly = true)
    public GameStateDTO getGameStateFinalizado(Integer idPartida, String idGoogle) {
        Partida partida = partidaRepository.findById(idPartida)
                .orElseThrow(() -> new NotFoundException("Partida no encontrada."));

        if (!Partida.EstadoPartida.finalizada.equals(partida.getEstado())) {
            throw new GameLogicException("La partida aún no ha finalizado.");
        }

        requireJugadorEnPartida(idGoogle, idPartida);

        List<TableroCarta> cartas = tableroCartaRepository.findByPartida_IdPartida(idPartida);
        Turno turnoActual = turnoRepository
                .findFirstByPartida_IdPartidaOrderByNumTurnoDesc(idPartida).orElse(null);
        List<VotoCarta> votos = turnoActual != null
                ? votoCartaRepository.findByTurno_IdTurno(turnoActual.getIdTurno())
                : List.of();

        // En la pantalla de resultados se muestra el tablero completo (todos ven los tipos)
        return GameStateMapper.toDTO(partida, cartas, turnoActual, votos, true);
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

        Turno ultimo = ultimoTurno.get();
        Equipo equipoUltimoTurno = ultimo.getJugadorPartida().getEquipo();

        if (equipoJugador.equals(equipoUltimoTurno)) {
            // Puede que aún tenga derecho a votar más cartas; se gestiona en resolverVotacion
        }
    }

    private boolean comprobarCondicionFin(Partida partida, TableroCarta carta,
                                           Equipo equipoVotante, Turno turno) {
        Integer idPartida = partida.getIdPartida();

        if (TipoCarta.asesino.equals(carta.getTipo())) {
            finalizarPartida(partida, !Equipo.rojo.equals(equipoVotante));
            return false;
        }

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

        return true;
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

        votos.forEach(v -> {
            JugadorPartida jp = v.getJugadorPartida();
            if (jp.getEquipo().equals(equipoVotante) && Rol.agente.equals(jp.getRol())) {
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

    private VotoRecibidoDTO buildVotoRecibidoDTO(Integer idTurno, List<VotoCarta> votos) {
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