package com.secretpanda.codenames.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.secretpanda.codenames.dto.partida.JugadorLobbyDTO;
import com.secretpanda.codenames.dto.partida.LobbyStatusDTO;
import com.secretpanda.codenames.dto.partida.PartidaPublicaDTO;
import com.secretpanda.codenames.exception.BadRequestException;
import com.secretpanda.codenames.exception.GameLogicException;
import com.secretpanda.codenames.exception.NotFoundException;
import com.secretpanda.codenames.model.Jugador;
import com.secretpanda.codenames.model.JugadorPartida;
import com.secretpanda.codenames.model.Partida;
import com.secretpanda.codenames.model.Tema;
import com.secretpanda.codenames.repository.JugadorPartidaRepository;
import com.secretpanda.codenames.repository.JugadorRepository;
import com.secretpanda.codenames.repository.PartidaRepository;
import com.secretpanda.codenames.repository.TemaRepository;

@Service
public class LobbyService {

    private final PartidaRepository partidaRepository;
    private final JugadorRepository jugadorRepository;
    private final JugadorPartidaRepository jugadorPartidaRepository;
    private final TemaRepository temaRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final JuegoService juegoService;

    public LobbyService(PartidaRepository partidaRepository,
                        JugadorRepository jugadorRepository,
                        JugadorPartidaRepository jugadorPartidaRepository,
                        TemaRepository temaRepository,
                        SimpMessagingTemplate messagingTemplate,
                        JuegoService juegoService) {
        this.partidaRepository = partidaRepository;
        this.jugadorRepository = jugadorRepository;
        this.jugadorPartidaRepository = jugadorPartidaRepository;
        this.temaRepository = temaRepository;
        this.messagingTemplate = messagingTemplate;
        this.juegoService = juegoService;
    }

    // ─── GET Lobby ─────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public LobbyStatusDTO getLobby(Integer idPartida) {
        Partida partida = findPartidaEsperando(idPartida);
        return buildLobbyDTO(partida);
    }

    // ─── Cambio de equipo ──────────────────────────────────────────────────────

    @Transactional
    public void cambiarEquipo(Integer idPartida, String equipo, String idGoogle) {
        Partida partida = findPartidaEsperando(idPartida);
        JugadorPartida jp = findJugadorEnPartida(idGoogle, idPartida);

        JugadorPartida.Equipo equipoEnum;
        try {
            equipoEnum = JugadorPartida.Equipo.valueOf(equipo.toLowerCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Equipo inválido: " + equipo);
        }

        jp.setEquipo(equipoEnum);
        jugadorPartidaRepository.save(jp);

        broadcastLobby(partida);
    }

    // ─── Cambiar tema (solo creador, solo privada) ─────────────────────────────

    @Transactional
    public void cambiarTema(Integer idPartida, Integer idTema, String idGoogle) {
        Partida partida = findPartidaEsperando(idPartida);
        validarCreador(partida, idGoogle);

        if (partida.isEsPublica()) {
            throw new GameLogicException("No se puede cambiar el tema en partidas públicas.");
        }

        Tema tema = temaRepository.findById(idTema)
                .orElseThrow(() -> new NotFoundException("Tema no encontrado."));

        partida.setTema(tema);
        partidaRepository.save(partida);
        broadcastLobby(partida);
    }

    // ─── Cambiar tiempo de turno (solo creador, solo privada) ──────────────────

    @Transactional
    public void cambiarTiempoTurno(Integer idPartida, int tiempoEspera, String idGoogle) {
        Partida partida = findPartidaEsperando(idPartida);
        validarCreador(partida, idGoogle);

        if (partida.isEsPublica()) {
            throw new GameLogicException("No se puede cambiar el tiempo en partidas públicas.");
        }
        if (tiempoEspera != 30 && tiempoEspera != 60 && tiempoEspera != 90 && tiempoEspera != 120) {
            throw new BadRequestException("Tiempo de turno debe ser 30, 60, 90 o 120 segundos.");
        }

        partida.setTiempoEspera(tiempoEspera);
        partidaRepository.save(partida);
        broadcastLobby(partida);
    }

    // ─── Iniciar partida ───────────────────────────────────────────────────────

    @Transactional
    public void iniciarPartida(Integer idPartida, String idGoogle) {
        Partida partida = findPartidaEsperando(idPartida);
        validarCreador(partida, idGoogle);

        List<JugadorPartida> jugadores = jugadorPartidaRepository
                .findByPartida_IdPartidaAndAbandonoFalse(idPartida);

        // Validar mínimo 2 jugadores por equipo
        long rojos = jugadores.stream()
                .filter(jp -> JugadorPartida.Equipo.rojo.equals(jp.getEquipo())).count();
        long azules = jugadores.stream()
                .filter(jp -> JugadorPartida.Equipo.azul.equals(jp.getEquipo())).count();

        if (rojos < 2 || azules < 2) {
            throw new GameLogicException("Cada equipo necesita al menos 2 jugadores.");
        }

        // Asignar roles automáticamente (1 líder por equipo, resto agentes)
        asignarRoles(jugadores);

        // Cambiar estado
        partida.setEstado(Partida.EstadoPartida.en_curso);
        partidaRepository.save(partida);

        // Generar tablero e inicializar el primer turno
        juegoService.inicializarPartida(partida, jugadores);

        // Notificar lobby actualizado (para que el frontend sepa que está en_curso)
        broadcastLobby(partida);
    }

    // ─── Abandonar lobby ───────────────────────────────────────────────────────

    @Transactional
    public void abandonarLobby(Integer idPartida, String idGoogle) {
        Partida partida = findPartidaEsperando(idPartida);
        JugadorPartida jp = findJugadorEnPartida(idGoogle, idPartida);

        boolean esCreador = partida.getCreador().getIdGoogle().equals(idGoogle);

        if (esCreador) {
            // Si el creador sale, se aborta la partida
            partida.setEstado(Partida.EstadoPartida.finalizada);
            partida.setFechaFin(LocalDateTime.now());
            partidaRepository.save(partida);
            broadcastPartidasPublicas();
        } else {
            // Simplemente lo quitamos
            jugadorPartidaRepository.delete(jp);
        }

        broadcastLobby(partida);
    }

    // ─── Broadcast ─────────────────────────────────────────────────────────────

    public void broadcastLobby(Partida partida) {
        LobbyStatusDTO dto = buildLobbyDTO(partida);
        messagingTemplate.convertAndSend(
                "/topic/partidas/" + partida.getIdPartida() + "/lobby", dto);
    }

    // ─── Lista de partidas públicas ────────────────────────────────────────────

    public List<PartidaPublicaDTO> listarPartidasPublicas() {
        List<Partida> partidas = partidaRepository
                .findPartidasPublicasDisponibles(Partida.EstadoPartida.esperando);

        return partidas.stream().map(this::buildPartidaPublicaDTO).collect(Collectors.toList());
    }

    public void broadcastPartidasPublicas() {
        messagingTemplate.convertAndSend("/topic/partidas/publicas", listarPartidasPublicas());
    }

    // ─── Helpers ───────────────────────────────────────────────────────────────

    private Partida findPartidaEsperando(Integer idPartida) {
        Partida p = partidaRepository.findById(idPartida)
                .orElseThrow(() -> new NotFoundException("Partida no encontrada."));
        if (!Partida.EstadoPartida.esperando.equals(p.getEstado())) {
            throw new GameLogicException("La partida no está en estado 'esperando'.");
        }
        return p;
    }

    private JugadorPartida findJugadorEnPartida(String idGoogle, Integer idPartida) {
        return jugadorPartidaRepository
                .findByJugador_IdGoogleAndPartida_IdPartida(idGoogle, idPartida)
                .orElseThrow(() -> new BadRequestException("No perteneces a esta partida."));
    }

    private void validarCreador(Partida partida, String idGoogle) {
        if (!partida.getCreador().getIdGoogle().equals(idGoogle)) {
            throw new GameLogicException("Solo el creador puede realizar esta acción.");
        }
    }

    private void asignarRoles(List<JugadorPartida> jugadores) {
        // 1 líder por equipo (el primero de cada equipo), el resto agentes
        boolean lidRojoAsignado = false;
        boolean lidAzulAsignado = false;

        for (JugadorPartida jp : jugadores) {
            if (JugadorPartida.Equipo.rojo.equals(jp.getEquipo())) {
                if (!lidRojoAsignado) {
                    jp.setRol(JugadorPartida.Rol.lider);
                    lidRojoAsignado = true;
                } else {
                    jp.setRol(JugadorPartida.Rol.agente);
                }
            } else {
                if (!lidAzulAsignado) {
                    jp.setRol(JugadorPartida.Rol.lider);
                    lidAzulAsignado = true;
                } else {
                    jp.setRol(JugadorPartida.Rol.agente);
                }
            }
        }
        jugadorPartidaRepository.saveAll(jugadores);
    }

    LobbyStatusDTO buildLobbyDTO(Partida partida) {
        List<JugadorPartida> jugadores = jugadorPartidaRepository
                .findByPartida_IdPartida(partida.getIdPartida());

        List<JugadorLobbyDTO> jugadoresDTO = jugadores.stream()
                .map(jp -> new JugadorLobbyDTO(
                        jp.getJugador().getTag(),
                        jp.getJugador().getFotoPerfil(),
                        jp.getEquipo().name()))
                .collect(Collectors.toList());

        long rojos = jugadores.stream()
                .filter(jp -> JugadorPartida.Equipo.rojo.equals(jp.getEquipo())
                           && !jp.isAbandono()).count();
        long azules = jugadores.stream()
                .filter(jp -> JugadorPartida.Equipo.azul.equals(jp.getEquipo())
                           && !jp.isAbandono()).count();

        LobbyStatusDTO dto = new LobbyStatusDTO();
        dto.setIdPartida(partida.getIdPartida());
        dto.setCodigoPartida(partida.getCodigoPartida());
        dto.setEstado(partida.getEstado().name());
        dto.setMaxJugadores(partida.getMaxJugadores());
        dto.setEsPublica(partida.isEsPublica());
        dto.setIdTema(partida.getTema().getIdTema());
        dto.setNombreTema(partida.getTema().getNombre());
        dto.setTiempoEspera(partida.getTiempoEspera());
        dto.setTagCreador(partida.getCreador().getTag());
        dto.setHayMinimo(rojos >= 2 && azules >= 2);
        dto.setJugadores(jugadoresDTO);
        return dto;
    }

    PartidaPublicaDTO buildPartidaPublicaDTO(Partida partida) {
        long actuales = jugadorPartidaRepository.countByPartida_IdPartidaAndAbandonoFalse(
                partida.getIdPartida());
        return new PartidaPublicaDTO(
                partida.getIdPartida(),
                partida.getCreador().getTag(),
                partida.getTema().getNombre(),
                partida.getTiempoEspera(),
                partida.getMaxJugadores(),
                (int) actuales);
    }
    
}
