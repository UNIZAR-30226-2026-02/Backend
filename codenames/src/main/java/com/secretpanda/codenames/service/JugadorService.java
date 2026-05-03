package com.secretpanda.codenames.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.secretpanda.codenames.dto.jugador.ActualizarPerfilDTO;
import com.secretpanda.codenames.dto.jugador.JugadorDTO;
import com.secretpanda.codenames.dto.jugador.PersonalizacionInventarioDTO;
import com.secretpanda.codenames.dto.jugador.PersonalizacionWS;
import com.secretpanda.codenames.dto.jugador.TemaInventarioDTO;
import com.secretpanda.codenames.dto.partida.PartidaResumenDTO;
import com.secretpanda.codenames.dto.social.NotificacionDTO;
import com.secretpanda.codenames.dto.tienda.LogroDTO;
import com.secretpanda.codenames.exception.BadRequestException;
import com.secretpanda.codenames.exception.NotFoundException;
import com.secretpanda.codenames.mapper.jugador.JugadorMapper;
import com.secretpanda.codenames.mapper.jugador.PersonalizacionInventarioMapper;
import com.secretpanda.codenames.mapper.jugador.TemaInventarioMapper;
import com.secretpanda.codenames.mapper.partida.PartidaMapper;
import com.secretpanda.codenames.model.InventarioPersonalizacion;
import com.secretpanda.codenames.model.Jugador;
import com.secretpanda.codenames.model.JugadorLogro;
import com.secretpanda.codenames.model.JugadorLogroId;
import com.secretpanda.codenames.model.JugadorPartida;
import com.secretpanda.codenames.model.Logro;
import com.secretpanda.codenames.model.Partida;
import com.secretpanda.codenames.model.Personalizacion;
import com.secretpanda.codenames.repository.InventarioPersonalizacionRepository;
import com.secretpanda.codenames.repository.InventarioTemaRepository;
import com.secretpanda.codenames.repository.JugadorLogroRepository;
import com.secretpanda.codenames.repository.JugadorPartidaRepository;
import com.secretpanda.codenames.repository.JugadorRepository;
import com.secretpanda.codenames.repository.LogroRepository;
import com.secretpanda.codenames.util.EstadisticasCalculator;

@Service
public class JugadorService {

    private static final int MAX_HISTORIAL = 30;

    private final JugadorRepository jugadorRepository;
    private final InventarioTemaRepository inventarioTemaRepository;
    private final InventarioPersonalizacionRepository inventarioPersonalizacionRepository;
    private final JugadorPartidaRepository jugadorPartidaRepository;
    private final JugadorLogroRepository jugadorLogroRepository;
    private final EstadisticasCalculator calculator;
    private final LogroRepository logroRepository;
    private final SimpMessagingTemplate messagingTemplate;


    public JugadorService(JugadorRepository jugadorRepository,
                          InventarioTemaRepository inventarioTemaRepository,
                          InventarioPersonalizacionRepository inventarioPersonalizacionRepository,
                          JugadorPartidaRepository jugadorPartidaRepository,
                          JugadorLogroRepository jugadorLogroRepository,
                          EstadisticasCalculator calculator, 
                          LogroRepository logroRepository, 
                          SimpMessagingTemplate messagingTemplate) {
        this.jugadorRepository = jugadorRepository;
        this.inventarioTemaRepository = inventarioTemaRepository;
        this.inventarioPersonalizacionRepository = inventarioPersonalizacionRepository;
        this.jugadorPartidaRepository = jugadorPartidaRepository;
        this.jugadorLogroRepository = jugadorLogroRepository;
        this.calculator = calculator;
        this.logroRepository = logroRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @Transactional(readOnly = true)
    public JugadorDTO getPerfil(String idGoogle) {
        Jugador jugador = jugadorRepository.findById(idGoogle)
                .orElseThrow(() -> new NotFoundException("Jugador no encontrado."));
        if (jugador.getInventario() != null) jugador.getInventario().size();
        JugadorDTO dto = JugadorMapper.toDTO(jugador, calculator);
        jugadorPartidaRepository.findFirstByJugador_IdGoogleAndPartida_EstadoInAndAbandonoFalse(idGoogle, 
            List.of(Partida.EstadoPartida.esperando, Partida.EstadoPartida.en_curso))
        .ifPresent(jp -> dto.setPartidaActivaId(jp.getPartida().getIdPartida()));
        return dto;
    }

    @Transactional
    public JugadorDTO actualizarPerfil(ActualizarPerfilDTO dto, String idGoogle) {
        Jugador jugador = findJugador(idGoogle);
        if (dto.getTag() != null && !dto.getTag().equals(jugador.getTag())) {
            if (jugadorRepository.existsByTagAndActivoTrue(dto.getTag().trim())) {
                throw new BadRequestException("Ese nombre de usuario ya está en uso.");
            }
        }
        JugadorMapper.applyUpdateDTO(dto, jugador);
        jugadorRepository.save(jugador);
        notificarActualizacionPerfil(idGoogle);
        return JugadorMapper.toDTO(jugador, calculator);
    }

    @Transactional(readOnly = true)
    public List<PersonalizacionInventarioDTO> getPersonalizacionesAdquiridas(String idGoogle) {
        return PersonalizacionInventarioMapper.toDTOList(inventarioPersonalizacionRepository.findById_IdJugador(idGoogle));
    }

    @Transactional
    public void equiparItem(Integer idPersonalizacion, boolean equipado, String idGoogle) {
        InventarioPersonalizacion item = inventarioPersonalizacionRepository.findById_IdJugadorAndId_IdPersonalizacion(idGoogle, idPersonalizacion)
                .orElseThrow(() -> new NotFoundException("No posees este artículo."));
        if (equipado) {
            Personalizacion.TipoPersonalizacion tipo = item.getPersonalizacion().getTipo();
            inventarioPersonalizacionRepository.findById_IdJugadorAndPersonalizacion_TipoAndEquipadoTrue(idGoogle, tipo)
                .ifPresent(antiguo -> { antiguo.setEquipado(false); inventarioPersonalizacionRepository.save(antiguo); });
        }
        item.setEquipado(equipado);
        inventarioPersonalizacionRepository.save(item);
        PersonalizacionWS dto = new PersonalizacionWS(item.getPersonalizacion().getTipo().name(), item.getPersonalizacion().getValorVisual(), equipado);
        messagingTemplate.convertAndSendToUser(idGoogle, "/queue/personalizacion", dto);
        notificarActualizacionPerfil(idGoogle);
    }

    @Transactional(readOnly = true)
    public List<TemaInventarioDTO> getTemasAdquiridos(String idGoogle) {
        return TemaInventarioMapper.toDTOList(inventarioTemaRepository.findById_IdJugador(idGoogle));
    }

    @Transactional(readOnly = true)
    public List<PartidaResumenDTO> getHistorial(String idGoogle) {
        Jugador jugador = findJugador(idGoogle);
        if (!jugador.isActivo()) return List.of();
        
        // Obtenemos el historial completo del repositorio y limitamos al número de partidas jugadas actuales
        return jugadorPartidaRepository.findHistoryByJugadorId(idGoogle).stream()
                .limit(Math.min(MAX_HISTORIAL, jugador.getPartidasJugadas()))
                .map(jp -> PartidaMapper.toResumenDTO(jp.getPartida(), jp))
                .collect(Collectors.toList());
    }

    @Transactional
    public void inicializarLogros(Jugador jugador) {
        logroRepository.findByActivoTrue().forEach(logro -> {
            JugadorLogro jl = new JugadorLogro();
            JugadorLogroId id = new JugadorLogroId();
            id.setIdJugador(jugador.getIdGoogle());
            id.setIdLogro(logro.getIdLogro());
            jl.setId(id); jl.setJugador(jugador); jl.setLogro(logro); jl.setProgresoActual(0); jl.setCompletado(false);
            jugadorLogroRepository.save(jl);
        });
    }

    @Transactional(readOnly = true)
    public List<LogroDTO> getLogros(String idGoogle) {
        List<Logro> todos = logroRepository.findByActivoTrue();
        List<JugadorLogro> progresos = jugadorLogroRepository.findById_IdJugador(idGoogle);
        return todos.stream().map(logro -> {
            LogroDTO dto = new LogroDTO();
            dto.setIdLogro(logro.getIdLogro()); dto.setNombre(logro.getNombre()); dto.setDescripcion(logro.getDescripcion());
            dto.setBalasRecompensa(logro.getBalasRecompensa()); dto.setProgresoMax(logro.getValorObjetivo());
            dto.setEsLogro("logro".equalsIgnoreCase(logro.getTipo().name()));
            progresos.stream().filter(pj -> pj.getLogro().getIdLogro().equals(logro.getIdLogro())).findFirst()
                .ifPresentOrElse(pj -> { dto.setProgresoActual(pj.getProgresoActual()); dto.setCompletado(pj.isCompletado()); },
                                () -> { dto.setProgresoActual(0); dto.setCompletado(false); });
            return dto;
        }).collect(Collectors.toList());
    }

    @Value("${game.balas-ganador:20}") private int balasGanador;
    @Value("${game.balas-derrota:10}") private int balasDerrota;

    @Transactional
    public void procesarFinPartida(String idGoogle, boolean gano, int aciertos, int fallos) {
        Jugador j = jugadorRepository.findByIdForUpdate(idGoogle).orElseThrow(() -> new NotFoundException("Jugador no encontrado."));
        j.setPartidasJugadas(j.getPartidasJugadas() + 1);
        if (gano) j.setVictorias(j.getVictorias() + 1);
        j.setNumAciertos(j.getNumAciertos() + aciertos); j.setNumFallos(j.getNumFallos() + fallos);
        j.setBalas(j.getBalas() + (gano ? balasGanador : balasDerrota));
        jugadorRepository.save(j);
        notificarActualizacionPerfil(idGoogle);
        if (fallos == 0) {
            jugadorLogroRepository.findById_IdJugador(idGoogle).stream()
                .filter(jl -> "partidas_sin_fallos".equals(jl.getLogro().getEstadisticaClave())).findFirst()
                .ifPresent(jl -> { jl.setProgresoActual(1); jugadorLogroRepository.save(jl); });
        }
        actualizarProgresoLogros(idGoogle);
    }

    @Transactional
    public void modificarBalas(String idGoogle, int cantidad) {
        Jugador jugador = jugadorRepository.findByIdForUpdate(idGoogle).orElseThrow(() -> new NotFoundException("Jugador no encontrado"));
        jugador.setBalas(Math.max(0, jugador.getBalas() + cantidad));
        jugadorRepository.save(jugador);
        notificarActualizacionPerfil(idGoogle);
    }

    @Transactional
    public void actualizarProgresoLogros(String idGoogle) {
        Jugador jugador = jugadorRepository.findById(idGoogle).orElseThrow();
        List<JugadorLogro> pendientes = jugadorLogroRepository.findById_IdJugadorAndCompletadoFalse(idGoogle);
        for (JugadorLogro jl : pendientes) {
            int valorActual = 0;
            switch (jl.getLogro().getEstadisticaClave()) {
                case "partidas_jugadas" -> valorActual = jugador.getPartidasJugadas();
                case "victorias" -> valorActual = jugador.getVictorias();
                case "amigos_añadidos" -> {
                    long env = jugador.getAmistadesEnviadas().stream().filter(a -> com.secretpanda.codenames.model.Amistad.EstadoAmistad.aceptada.equals(a.getEstado())).count();
                    long rec = jugador.getAmistadesRecibidas().stream().filter(a -> com.secretpanda.codenames.model.Amistad.EstadoAmistad.aceptada.equals(a.getEstado())).count();
                    valorActual = (int) (env + rec);
                }
                case "partidas_sin_fallos" -> valorActual = jl.getProgresoActual();
                case "compras_tienda" -> valorActual = (jugador.getInventario() != null ? jugador.getInventario().size() : 0) + (jugador.getInventarioTemas() != null ? jugador.getInventarioTemas().size() : 0);
            }
            jl.setProgresoActual(valorActual);
            if (valorActual >= jl.getLogro().getValorObjetivo()) {
                jl.setCompletado(true); jl.setFechaDesbloqueo(LocalDateTime.now());
                if ("logro".equalsIgnoreCase(jl.getLogro().getTipo().name())) modificarBalas(idGoogle, 50);
                NotificacionDTO n = new NotificacionDTO("logro_desbloqueado", Map.of("mensaje", "logro".equalsIgnoreCase(jl.getLogro().getTipo().name()) ? "¡Logro desbloqueado: " + jl.getLogro().getNombre() + "!" : "¡Has ganado la medalla: " + jl.getLogro().getNombre() + "!", "recompensa", 50));
                messagingTemplate.convertAndSendToUser(idGoogle, "/queue/jugadores/notificaciones", n);
            }
        }
        jugadorLogroRepository.saveAll(pendientes);
    }

    private Jugador findJugador(String idGoogle) { return jugadorRepository.findById(idGoogle).orElseThrow(() -> new NotFoundException("Jugador no encontrado.")); }
    
    public void notificarActualizacionPerfil(String idGoogle) {
        try {
            JugadorDTO dto = getPerfil(idGoogle);
            messagingTemplate.convertAndSendToUser(idGoogle, "/queue/jugadores", dto);
        } catch (Exception e) {
            // Silently fail as notification is a non-critical side effect
        }
    }
}
