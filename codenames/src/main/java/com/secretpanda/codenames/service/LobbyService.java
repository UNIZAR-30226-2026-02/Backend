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
import com.secretpanda.codenames.model.JugadorPartida;
import com.secretpanda.codenames.model.Partida;
import com.secretpanda.codenames.model.Tema;
import com.secretpanda.codenames.repository.InventarioTemaRepository;
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
    private final InventarioTemaRepository inventarioTemaRepository;

public LobbyService(PartidaRepository partidaRepository,
                        JugadorRepository jugadorRepository,
                        JugadorPartidaRepository jugadorPartidaRepository,
                        TemaRepository temaRepository,
                        InventarioTemaRepository inventarioTemaRepository,
                        SimpMessagingTemplate messagingTemplate,
                        JuegoService juegoService) {
        this.partidaRepository = partidaRepository;
        this.jugadorRepository = jugadorRepository;
        this.jugadorPartidaRepository = jugadorPartidaRepository;
        this.temaRepository = temaRepository;
        this.inventarioTemaRepository = inventarioTemaRepository;
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

        long rojos = jugadores.stream()
                .filter(jp -> JugadorPartida.Equipo.rojo.equals(jp.getEquipo())).count();
        long azules = jugadores.stream()
                .filter(jp -> JugadorPartida.Equipo.azul.equals(jp.getEquipo())).count();

        if (rojos < 2 || azules < 2) {
            throw new GameLogicException("Cada equipo necesita al menos 2 jugadores.");
        }

        asignarRoles(jugadores);

        partida.setEstado(Partida.EstadoPartida.en_curso);
        partidaRepository.save(partida);

        juegoService.inicializarPartida(partida, jugadores);

        broadcastLobby(partida);
    }

    // ─── Abandonar lobby ───────────────────────────────────────────────────────

    /**
     * Cuando el creador abandona el lobby, se elimina también su registro
     * de JUGADOR_PARTIDA (antes quedaba huérfano en BD). Además, el broadcastLobby
     * al final envía el estado con estado: "finalizada", que es la señal que el
     * frontend debe usar para cerrar el lobby y redirigir a los demás jugadores.
     *
     * Contrato WebSocket tras este cambio:
     *   - El topic /topic/partidas/{id}/lobby recibe un LobbyStatusDTO con
     *     estado: "finalizada" cuando el creador abandona.
     *   - El frontend debe manejar estado === "finalizada" como señal de cierre.
     */
    @Transactional
    public void abandonarLobby(Integer idPartida, String idGoogle) {
        Partida partida = findPartidaEsperando(idPartida);
        JugadorPartida jp = findJugadorEnPartida(idGoogle, idPartida);

        boolean esCreador = partida.getCreador().getIdGoogle().equals(idGoogle);

        if (esCreador) {
            // Marcar partida como finalizada
            partida.setEstado(Partida.EstadoPartida.finalizada);
            partida.setFechaFin(LocalDateTime.now());
            partidaRepository.save(partida);

            // Eliminar el registro del creador en JUGADOR_PARTIDA
            // Antes este delete no existía, dejando al creador como participante
            // de una partida finalizada, lo que podía bloquear la creación de
            // nuevas partidas (validarSinPartidaActiva en PartidaService).
            jugadorPartidaRepository.delete(jp);

            // Actualizar lista de partidas públicas (si era pública, desaparece)
            broadcastPartidasPublicas();
        } else {
            // Jugador normal: simplemente lo eliminamos del lobby
            jugadorPartidaRepository.delete(jp);
        }

        // Broadcast del estado actualizado a todos los suscritos al lobby.
        // Si el creador abandonó, el DTO lleva estado: "finalizada" → señal de cierre.
        // Si fue otro jugador, el DTO lleva la lista actualizada sin él.
        broadcastLobby(partida);
    }

    // ─── Broadcast ─────────────────────────────────────────────────────────────

    public void broadcastLobby(Partida partida) {
        LobbyStatusDTO dto = buildLobbyDTO(partida);
        messagingTemplate.convertAndSend(
                "/topic/partidas/" + partida.getIdPartida() + "/lobby", dto);
    }

    // ─── Lista de partidas públicas ────────────────────────────────────────────

   @Transactional(readOnly = true)
    public List<PartidaPublicaDTO> listarPartidasPublicas(String idGoogle) {
        // 1. Obtenemos los IDs de los temas que el jugador SÍ tiene en su inventario
        List<Integer> misTemasIds = inventarioTemaRepository.findById_IdJugador(idGoogle)
                .stream()
                .map(inv -> inv.getTema().getIdTema())
                .collect(Collectors.toList());

        // 2. Obtenemos todas las partidas públicas en estado 'esperando'
        List<Partida> partidasDisponibles = partidaRepository
                .findPartidasPublicasDisponibles(Partida.EstadoPartida.esperando);

        // 3. Filtramos la lista de partidas:
        // Solo se quedan aquellas cuyo tema_id esté dentro de la lista 'misTemasIds'
        return partidasDisponibles.stream()
                .filter(p -> misTemasIds.contains(p.getTema().getIdTema()))
                .map(this::buildPartidaPublicaDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public void broadcastPartidasPublicas() {
        List<Partida> partidas = partidaRepository
                .findPartidasPublicasDisponibles(Partida.EstadoPartida.esperando);
        
        List<PartidaPublicaDTO> listaCompleta = partidas.stream()
                .map(this::buildPartidaPublicaDTO)
                .collect(Collectors.toList());

        messagingTemplate.convertAndSend("/topic/partidas/publicas", listaCompleta);
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