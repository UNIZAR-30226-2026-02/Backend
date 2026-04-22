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

    // ─── Perfil ───────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public JugadorDTO getPerfil(String idGoogle) {
        Jugador jugador = jugadorRepository.findById(idGoogle)
                .orElseThrow(() -> new NotFoundException("Jugador no encontrado."));
        
        if (jugador.getInventario() != null) {
            jugador.getInventario().size();
        }

        JugadorDTO dto = JugadorMapper.toDTO(jugador, calculator);

        jugadorPartidaRepository.findFirstByJugador_IdGoogleAndPartida_EstadoInAndAbandonoFalse(
            idGoogle, 
            List.of(Partida.EstadoPartida.esperando, Partida.EstadoPartida.en_curso)
        ).ifPresent(jp -> dto.setPartidaActivaId(jp.getPartida().getIdPartida()));

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
        return JugadorMapper.toDTO(jugador, calculator);
    }

    // ─── Personalización e Inventario ─────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<PersonalizacionInventarioDTO> getPersonalizacionesAdquiridas(String idGoogle) {
        return PersonalizacionInventarioMapper.toDTOList(
                inventarioPersonalizacionRepository.findById_IdJugador(idGoogle));
    }

    @Transactional
    public void equiparItem(Integer idPersonalizacion, boolean equipado, String idGoogle) {
        InventarioPersonalizacion itemAEquipar = inventarioPersonalizacionRepository
                .findById_IdJugadorAndId_IdPersonalizacion(idGoogle, idPersonalizacion)
                .orElseThrow(() -> new NotFoundException("No posees este artículo."));

        if (equipado) {
            Personalizacion.TipoPersonalizacion tipo = itemAEquipar.getPersonalizacion().getTipo();
            inventarioPersonalizacionRepository
                .findById_IdJugadorAndPersonalizacion_TipoAndEquipadoTrue(idGoogle, tipo)
                .ifPresent(antiguo -> {
                    antiguo.setEquipado(false);
                    inventarioPersonalizacionRepository.save(antiguo);
                });
        }

        itemAEquipar.setEquipado(equipado);
        inventarioPersonalizacionRepository.save(itemAEquipar);

        PersonalizacionWS dto = new PersonalizacionWS(
            itemAEquipar.getPersonalizacion().getTipo().name(),
            itemAEquipar.getPersonalizacion().getValorVisual(),
            equipado
        );
    
        messagingTemplate.convertAndSendToUser(idGoogle, "/queue/personalizacion", dto);
    }

    // ─── Temas ────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<TemaInventarioDTO> getTemasAdquiridos(String idGoogle) {
        return TemaInventarioMapper.toDTOList(
                inventarioTemaRepository.findById_IdJugador(idGoogle));
    }

    // ─── Historial ────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<PartidaResumenDTO> getHistorial(String idGoogle) {
        List<JugadorPartida> participaciones = 
            jugadorPartidaRepository.findHistoryByJugadorId(idGoogle);

        return participaciones.stream()
                .limit(MAX_HISTORIAL)
                .map(jp -> PartidaMapper.toResumenDTO(jp.getPartida(), jp))
                .collect(Collectors.toList());
    }

    // ─── Logros ───────────────────────────────────────────────────────────────

    @Transactional
    public void inicializarLogros(Jugador jugador) {
        List<Logro> todosLosLogros = logroRepository.findByActivoTrue();
        for (Logro logro : todosLosLogros) {
            JugadorLogro jl = new JugadorLogro();
            JugadorLogroId id = new JugadorLogroId();
            id.setIdJugador(jugador.getIdGoogle());
            id.setIdLogro(logro.getIdLogro());
            jl.setId(id);
            jl.setJugador(jugador);
            jl.setLogro(logro);
            jl.setProgresoActual(0);
            jl.setCompletado(false);
            jugadorLogroRepository.save(jl);
        }
    }

    @Transactional(readOnly = true)
    public List<LogroDTO> getLogros(String idGoogle) {
        List<Logro> todosLosLogros = logroRepository.findByActivoTrue();
        List<JugadorLogro> progresosJugador = jugadorLogroRepository.findById_IdJugador(idGoogle);

        return todosLosLogros.stream().map(logroBase -> {
            LogroDTO dto = new LogroDTO();
            dto.setIdLogro(logroBase.getIdLogro());
            dto.setNombre(logroBase.getNombre());
            dto.setDescripcion(logroBase.getDescripcion());
            dto.setBalasRecompensa(logroBase.getBalasRecompensa());
            dto.setProgresoMax(logroBase.getValorObjetivo());
            dto.setEsLogro("logro".equalsIgnoreCase(logroBase.getTipo().name()));

            progresosJugador.stream()
                .filter(pj -> pj.getLogro().getIdLogro().equals(logroBase.getIdLogro()))
                .findFirst()
                .ifPresentOrElse(pj -> {
                    dto.setProgresoActual(pj.getProgresoActual());
                    dto.setCompletado(pj.isCompletado());
                }, () -> {
                    dto.setProgresoActual(0);
                    dto.setCompletado(false);
                });

            return dto;
        }).collect(Collectors.toList());
    }

    // ─── Balas y Notificaciones ───────────────────────────────────────────────
    
    @Value("${game.balas-ganador:20}")
    private int balasGanador;

    @Value("${game.balas-derrota:10}")
    private int balasDerrota;

    @Transactional
    public void procesarFinPartida(String idGoogle, boolean gano, int aciertos, int fallos) {
        Jugador j = jugadorRepository.findByIdForUpdate(idGoogle)
                .orElseThrow(() -> new NotFoundException("Jugador no encontrado."));

        j.setPartidasJugadas(j.getPartidasJugadas() + 1);
        if (gano) {
            j.setVictorias(j.getVictorias() + 1);
        }
        j.setNumAciertos(j.getNumAciertos() + aciertos);
        j.setNumFallos(j.getNumFallos() + fallos);

        int premio = gano ? balasGanador : balasDerrota;
        j.setBalas(j.getBalas() + premio);

        jugadorRepository.save(j);
        notificarActualizacionBalas(idGoogle, j.getBalas());

        if (fallos == 0) {
            jugadorLogroRepository.findById_IdJugador(idGoogle).stream()
                .filter(jl -> "partidas_sin_fallos".equals(jl.getLogro().getEstadisticaClave()))
                .findFirst()
                .ifPresent(jl -> {
                    jl.setProgresoActual(1);
                    jugadorLogroRepository.save(jl);
                });
        }

        actualizarProgresoLogros(idGoogle);
    }

    @Transactional
    public void modificarBalas(String idGoogle, int cantidadAModificar) {
        Jugador jugador = jugadorRepository.findByIdForUpdate(idGoogle)
                .orElseThrow(() -> new NotFoundException("Jugador no encontrado"));

        jugador.setBalas(jugador.getBalas() + cantidadAModificar);
        if (jugador.getBalas() < 0) jugador.setBalas(0); 

        jugadorRepository.save(jugador);
        notificarActualizacionBalas(idGoogle, jugador.getBalas());
    }

    @Transactional
    public void actualizarProgresoLogros(String idGoogle) {
        Jugador jugador = jugadorRepository.findById(idGoogle).orElseThrow();
        List<JugadorLogro> pendientes = jugadorLogroRepository.findById_IdJugadorAndCompletadoFalse(idGoogle);

        for (JugadorLogro jl : pendientes) {
            Logro logro = jl.getLogro();
            int valorActual = 0;

            switch (logro.getEstadisticaClave()) {
                case "partidas_jugadas" -> valorActual = jugador.getPartidasJugadas();
                case "victorias" -> valorActual = jugador.getVictorias();
                case "amigos_añadidos" -> {
                    long enviados = jugador.getAmistadesEnviadas().stream()
                        .filter(a -> com.secretpanda.codenames.model.Amistad.EstadoAmistad.aceptada.equals(a.getEstado())).count();
                    long recibidos = jugador.getAmistadesRecibidas().stream()
                        .filter(a -> com.secretpanda.codenames.model.Amistad.EstadoAmistad.aceptada.equals(a.getEstado())).count();
                    valorActual = (int) (enviados + recibidos);
                }
                case "partidas_sin_fallos" -> valorActual = jl.getProgresoActual();
                case "compras_tienda" -> {
                    int items = jugador.getInventario() != null ? jugador.getInventario().size() : 0;
                    int temas = jugador.getInventarioTemas() != null ? jugador.getInventarioTemas().size() : 0;
                    valorActual = items + temas;
                }
            }

            jl.setProgresoActual(valorActual);

            if (valorActual >= logro.getValorObjetivo()) {
                jl.setCompletado(true);
                jl.setFechaDesbloqueo(LocalDateTime.now());
                
                if ("logro".equalsIgnoreCase(logro.getTipo().name())) {
                    modificarBalas(idGoogle, 50); 
                }
                
                String mensajeNotif = "logro".equalsIgnoreCase(logro.getTipo().name()) 
                    ? "¡Logro desbloqueado: " + logro.getNombre() + "!"
                    : "¡Has ganado la medalla: " + logro.getNombre() + "!";
                
                NotificacionDTO notif = new NotificacionDTO("logro_desbloqueado", 
                    Map.of("mensaje", mensajeNotif, "recompensa", 50));
                messagingTemplate.convertAndSendToUser(idGoogle, "/queue/jugadores/notificaciones", notif);
            }
        }
        jugadorLogroRepository.saveAll(pendientes);
    }

    private Jugador findJugador(String idGoogle) {
        return jugadorRepository.findById(idGoogle)
                .orElseThrow(() -> new NotFoundException("Jugador no encontrado."));
    }

    public void notificarActualizacionBalas(String idGoogle, int nuevasBalas) {
        messagingTemplate.convertAndSendToUser(idGoogle, "/queue/balas", nuevasBalas);
    }
}
