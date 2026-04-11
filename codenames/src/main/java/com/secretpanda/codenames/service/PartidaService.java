package com.secretpanda.codenames.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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

    private static final int BALAS_PENALIZACION_ABANDONO = 10;

    private final PartidaRepository partidaRepository;
    private final JugadorRepository jugadorRepository;
    private final TemaRepository temaRepository;
    private final JugadorPartidaRepository jugadorPartidaRepository;
    private final InventarioTemaRepository inventarioTemaRepository;
    private final TableroCartaRepository tableroCartaRepository;
    private final CodigoPartidaGenerator codigoGenerator;
    private final SimpMessagingTemplate messagingTemplate;
    private final JugadorService jugadorService;

    public PartidaService(PartidaRepository partidaRepository,
                          JugadorRepository jugadorRepository,
                          TemaRepository temaRepository,
                          JugadorPartidaRepository jugadorPartidaRepository,
                          InventarioTemaRepository inventarioTemaRepository,
                          TableroCartaRepository tableroCartaRepository,
                          CodigoPartidaGenerator codigoGenerator,
                          SimpMessagingTemplate messagingTemplate,
                          JugadorService jugadorService) {
        this.partidaRepository = partidaRepository;
        this.jugadorRepository = jugadorRepository;
        this.temaRepository = temaRepository;
        this.jugadorPartidaRepository = jugadorPartidaRepository;
        this.inventarioTemaRepository = inventarioTemaRepository;
        this.tableroCartaRepository = tableroCartaRepository;
        this.codigoGenerator = codigoGenerator;
        this.messagingTemplate = messagingTemplate;
        this.jugadorService = jugadorService;
    }

    // ─── Crear partida ─────────────────────────────────────────────────────────

    @Transactional
    public LobbyStatusDTO crearPartida(CrearPartidaDTO dto, String idGoogle) {
        // 1 partida activa por jugador
        validarSinPartidaActiva(idGoogle);

        Tema tema = temaRepository.findById(dto.getIdTema())
                .orElseThrow(() -> new NotFoundException("El tema seleccionado no existe."));
        Jugador creador = findJugador(idGoogle);

        // Validar que el creador tiene ese tema adquirido
        if (!inventarioTemaRepository.existsById_IdJugadorAndId_IdTema(idGoogle, dto.getIdTema())) {
            throw new GameLogicException("No tienes adquirido ese tema de cartas.");
        }

        // Generar código único
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

        // El creador entra al lobby en equipo rojo por defecto
        JugadorPartida jp = new JugadorPartida();
        jp.setJugador(creador);
        jp.setPartida(partida);
        jp.setEquipo(JugadorPartida.Equipo.rojo);
        jp.setRol(JugadorPartida.Rol.agente); // El rol real se asigna al iniciar
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

        // Comprobar que el jugador tiene el tema de la partida
        if (!inventarioTemaRepository.existsById_IdJugadorAndId_IdTema(
                idGoogle, partida.getTema().getIdTema())) {
            throw new GameLogicException("No tienes adquirido el tema de esta partida.");
        }

        unirseValidado(partida, idGoogle);
    }

    // ─── Abandonar (lobby o en curso) ──────────────────────────────────────────

    @Transactional
    public void abandonar(Integer idPartida, String idGoogle) {
        // 1. Bloqueamos la fila de la partida para evitar que dos abandonos simultáneos corrompan el estado
        Partida partida = partidaRepository.findByIdForUpdate(idPartida)
                .orElseThrow(() -> new NotFoundException("Partida no encontrada."));

        JugadorPartida jp = jugadorPartidaRepository
                .findByJugador_IdGoogleAndPartida_IdPartida(idGoogle, idPartida)
                .orElseThrow(() -> new BadRequestException("No perteneces a esta partida."));

        // CASO A: LA PARTIDA ESTÁ EN JUEGO
        if (Partida.EstadoPartida.en_curso.equals(partida.getEstado())) {
            jugadorService.modificarBalas(idGoogle, -10);

            // Marcar que el jugador ha abandonado
            jp.setAbandono(true);
            jugadorPartidaRepository.save(jp);

            // Chequeo de fin de partida por falta de personal operativo
            // Obtenemos todos los que NO han abandonado en esta partida
            List<JugadorPartida> activos = jugadorPartidaRepository
                    .findByPartida_IdPartidaAndAbandonoFalse(idPartida);
            
            JugadorPartida.Equipo miEquipo = jp.getEquipo();
            
            // Contamos cuántos Líderes y Agentes quedan en el equipo del que se acaba de ir
            long lideresEnMiEquipo = activos.stream()
                    .filter(a -> a.getEquipo().equals(miEquipo) && a.getRol().equals(JugadorPartida.Rol.lider))
                    .count();
            long agentesEnMiEquipo = activos.stream()
                    .filter(a -> a.getEquipo().equals(miEquipo) && a.getRol().equals(JugadorPartida.Rol.agente))
                    .count();

            // Si mi equipo se queda sin Líder (RF-27) O sin Agentes (nadie para votar)
            if (lideresEnMiEquipo == 0 || agentesEnMiEquipo == 0) {
                // El equipo rival gana automáticamente
                boolean ganaRojo = !miEquipo.equals(JugadorPartida.Equipo.rojo);
                finalizarPartidaManual(partida, ganaRojo);
            }
            // Si aún quedan jugadores suficientes, la partida continúa normalmente.

        } 
        // CASO B: LA PARTIDA ESTÁ EN EL LOBBY
        else {
            boolean esCreador = partida.getCreador().getIdGoogle().equals(idGoogle);
            if (esCreador) {
                // Si el creador se va del lobby, la partida se cancela para todos
                partida.setEstado(Partida.EstadoPartida.finalizada);
                partida.setFechaFin(LocalDateTime.now());
                partidaRepository.save(partida);
            }
            // Eliminamos físicamente el registro del jugador del lobby
            jugadorPartidaRepository.delete(jp);
        }
    }

    // ─── Rol del jugador ───────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public RolPartidaDTO getRolJugador(Integer idPartida, String idGoogle) {
        JugadorPartida jp = jugadorPartidaRepository
                .findByJugador_IdGoogleAndPartida_IdPartida(idGoogle, idPartida)
                .orElseThrow(() -> new NotFoundException("No perteneces a esta partida."));

        // El equipo inicial es el que tiene más cartas (rojo normalmente empieza con 9 vs 8)
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
        // Balancear: ir al equipo con menos jugadores
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
                .findByJugador_IdGoogle(idGoogle).stream()
                .anyMatch(jp -> !jp.isAbandono() &&
                        (Partida.EstadoPartida.en_curso.equals(jp.getPartida().getEstado()) ||
                         Partida.EstadoPartida.esperando.equals(jp.getPartida().getEstado())));
        if (tienePartidaActiva) {
            throw new GameLogicException("Ya estás en una partida activa.");
        }
    }

    private void finalizarPorAbandonoLider(Partida partida, JugadorPartida.Equipo equipoPerdedor) {
        partida.setEstado(Partida.EstadoPartida.finalizada);
        partida.setFechaFin(LocalDateTime.now());
        partida.setRojoGana(!JugadorPartida.Equipo.rojo.equals(equipoPerdedor));
        partidaRepository.save(partida);
    }

    private void finalizarPartidaManual(Partida partida, boolean rojoGana) {
        partida.setEstado(Partida.EstadoPartida.finalizada);
        partida.setFechaFin(LocalDateTime.now());
        partida.setRojoGana(rojoGana);
        partidaRepository.save(partida);

        // Notificar al canal de la partida que ha terminado
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
