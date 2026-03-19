package com.secretpanda.codenames.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.secretpanda.codenames.dto.partida.CrearPartidaDTO;
import com.secretpanda.codenames.dto.partida.JugadorPartidaDTO;
import com.secretpanda.codenames.dto.partida.LobbyStatusDTO;
import com.secretpanda.codenames.dto.partida.UnirsePartidaDTO;
import com.secretpanda.codenames.exception.BadRequestException;
import com.secretpanda.codenames.exception.GameLogicException;
import com.secretpanda.codenames.exception.NotFoundException;
import com.secretpanda.codenames.mapper.partida.JugadorPartidaMapper;
import com.secretpanda.codenames.mapper.partida.PartidaMapper;
import com.secretpanda.codenames.model.Jugador;
import com.secretpanda.codenames.model.JugadorPartida;
import com.secretpanda.codenames.model.Partida;
import com.secretpanda.codenames.model.Tema;
import com.secretpanda.codenames.repository.JugadorPartidaRepository;
import com.secretpanda.codenames.repository.JugadorRepository;
import com.secretpanda.codenames.repository.PartidaRepository;
import com.secretpanda.codenames.repository.TemaRepository;
import com.secretpanda.codenames.util.CodigoPartidaGenerator;

@Service
public class PartidaService {

    private final PartidaRepository partidaRepository;
    private final JugadorRepository jugadorRepository;
    private final TemaRepository temaRepository;
    private final JugadorPartidaRepository jugadorPartidaRepository;
    private final CodigoPartidaGenerator codigoGenerator;

    public PartidaService(PartidaRepository partidaRepository, JugadorRepository jugadorRepository, 
                          TemaRepository temaRepository, JugadorPartidaRepository jugadorPartidaRepository,
                          CodigoPartidaGenerator codigoGenerator) {
        this.partidaRepository = partidaRepository;
        this.jugadorRepository = jugadorRepository;
        this.temaRepository = temaRepository;
        this.jugadorPartidaRepository = jugadorPartidaRepository;
        this.codigoGenerator = codigoGenerator;
    }

    /**
     * Endpoint: POST /partidas (RF-12)
     */
    @Transactional
    public LobbyStatusDTO crearPartida(CrearPartidaDTO dto, String idGoogleCreador) {
        Tema tema = temaRepository.findById(dto.getIdTema())
                .orElseThrow(() -> new NotFoundException("El tema seleccionado no existe."));
        
        Jugador creador = jugadorRepository.findById(idGoogleCreador)
                .orElseThrow(() -> new NotFoundException("Jugador no encontrado."));

        Partida partida = new Partida();
        partida.setTema(tema);
        partida.setCreador(creador);
        partida.setTiempoEspera(dto.getTiempoEspera());
        partida.setMaxJugadores(dto.getMaxJugadores());
        partida.setEsPublica(dto.isEsPublica());
        partida.setEstado(Partida.EstadoPartida.esperando);

        // Generamos el código seguro y legible de 6 caracteres sin ambigüedades
        String codigo;
        do {
            codigo = codigoGenerator.generarCodigo();
        } while (partidaRepository.existsByCodigoPartida(codigo));
        partida.setCodigoPartida(codigo);

        partida = partidaRepository.save(partida);

        // Añadimos automáticamente al creador a la tabla JUGADOR_PARTIDA
        JugadorPartida jp = new JugadorPartida();
        jp.setJugador(creador);
        jp.setPartida(partida);
        // Asignación temporal por defecto, el matchmaking final (RF-18) los redistribuirá
        boolean moneda = new java.util.Random().nextBoolean();
        String miEquipo = moneda ? "rojo" : "azul";

        if (moneda) {
            jp.setEquipo(JugadorPartida.Equipo.rojo);
        } else {
            jp.setEquipo(JugadorPartida.Equipo.azul); 
        }
        boolean monedaRol = new java.util.Random().nextBoolean();
        // (Ajusta esto según cómo se llame tu Enum de roles, igual que hicimos con Equipo)
        if (monedaRol) {
            jp.setRol(JugadorPartida.Rol.lider);
        } else {
            jp.setRol(JugadorPartida.Rol.agente);
        }
        jp.setRol(JugadorPartida.Rol.lider); 
        jugadorPartidaRepository.save(jp);

        partida.getJugadores().add(jp);
        return PartidaMapper.toLobbyStatusDTO(partida, partida.getJugadores());
    }

    /**
     * Endpoint: GET /partidas/publicas (RF-19 Matchmaking)
     */
    @Transactional(readOnly = true)
    public List<LobbyStatusDTO> listarPartidasPublicasDisponibles() {
        // Obtenemos solo las partidas públicas, en estado esperando y con huecos libres
        List<Partida> partidas = partidaRepository.findPartidasPublicasDisponibles(Partida.EstadoPartida.esperando);

        return partidas.stream()
                .map(p -> PartidaMapper.toLobbyStatusDTO(p, p.getJugadores()))
                .collect(Collectors.toList());
    }

    /**
     * Endpoint: POST /partidas/{id_partida}/unirse
     */
    @Transactional
    public JugadorPartidaDTO unirsePartida(Integer idPartida, UnirsePartidaDTO dto, String idGoogleJugador) {
        Partida partida = partidaRepository.findById(idPartida)
                .orElseThrow(() -> new NotFoundException("Partida no encontrada."));

        // Validar si la partida ya ha empezado
        if (!Partida.EstadoPartida.esperando.equals(partida.getEstado())) {
            throw new GameLogicException("La partida ya ha comenzado o finalizado.");
        }

        // Validar código si es privada
        if (!partida.isEsPublica()) {
            if (dto.getCodigoPartida() == null || !partida.getCodigoPartida().equalsIgnoreCase(dto.getCodigoPartida())) {
                throw new BadRequestException("Código de partida incorrecto.");
            }
        }

        // Validar si ya está llena
        long jugadoresActuales = jugadorPartidaRepository.countByPartida_IdPartidaAndAbandonoFalse(idPartida);
        if (jugadoresActuales >= partida.getMaxJugadores()) {
            throw new GameLogicException("La partida ya está llena.");
        }

        // Validar si el jugador ya está dentro de esta partida
        if (jugadorPartidaRepository.existsByJugador_IdGoogleAndPartida_IdPartida(idGoogleJugador, idPartida)) {
            throw new GameLogicException("Ya estás dentro de esta partida.");
        }

        Jugador jugador = jugadorRepository.findById(idGoogleJugador)
                .orElseThrow(() -> new NotFoundException("Jugador no encontrado."));

        JugadorPartida jp = new JugadorPartida();
        jp.setJugador(jugador);
        jp.setPartida(partida);
        boolean moneda = new java.util.Random().nextBoolean();
        String miEquipo = moneda ? "rojo" : "azul";

        if (moneda) {
            jp.setEquipo(JugadorPartida.Equipo.rojo);
        } else {
            jp.setEquipo(JugadorPartida.Equipo.azul); 
        }
        // Asignación por defecto (Rojo/Agente). Deberá ajustarse según el balanceo.
        boolean jefeYaExisteEnMiEquipo = false;

        // Revisamos a todos los jugadores que YA están en la partida
        for (JugadorPartida existente : partida.getJugadores()) {
            if (existente.getEquipo().name().equalsIgnoreCase(miEquipo) 
                && existente.getRol().name().equalsIgnoreCase("lider")) {
                jefeYaExisteEnMiEquipo = true;
                break; // ¡Ya hemos encontrado al jefe, dejamos de buscar!
            }
        }

        // Asignamos el rol al nuevo jugador
        if (jefeYaExisteEnMiEquipo) {
            // Si ya hay jefe, le toca ser agente obligatoriamente
            jp.setRol(JugadorPartida.Rol.agente);
        } else {
            // Si la plaza está libre, tiramos la moneda a ver si tiene suerte
            boolean suerte = new java.util.Random().nextBoolean();
            jp.setRol(suerte ? JugadorPartida.Rol.lider : JugadorPartida.Rol.agente);
        }

        jp = jugadorPartidaRepository.save(jp);

        return JugadorPartidaMapper.toDTO(jp);
    }
}