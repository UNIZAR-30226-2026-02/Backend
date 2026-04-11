package com.secretpanda.codenames.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.secretpanda.codenames.dto.jugador.ActualizarPerfilDTO;
import com.secretpanda.codenames.dto.jugador.JugadorDTO;
import com.secretpanda.codenames.dto.jugador.PersonalizacionInventarioDTO;
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
import com.secretpanda.codenames.model.JugadorPartida;
import com.secretpanda.codenames.model.Logro;
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
        Jugador jugador = findJugador(idGoogle);
        return JugadorMapper.toDTO(jugador, calculator);
    }

    @Transactional
    public JugadorDTO actualizarPerfil(ActualizarPerfilDTO dto, String idGoogle) {
        Jugador jugador = findJugador(idGoogle);

        // Validar unicidad del nuevo tag (si lo está cambiando)
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

    /**
     * Gestiona qué aspecto estético tiene activo el jugador.
     * Si se equipa un nuevo tablero o carta, se desequipa automáticamente el anterior del mismo tipo.
     */
    @Transactional
    public void equiparItem(Integer idPersonalizacion, boolean equipado, String idGoogle) {
        // Lógica de base de datos (Ya la tienes, pero asegúrate de que sea así)
        InventarioPersonalizacion itemAEquipar = inventarioPersonalizacionRepository
                .findById_IdJugadorAndId_IdPersonalizacion(idGoogle, idPersonalizacion)
                .orElseThrow(() -> new NotFoundException("No posees este artículo."));

        if (equipado) {
            // Desequipar el anterior del mismo tipo
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

        // NOTIFICACIÓN EN DIRECTO (WS)
        // Enviamos al usuario un objeto con su nueva configuración visual
        Map<String, String> nuevaConfig = Map.of(
            "tipo", itemAEquipar.getPersonalizacion().getTipo().name(),
            "valor", itemAEquipar.getPersonalizacion().getValorVisual(),
            "equipado", String.valueOf(equipado)
        );
        
        messagingTemplate.convertAndSendToUser(idGoogle, "/queue/personalizacion", nuevaConfig);
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
        // Buscamos las participaciones ordenadas por fecha descendente
        List<JugadorPartida> participaciones = 
            jugadorPartidaRepository.findByJugador_IdGoogleOrderByPartida_FechaFinDesc(idGoogle);

        // Limitamos a las últimas 30 y mapeamos
        return participaciones.stream()
                .limit(MAX_HISTORIAL)
                .map(jp -> PartidaMapper.toResumenDTO(jp.getPartida(), jp))
                .collect(Collectors.toList());
    }

    // ─── Logros ───────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<LogroDTO> getLogros(String idGoogle) {
        // Obtener todos los logros que están activos en el juego (Catálogo)
        List<Logro> todosLosLogros = logroRepository.findByActivoTrue();
        
        // Obtener solo el progreso específico de este jugador
        List<JugadorLogro> progresosJugador = jugadorLogroRepository.findById_IdJugador(idGoogle);

        // xCruzamos los datos para construir la respuesta de la API
        return todosLosLogros.stream().map(logroBase -> {
            LogroDTO dto = new LogroDTO();
            
            // Datos estáticos del logro
            dto.setIdLogro(logroBase.getIdLogro());
            dto.setNombre(logroBase.getNombre());
            dto.setDescripcion(logroBase.getDescripcion());
            dto.setBalasRecompensa(logroBase.getBalasRecompensa());
            dto.setProgresoMax(logroBase.getValorObjetivo());
            
            // Lógica es_logro: true si el tipo es 'logro', false si es 'medalla'
            dto.setEsLogro("logro".equalsIgnoreCase(logroBase.getTipo().name()));

            // Buscamos si hay progreso registrado
            progresosJugador.stream()
                .filter(pj -> pj.getLogro().getIdLogro().equals(logroBase.getIdLogro()))
                .findFirst()
                .ifPresentOrElse(pj -> {
                    // Si hay progreso en la BD, lo ponemos
                    dto.setProgresoActual(pj.getProgresoActual());
                    dto.setCompletado(pj.isCompletado());
                }, () -> {
                    // Si no hay progreso registrado aún, devolvemos valores por defecto
                    dto.setProgresoActual(0);
                    dto.setCompletado(false);
                });

            return dto;
        }).collect(Collectors.toList());
    }

    // ─── Balas y Notificaciones ───────────────────────────────────────────────
    @Transactional
    public void modificarBalas(String idGoogle, int cantidadAModificar) {
        // Buscamos con bloqueo para que nadie más toque este jugador hasta que terminemos
        Jugador jugador = jugadorRepository.findByIdForUpdate(idGoogle)
                .orElseThrow(() -> new NotFoundException("Jugador no encontrado"));

        jugador.setBalas(jugador.getBalas() + cantidadAModificar);
        if (jugador.getBalas() < 0) jugador.setBalas(0); // Seguro contra negativos

        jugadorRepository.save(jugador);
        
        // Notificamos por WebSocket inmediatamente
        notificarActualizacionBalas(idGoogle, jugador.getBalas());
    }

    // ─── Progreso de Logros ───────────────────────────────────────────────────────
    @Transactional
    public void actualizarProgresoLogros(String idGoogle) {
        Jugador jugador = jugadorRepository.findById(idGoogle).orElseThrow();
        // Traemos solo los logros activos que el jugador aún no ha completado
        List<JugadorLogro> pendientes = jugadorLogroRepository.findById_IdJugadorAndCompletadoFalse(idGoogle);

        for (JugadorLogro jl : pendientes) {
            Logro logro = jl.getLogro();
            int valorActual = 0;

            // Mapeo dinámico de la estadística clave definida en BD
            switch (logro.getEstadisticaClave()) {
                case "partidas_jugadas" -> valorActual = jugador.getPartidasJugadas();
                case "victorias" -> valorActual = jugador.getVictorias();
                case "num_aciertos" -> valorActual = jugador.getNumAciertos();
                case "num_fallos" -> valorActual = jugador.getNumFallos();
            }

            jl.setProgresoActual(valorActual);

            if (valorActual >= logro.getValorObjetivo()) {
                jl.setCompletado(true);
                jl.setFechaDesbloqueo(LocalDateTime.now());
                
                // Entregar recompensa usando el método seguro del Punto 1
                modificarBalas(idGoogle, logro.getBalasRecompensa());
                
                // Notificar desbloqueo por WebSocket
                NotificacionDTO notif = new NotificacionDTO("logro_desbloqueado", 
                    Map.of("mensaje", "¡Logro desbloqueado: " + logro.getNombre() + "!", 
                        "recompensa", logro.getBalasRecompensa()));
                messagingTemplate.convertAndSendToUser(idGoogle, "/queue/jugadores/notificaciones", notif);
            }
        }
        jugadorLogroRepository.saveAll(pendientes);
    }

    // ─── Helper ───────────────────────────────────────────────────────────────

    private Jugador findJugador(String idGoogle) {
        return jugadorRepository.findById(idGoogle)
                .orElseThrow(() -> new NotFoundException("Jugador no encontrado."));
    }

    public void notificarActualizacionBalas(String idGoogle, int nuevasBalas) {
        // Enviamos el nuevo saldo a la cola privada del usuario
        // El frontend escuchará en /user/queue/balas
        messagingTemplate.convertAndSendToUser(idGoogle, "/queue/balas", nuevasBalas);
    }
}
