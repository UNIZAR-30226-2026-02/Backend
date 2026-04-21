package com.secretpanda.codenames.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.secretpanda.codenames.dto.partida.CrearPartidaDTO;
import com.secretpanda.codenames.dto.partida.JugadorLobbyDTO;
import com.secretpanda.codenames.dto.partida.LobbyStatusDTO;
import com.secretpanda.codenames.dto.partida.RolPartidaDTO;
import com.secretpanda.codenames.exception.BadRequestException;
import com.secretpanda.codenames.exception.GameLogicException;
import com.secretpanda.codenames.exception.NotFoundException;
import com.secretpanda.codenames.model.Jugador;
import com.secretpanda.codenames.model.JugadorPartida;
import com.secretpanda.codenames.model.Partida;
import com.secretpanda.codenames.model.Tema;
import com.secretpanda.codenames.repository.InventarioTemaRepository;
import com.secretpanda.codenames.repository.JugadorPartidaRepository;
import com.secretpanda.codenames.repository.JugadorRepository;
import com.secretpanda.codenames.repository.PartidaRepository;
import com.secretpanda.codenames.repository.TableroCartaRepository;
import com.secretpanda.codenames.repository.TemaRepository;
import com.secretpanda.codenames.util.CodigoPartidaGenerator;

@Service
public class PartidaService {

    @Value("${game.penalizacion-abandono:5}")
    private int balasPenalizacionAbandono;

    private final PartidaRepository partidaRepository;
    private final JugadorRepository jugadorRepository;
    private final TemaRepository temaRepository;
    private final JugadorPartidaRepository jugadorPartidaRepository;
    private final InventarioTemaRepository inventarioTemaRepository;
    private final TableroCartaRepository tableroCartaRepository;
    private final CodigoPartidaGenerator codigoGenerator;
    private final SimpMessagingTemplate messagingTemplate;
    private final JugadorService jugadorService;
    private final JuegoService juegoService;

    public PartidaService(PartidaRepository partidaRepository,
                          JugadorRepository jugadorRepository,
                          TemaRepository temaRepository,
                          JugadorPartidaRepository jugadorPartidaRepository,
                          InventarioTemaRepository inventarioTemaRepository,
                          TableroCartaRepository tableroCartaRepository,
                          CodigoPartidaGenerator codigoGenerator,
                          SimpMessagingTemplate messagingTemplate,
                          JugadorService jugadorService,
                          JuegoService juegoService) {
        this.partidaRepository = partidaRepository;
        this.jugadorRepository = jugadorRepository;
        this.temaRepository = temaRepository;
        this.jugadorPartidaRepository = jugadorPartidaRepository;
        this.inventarioTemaRepository = inventarioTemaRepository;
        this.tableroCartaRepository = tableroCartaRepository;
        this.codigoGenerator = codigoGenerator;
        this.messagingTemplate = messagingTemplate;
        this.jugadorService = jugadorService;
        this.juegoService = juegoService;
    }

    // ─── Crear partida ─────────────────────────────────────────────────────────

    @Transactional
    public LobbyStatusDTO crearPartida(CrearPartidaDTO dto, String idGoogle) {
        validarSinPartidaActiva(idGoogle);

        Tema tema = temaRepository.findById(dto.getIdTema())
                .orElseThrow(() -> new NotFoundException("El tema seleccionado no existe."));
        Jugador creador = findJugador(idGoogle);

        if (!inventarioTemaRepository.existsById_IdJugadorAndId_IdTema(idGoogle, dto.getIdTema())) {
            throw new GameLogicException("No tienes adquirido ese tema de cartas.");
        }

        String codigo;
        do {
            codigo = codigoGenerator.generarCodigo();
        } while (partidaRepository.existsByCodigoPartida(codigo));

        Partida partida = new Partida();
        partida.setTema(tema);
        partida.setCreador(creador);
        partida.setTiempoEspera(dto.getTiempoEspera());
        partida.setMaxJugadores(dto.getMaxJugadores());
        partida.setEsPublica(dto.isEsPublica());
        partida.setEstado(Partida.EstadoPartida.esperando);
        partida.setCodigoPartida(codigo);
        partida = partidaRepository.save(partida);

        JugadorPartida jp = new JugadorPartida();
        jp.setJugador(creador);
        jp.setPartida(partida);
        jp.setEquipo(JugadorPartida.Equipo.rojo);
        jp.setRol(JugadorPartida.Rol.agente);
        jugadorPartidaRepository.save(jp);

        return buildLobbyStatusDTO(partida, List.of(jp));
    }

    // ─── Unirse a privada ──────────────────────────────────────────────────────

    @Transactional
    public Integer unirsePartidaPrivada(String codigoPartida, String idGoogle) {
        validarSinPartidaActiva(idGoogle);

        Partida partida = partidaRepository.findByCodigoPartida(codigoPartida.toUpperCase().trim())
                .orElseThrow(() -> new NotFoundException("Partida no encontrada."));

        if (partida.isEsPublica()) {
            throw new GameLogicException("Esta partida es pública. Usa el endpoint de unirse a pública.");
        }

        unirseValidado(partida, idGoogle);
        return partida.getIdPartida();
    }

    // ─── Unirse a pública ──────────────────────────────────────────────────────

    @Transactional
    public void unirsePartidaPublica(Integer idPartida, String idGoogle) {
        validarSinPartidaActiva(idGoogle);

        Partida partida = partidaRepository.findById(idPartida)
                .orElseThrow(() -> new NotFoundException("Partida no encontrada."));

        if (!partida.isEsPublica()) {
            throw new GameLogicException("Esta partida es privada.");
        }

        // RF-15: Un usuario solo puede unirse a partidas públicas con temas que ya posea (Básico ID 1 siempre)
        Integer idTema = partida.getTema().getIdTema();
        if (idTema != 1) {
            boolean tieneTema = inventarioTemaRepository.existsById_IdJugadorAndId_IdTema(idGoogle, idTema);
            if (!tieneTema) {
                throw new GameLogicException("No puedes unirte a esta partida pública porque no has adquirido el paquete de cartas '" 
                                            + partida.getTema().getNombre() + "'.");
            }
        }

        unirseValidado(partida, idGoogle);
    }

    // ─── Abandonar (lobby o en curso) ──────────────────────────────────────────

    @Transactional
    public void abandonar(Integer idPartida, String idGoogle) {
        Partida partida = partidaRepository.findByIdForUpdate(idPartida)
                .orElseThrow(() -> new NotFoundException("Partida no encontrada."));

        JugadorPartida jp = jugadorPartidaRepository
                .findByJugador_IdGoogleAndPartida_IdPartida(idGoogle, idPartida)
                .orElseThrow(() -> new BadRequestException("No perteneces a esta partida."));

        if (Partida.EstadoPartida.en_curso.equals(partida.getEstado())) {
            // RF-35: Penalización de 5 balas por abandono
            jugadorService.modificarBalas(idGoogle, -5);

            jp.setAbandono(true);
            jugadorPartidaRepository.save(jp);

            // RF-35: Si abandona un Jefe de Espionaje (Líder), su equipo pierde automáticamente
            if (JugadorPartida.Rol.lider.equals(jp.getRol())) {
                boolean rojoGana = !JugadorPartida.Equipo.rojo.equals(jp.getEquipo());
                finalizarPartidaManual(partida, rojoGana);
            } else {
                List<JugadorPartida> activos = jugadorPartidaRepository
                        .findByPartida_IdPartidaAndAbandonoFalse(idPartida);
                
                long agentesEnMiEquipo = activos.stream()
                        .filter(a -> a.getEquipo().equals(jp.getEquipo()) && a.getRol().equals(JugadorPartida.Rol.agente))
                        .count();

                if (agentesEnMiEquipo == 0) {
                    // Si el equipo se queda sin agentes, no pueden votar -> Derrota automática
                    boolean rojoGana = !JugadorPartida.Equipo.rojo.equals(jp.getEquipo());
                    finalizarPartidaManual(partida, rojoGana);
                } else {
                    juegoService.broadcastEstado(idPartida);
                }
            }
        } 
        else {
            boolean esCreador = partida.getCreador().getIdGoogle().equals(idGoogle);
            if (esCreador) {
                partida.setEstado(Partida.EstadoPartida.finalizada);
                partida.setFechaFin(LocalDateTime.now());
                partidaRepository.save(partida);
            }
            jugadorPartidaRepository.delete(jp);
        }
    }

    // ─── Rol del jugador ───────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public RolPartidaDTO getRolJugador(Integer idPartida, String idGoogle) {
        JugadorPartida jp = jugadorPartidaRepository
                .findByJugador_IdGoogleAndPartida_IdPartida(idGoogle, idPartida)
                .orElseThrow(() -> new NotFoundException("No perteneces a esta partida."));

        long cartasRojo = tableroCartaRepository.countByPartida_IdPartidaAndTipoAndEstado(
                idPartida,
                com.secretpanda.codenames.model.TableroCarta.TipoCarta.rojo,
                com.secretpanda.codenames.model.TableroCarta.EstadoCarta.oculta);
        long cartasAzul = tableroCartaRepository.countByPartida_IdPartidaAndTipoAndEstado(
                idPartida,
                com.secretpanda.codenames.model.TableroCarta.TipoCarta.azul,
                com.secretpanda.codenames.model.TableroCarta.EstadoCarta.oculta);

        String equipoInicial = (cartasRojo >= cartasAzul) ? "rojo" : "azul";

        return new RolPartidaDTO(
                jp.getRol().name(),
                jp.getEquipo().name(),
                equipoInicial);
    }

    // ─── Helpers ───────────────────────────────────────────────────────────────

    private void unirseValidado(Partida partida, String idGoogle) {
        if (!Partida.EstadoPartida.esperando.equals(partida.getEstado())) {
            throw new GameLogicException("La partida ya ha comenzado o finalizado.");
        }

        long actuales = jugadorPartidaRepository
                .countByPartida_IdPartidaAndAbandonoFalse(partida.getIdPartida());
        if (actuales >= partida.getMaxJugadores()) {
            throw new GameLogicException("La sala está llena.");
        }
        if (jugadorPartidaRepository.existsByJugador_IdGoogleAndPartida_IdPartida(
                idGoogle, partida.getIdPartida())) {
            throw new GameLogicException("Ya estás en esta partida.");
        }

        Jugador jugador = findJugador(idGoogle);

        JugadorPartida jp = new JugadorPartida();
        jp.setJugador(jugador);
        jp.setPartida(partida);
        long rojos = jugadorPartidaRepository
                .findByPartida_IdPartidaAndEquipo(partida.getIdPartida(), JugadorPartida.Equipo.rojo).size();
        long azules = jugadorPartidaRepository
                .findByPartida_IdPartidaAndEquipo(partida.getIdPartida(), JugadorPartida.Equipo.azul).size();
        jp.setEquipo(rojos <= azules ? JugadorPartida.Equipo.rojo : JugadorPartida.Equipo.azul);
        jp.setRol(JugadorPartida.Rol.agente);
        jugadorPartidaRepository.save(jp);
    }

    private void validarSinPartidaActiva(String idGoogle) {
        boolean tienePartidaActiva = jugadorPartidaRepository
                .existsByJugador_IdGoogleAndPartida_EstadoInAndAbandonoFalse(
                        idGoogle, List.of(Partida.EstadoPartida.en_curso, Partida.EstadoPartida.esperando));
        if (tienePartidaActiva) {
            throw new GameLogicException("Ya estás en una partida activa.");
        }
    }

    private void finalizarPartidaManual(Partida partida, boolean rojoGana) {
        partida.setEstado(Partida.EstadoPartida.finalizada);
        partida.setFechaFin(LocalDateTime.now());
        partida.setRojoGana(rojoGana);
        partidaRepository.save(partida);

        List<JugadorPartida> todos = jugadorPartidaRepository.findByPartida_IdPartida(partida.getIdPartida());
        for (JugadorPartida jp : todos) {
            boolean esRojo = jp.getEquipo() == JugadorPartida.Equipo.rojo;
            boolean gano = (rojoGana && esRojo) || (!rojoGana && !esRojo);
            jugadorService.procesarFinPartida(jp.getJugador().getIdGoogle(), gano, jp.getNumAciertos(), jp.getNumFallos());
        }

        messagingTemplate.convertAndSend("/topic/partidas/" + partida.getIdPartida() + "/estado", "FINALIZADA");
    }

    private Jugador findJugador(String idGoogle) {
        return jugadorRepository.findById(idGoogle)
                .orElseThrow(() -> new NotFoundException("Jugador no encontrado."));
    }

    private LobbyStatusDTO buildLobbyStatusDTO(Partida partida, List<JugadorPartida> jugadores) {
        List<JugadorLobbyDTO> jugadoresDTO = jugadores.stream()
                .map(jp -> new JugadorLobbyDTO(
                        jp.getJugador().getTag(),
                        jp.getJugador().getFotoPerfil(),
                        jp.getEquipo().name()))
                .collect(Collectors.toList());

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
        dto.setHayMinimo(false);
        dto.setJugadores(jugadoresDTO);
        return dto;
    }
}
