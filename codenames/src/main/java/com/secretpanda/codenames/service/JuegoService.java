package com.secretpanda.codenames.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationContext;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.secretpanda.codenames.dto.juego.GameStateDTO;
import com.secretpanda.codenames.dto.juego.PartidaFinDTO;
import com.secretpanda.codenames.dto.juego.PistaDTO;
import com.secretpanda.codenames.dto.juego.VotoRecibidoDTO;
import com.secretpanda.codenames.exception.BadRequestException;
import com.secretpanda.codenames.exception.GameLogicException;
import com.secretpanda.codenames.exception.NotFoundException;
import com.secretpanda.codenames.mapper.juego.GameStateMapper;
import com.secretpanda.codenames.model.Jugador;
import com.secretpanda.codenames.model.JugadorPartida;
import com.secretpanda.codenames.model.JugadorPartida.Equipo;
import com.secretpanda.codenames.model.JugadorPartida.Rol;
import com.secretpanda.codenames.model.PalabraTema;
import com.secretpanda.codenames.model.Partida;
import com.secretpanda.codenames.model.TableroCarta;
import com.secretpanda.codenames.model.TableroCarta.EstadoCarta;
import com.secretpanda.codenames.model.TableroCarta.TipoCarta;
import com.secretpanda.codenames.model.Turno;
import com.secretpanda.codenames.model.VotoCarta;
import com.secretpanda.codenames.repository.JugadorPartidaRepository;
import com.secretpanda.codenames.repository.JugadorRepository;
import com.secretpanda.codenames.repository.PalabraTemaRepository;
import com.secretpanda.codenames.repository.PartidaRepository;
import com.secretpanda.codenames.repository.TableroCartaRepository;
import com.secretpanda.codenames.repository.TurnoRepository;
import com.secretpanda.codenames.repository.VotoCartaRepository;

@Service
public class JuegoService {

    // Distribución estándar de Codenames en tablero 4×5 (20 cartas)
    private static final int TOTAL_CARTAS          = 20; // 4 filas × 5 columnas
    // El equipo que empieza tiene 9 cartas, el otro 8, 1 asesino, 2 civiles
    private static final int CARTAS_EQUIPO_QUE_INICIA = 9;
    private static final int CARTAS_EQUIPO_SEGUNDO = 8;
    private static final int CARTAS_ASESINO        = 1;
    private static final int CARTAS_CIVIL          = TOTAL_CARTAS - CARTAS_EQUIPO_QUE_INICIA - CARTAS_EQUIPO_SEGUNDO - CARTAS_ASESINO;

    private final PartidaRepository          partidaRepository;
    private final JugadorPartidaRepository   jugadorPartidaRepository;
    private final TableroCartaRepository     tableroCartaRepository;
    private final TurnoRepository            turnoRepository;
    private final VotoCartaRepository        votoCartaRepository;
    private final PalabraTemaRepository      palabraTemaRepository;
    private final JugadorRepository          jugadorRepository;
    private final SimpMessagingTemplate      messagingTemplate;
    private final TemporizadorService        temporizadorService;
    private final LeaderboardService leaderboardService;
    private final JugadorService jugadorService;
    private final ApplicationContext applicationContext;

    public JuegoService(PartidaRepository partidaRepository,
                        JugadorPartidaRepository jugadorPartidaRepository,
                        TableroCartaRepository tableroCartaRepository,
                        TurnoRepository turnoRepository,
                        VotoCartaRepository votoCartaRepository,
                        PalabraTemaRepository palabraTemaRepository,
                        JugadorRepository jugadorRepository,
                        SimpMessagingTemplate messagingTemplate,
                        TemporizadorService temporizadorService,
                        LeaderboardService leaderboardService,
                        JugadorService jugadorService,
                        ApplicationContext applicationContext) {
        this.partidaRepository        = partidaRepository;
        this.jugadorPartidaRepository = jugadorPartidaRepository;
        this.tableroCartaRepository   = tableroCartaRepository;
        this.turnoRepository          = turnoRepository;
        this.votoCartaRepository      = votoCartaRepository;
        this.palabraTemaRepository    = palabraTemaRepository;
        this.jugadorRepository        = jugadorRepository;
        this.messagingTemplate        = messagingTemplate;
        this.temporizadorService      = temporizadorService;
        this.leaderboardService       = leaderboardService;
        this.jugadorService           = jugadorService;
        this.applicationContext       = applicationContext;
    }

    // ─── Inicializar partida ──────────────────────────────────────────────────

    @Transactional
    public void inicializarPartida(Partida partida, List<JugadorPartida> jugadores) {
        boolean rojoEmpieza = new Random().nextBoolean();

        int totalRojas = rojoEmpieza ? CARTAS_EQUIPO_QUE_INICIA : CARTAS_EQUIPO_SEGUNDO;
        int totalAzules = rojoEmpieza ? CARTAS_EQUIPO_SEGUNDO : CARTAS_EQUIPO_QUE_INICIA;

        List<PalabraTema> palabras = palabraTemaRepository
                .findPalabrasAleatoriasPorTema(partida.getTema().getIdTema(), TOTAL_CARTAS);

        if (palabras.size() < TOTAL_CARTAS) {
            throw new GameLogicException(
                    "El tema no tiene suficientes palabras. Necesita al menos " + TOTAL_CARTAS + ".");
        }

        List<TipoCarta> tipos = new ArrayList<>();
        for (int i = 0; i < totalRojas; i++) tipos.add(rojoEmpieza ? TipoCarta.rojo : TipoCarta.azul);
        for (int i = 0; i < totalAzules; i++) tipos.add(rojoEmpieza ? TipoCarta.azul : TipoCarta.rojo);
        for (int i = 0; i < CARTAS_ASESINO; i++) tipos.add(TipoCarta.asesino);
        for (int i = 0; i < CARTAS_CIVIL; i++) tipos.add(TipoCarta.civil);
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

        Equipo equipoInicial = rojoEmpieza ? Equipo.rojo : Equipo.azul;
        JugadorPartida liderInicial = jugadores.stream()
                .filter(jp -> jp.getEquipo().equals(equipoInicial) && jp.getRol().equals(Rol.lider))
                .findFirst()
                .orElseThrow(() -> new GameLogicException("No hay líder en el equipo inicial."));

        Turno turnoInicial = new Turno();
        turnoInicial.setPartida(partida);
        turnoInicial.setJugadorPartida(liderInicial);
        turnoInicial.setNumTurno(1);
        turnoInicial.setPalabraPista(null);
        turnoInicial.setPistaNumero(null);
        turnoInicial.setAciertosTurno(0);
        turnoRepository.save(turnoInicial);

        // Uso del Bean a través de ApplicationContext para asegurar transaccionalidad en el hilo del timer
        temporizadorService.iniciarTemporizador(partida.getIdPartida(), partida.getTiempoEspera(), 
                () -> applicationContext.getBean(JuegoService.class).forzarFinTurno(partida.getIdPartida()));

        broadcastEstado(partida.getIdPartida());
    }

    // ─── Dar pista (Jefe) ─────────────────────────────────────────────────────

    @Transactional
    public void darPista(Integer idPartida, String palabraPista, int pistaNumero, String idGoogle) {
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

        Turno turno = turnoRepository
                .findFirstByPartida_IdPartidaOrderByNumTurnoDesc(idPartida)
                .orElseThrow(() -> new GameLogicException("No hay turno activo para dar la pista."));

        if (turno.getPalabraPista() != null) {
            throw new GameLogicException("El turno activo ya tiene una pista dada.");
        }

        turno.setPalabraPista(pistaLimpia);
        turno.setPistaNumero(pistaNumero);
        turnoRepository.save(turno);

        temporizadorService.iniciarTemporizador(idPartida, partida.getTiempoEspera(),
                () -> applicationContext.getBean(JuegoService.class).forzarFinTurno(idPartida));

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    enviarNotificacionPista(idPartida, turno);
                }
            });
        } else {
            enviarNotificacionPista(idPartida, turno);
        }
    }

    private void enviarNotificacionPista(Integer idPartida, Turno turno) {
        PistaDTO pistaDTO = new PistaDTO(turno);
        messagingTemplate.convertAndSend("/topic/partidas/" + idPartida + "/pista", pistaDTO);
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

        votoCartaRepository.findByTurno_IdTurnoAndJugadorPartida_IdJugadorPartida(
                        turno.getIdTurno(), jp.getIdJugadorPartida())
                .ifPresent(v -> {
                    votoCartaRepository.delete(v);
                    votoCartaRepository.flush();
                });

        VotoCarta voto = new VotoCarta();
        voto.setTurno(turno);
        voto.setJugadorPartida(jp);
        voto.setCartaTablero(carta);
        votoCartaRepository.saveAndFlush(voto);

        List<VotoCarta> todosVotos = votoCartaRepository.findByTurno_IdTurno(turno.getIdTurno());

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
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    broadcastEstado(idPartida);
                }
            });
        }

        return buildVotoRecibidoDTO(turno.getIdTurno(), todosVotos);
    }

    // ─── Resolver votación ────────────────────────────────────────────────────

    @Transactional
    public void resolverVotacion(Partida partida, Turno turno, Equipo equipoVotante) {
        List<VotoCarta> votos = votoCartaRepository.findByTurno_IdTurno(turno.getIdTurno());

        if (votos.isEmpty()) return;

        TableroCarta cartaGanadora = votos.stream()
                .collect(Collectors.groupingBy(v -> v.getCartaTablero().getIdCartaTablero(),
                        Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(e -> tableroCartaRepository.findById(e.getKey()).orElseThrow())
                .orElseThrow(() -> new GameLogicException("Error al procesar la votación."));

        cartaGanadora.setEstado(EstadoCarta.revelada);
        tableroCartaRepository.save(cartaGanadora);

        actualizarEstadisticasAgentes(votos, equipoVotante, cartaGanadora);

        votos.forEach(v -> v.setCartaRevelada(cartaGanadora));
        votoCartaRepository.saveAll(votos);
        votoCartaRepository.flush();

        temporizadorService.cancelarTemporizador(partida.getIdPartida());

        boolean continuaPartida = comprobarCondicionFin(partida, cartaGanadora, equipoVotante, turno);

        if (continuaPartida) {
            boolean esAciertoPropio = esCartaDelEquipo(cartaGanadora.getTipo(), equipoVotante);

            if (!esAciertoPropio) {
                prepararTurnoRival(partida, equipoVotante);
            } else {
                turno.setAciertosTurno(turno.getAciertosTurno() + 1);
                turnoRepository.save(turno);

                if (turno.getPistaNumero() != null && turno.getAciertosTurno() >= turno.getPistaNumero()) {
                    prepararTurnoRival(partida, equipoVotante);
                } else {
                    temporizadorService.iniciarTemporizador(partida.getIdPartida(),
                            partida.getTiempoEspera(), () -> applicationContext.getBean(JuegoService.class).forzarFinTurno(partida.getIdPartida()));
                }
            }
            
            Integer idPartida = partida.getIdPartida();
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    broadcastEstado(idPartida);
                }
            });
        }
    }

    private void prepararTurnoRival(Partida partida, Equipo equipoActual) {
        Equipo rival = (equipoActual == Equipo.rojo) ? Equipo.azul : Equipo.rojo;
        
        JugadorPartida liderRival = jugadorPartidaRepository.findByPartida_IdPartida(partida.getIdPartida())
                .stream()
                .filter(jp -> jp.getEquipo().equals(rival) && jp.getRol().equals(Rol.lider))
                .findFirst()
                .orElseThrow(() -> new GameLogicException("No hay líder en el equipo rival."));

        Turno turnoVacio = new Turno();
        turnoVacio.setPartida(partida);
        turnoVacio.setJugadorPartida(liderRival);
        turnoVacio.setNumTurno(turnoRepository.findByPartida_IdPartidaOrderByNumTurnoAsc(partida.getIdPartida()).size() + 1);
        turnoVacio.setPalabraPista(null);
        turnoVacio.setPistaNumero(null);
        turnoVacio.setAciertosTurno(0);
        turnoRepository.save(turnoVacio);
        
        temporizadorService.iniciarTemporizador(partida.getIdPartida(), partida.getTiempoEspera(), 
                () -> applicationContext.getBean(JuegoService.class).forzarFinTurno(partida.getIdPartida()));
    }

    // ─── GameState broadcast ──────────────────────────────────────────────────

    @Transactional(readOnly = true)
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
    }

    // ─── Forzar fin de turno (timeout) ────────────────────────────────────────

    @Transactional
    public void forzarFinTurno(Integer idPartida) {
        Partida partida = partidaRepository.findById(idPartida).orElse(null);
        if (partida == null || !Partida.EstadoPartida.en_curso.equals(partida.getEstado())) return;

        Turno turnoActual = turnoRepository.findFirstByPartida_IdPartidaOrderByNumTurnoDesc(idPartida).orElse(null);
        if (turnoActual == null) return;

        List<VotoCarta> votos = votoCartaRepository.findByTurno_IdTurno(turnoActual.getIdTurno());

        if (!votos.isEmpty()) {
            Map<Integer, Long> conteo = votos.stream()
                .collect(Collectors.groupingBy(v -> v.getCartaTablero().getIdCartaTablero(), Collectors.counting()));

            long maxVotos = Collections.max(conteo.values());
            List<Integer> ganadores = conteo.entrySet().stream()
                .filter(e -> e.getValue() == maxVotos)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

            if (ganadores.size() == 1) {
                resolverVotacion(partida, turnoActual, turnoActual.getJugadorPartida().getEquipo());
                return;
            }
        }

        prepararTurnoRival(partida, turnoActual.getJugadorPartida().getEquipo());
        broadcastEstado(idPartida);
    }

    // ─── GameState para un jugador concreto ───────────────────────────────────

    @Transactional(readOnly = true)
    public GameStateDTO getGameState(Integer idPartida, String idGoogle) {
        Partida partida = partidaRepository.findById(idPartida)
                .orElseThrow(() -> new NotFoundException("Partida no encontrada."));

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
            if (ultimo.getPalabraPista() != null) {
                throw new GameLogicException("Tu equipo ya ha dado una pista. Debes esperar al turno del rival.");
            }
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

        for (JugadorPartida jp : partida.getJugadores()) {
            Jugador j = jp.getJugador();
            boolean esRojo = jp.getEquipo() == JugadorPartida.Equipo.rojo;
            boolean gano = (rojoGana && esRojo) || (!rojoGana && !esRojo);
            
            jugadorService.procesarFinPartida(j.getIdGoogle(), gano, jp.getNumAciertos(), jp.getNumFallos());
        }

        leaderboardService.broadcastGlobalRanking(); 
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

    @Transactional(readOnly = true)
    public PartidaFinDTO getFinPartida(Integer idPartida, String idGoogle) {
        Partida partida = partidaRepository.findById(idPartida)
                .orElseThrow(() -> new NotFoundException("Partida no encontrada."));

        if (!Partida.EstadoPartida.finalizada.equals(partida.getEstado())) {
            throw new GameLogicException("La partida aún no ha finalizado.");
        }
        
        requireJugadorEnPartida(idGoogle, idPartida);

        long aciertosRojo = tableroCartaRepository.countByPartida_IdPartidaAndTipoAndEstado(
                idPartida, TipoCarta.rojo, EstadoCarta.revelada);
        long aciertosAzul = tableroCartaRepository.countByPartida_IdPartidaAndTipoAndEstado(
                idPartida, TipoCarta.azul, EstadoCarta.revelada);

        String equipoGanador = (partida.getRojoGana() != null && partida.getRojoGana()) ? "Rojo" : "Azul";

        PartidaFinDTO dto = new PartidaFinDTO();
        dto.setEquipoGanador(equipoGanador);
        dto.setAciertosRojo((int) aciertosRojo);
        dto.setAciertosAzul((int) aciertosAzul);

        return dto;
    }
}
