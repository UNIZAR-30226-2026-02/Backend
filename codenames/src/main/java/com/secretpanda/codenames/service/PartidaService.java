package com.secretpanda.codenames.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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

    private final PartidaRepository partidaRepository;
    private final JugadorRepository jugadorRepository;
    private final TemaRepository temaRepository;
    private final JugadorPartidaRepository jugadorPartidaRepository;
    private final InventarioTemaRepository inventarioTemaRepository;
    private final TableroCartaRepository tableroCartaRepository;
    private final CodigoPartidaGenerator codigoGenerator;

    public PartidaService(PartidaRepository partidaRepository,
                          JugadorRepository jugadorRepository,
                          TemaRepository temaRepository,
                          JugadorPartidaRepository jugadorPartidaRepository,
                          InventarioTemaRepository inventarioTemaRepository,
                          TableroCartaRepository tableroCartaRepository,
                          CodigoPartidaGenerator codigoGenerator) {
        this.partidaRepository = partidaRepository;
        this.jugadorRepository = jugadorRepository;
        this.temaRepository = temaRepository;
        this.jugadorPartidaRepository = jugadorPartidaRepository;
        this.inventarioTemaRepository = inventarioTemaRepository;
        this.tableroCartaRepository = tableroCartaRepository;
        this.codigoGenerator = codigoGenerator;
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
    public void unirsePartidaPrivada(Integer idPartida, String codigoPartida, String idGoogle) {
        validarSinPartidaActiva(idGoogle);

        Partida partida = partidaRepository.findById(idPartida)
                .orElseThrow(() -> new NotFoundException("Partida no encontrada."));

        if (partida.isEsPublica()) {
            throw new GameLogicException("Esta partida es pública. Usa el endpoint de unirse a pública.");
        }
        if (!partida.getCodigoPartida().equalsIgnoreCase(codigoPartida)) {
            throw new BadRequestException("Código de partida incorrecto.");
        }

        unirseValidado(partida, idGoogle);
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
        Partida partida = partidaRepository.findById(idPartida)
                .orElseThrow(() -> new NotFoundException("Partida no encontrada."));

        JugadorPartida jp = jugadorPartidaRepository
                .findByJugador_IdGoogleAndPartida_IdPartida(idGoogle, idPartida)
                .orElseThrow(() -> new BadRequestException("No perteneces a esta partida."));

        if (Partida.EstadoPartida.en_curso.equals(partida.getEstado())) {
            // Penalizar: restar balas
            Jugador jugador = findJugador(idGoogle);
            jugador.setBalas(Math.max(0, jugador.getBalas() - 5));
            jugadorRepository.save(jugador);

            // Marcar abandono
            jp.setAbandono(true);
            jugadorPartidaRepository.save(jp);

            // Si era jefe de espías → derrota automática para su equipo (RF-27)
            if (JugadorPartida.Rol.lider.equals(jp.getRol())) {
                finalizarPorAbandonoLider(partida, jp.getEquipo());
            }

        } else {
            // En lobby: eliminar la fila
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
