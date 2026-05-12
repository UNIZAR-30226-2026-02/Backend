package com.secretpanda.codenames.config;

import com.secretpanda.codenames.model.Partida;
import com.secretpanda.codenames.repository.JugadorPartidaRepository;
import com.secretpanda.codenames.repository.JugadorRepository;
import com.secretpanda.codenames.service.JugadorService;
import com.secretpanda.codenames.service.LobbyService;
import com.secretpanda.codenames.service.PartidaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@Slf4j
@Component
public class WebSocketEventListener {

    @Autowired
    private LobbyService lobbyService;

    @Autowired
    private PartidaService partidaService;
    
    @Autowired
    private JugadorService jugadorService;
    
    @Autowired
    private JugadorRepository jugadorRepository;

    @Autowired
    private JugadorPartidaRepository jugadorPartidaRepository;

    @Autowired
    private TaskScheduler taskScheduler;

    @org.springframework.beans.factory.annotation.Value("${game.timeout-reconexion:60}")
    private int timeoutReconexion;

    // Mapa para guardar las tareas de desconexión pendientes
    private final Map<String, ScheduledFuture<?>> disconnectTasks = new ConcurrentHashMap<>();

    /**
     * PASO 1: MANEJAR LA RECONEXIÓN
     * Si el usuario se vuelve a conectar antes del timeout, cancelamos su abandono.
     */
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
        Principal principal = sha.getUser();
        if (principal == null) return;

        String idGoogle = principal.getName();
        
        // Limpiamos la marca de desconexión en BDD
        jugadorService.actualizarDesconexion(idGoogle, null);

        // Intentamos sacar su temporizador del mapa. Si existe, lo cancelamos.
        ScheduledFuture<?> scheduledTask = disconnectTasks.remove(idGoogle);
        if (scheduledTask != null) {
            scheduledTask.cancel(false); // Cancelamos la expulsión inminente
            log.info("Jugador [{}] reconectado a tiempo. Temporizador de abandono cancelado.", idGoogle);
        } else {
            log.info("Jugador [{}] conectado con éxito.", idGoogle);
        }
    }

    /**
     * PASO 2: MANEJAR LA DESCONEXIÓN
     * Si está en partida, iniciamos cuenta atrás (gracia 60s).
     * Si no, limpieza inmediata.
     */
    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
        Principal principal = sha.getUser();
        
        if (principal == null) return;
        
        String idGoogle = principal.getName();

        // Comprobamos si el jugador está en una partida (en curso o esperando)
        boolean enPartida = jugadorPartidaRepository.findByJugador_IdGoogleAndAbandonoFalse(idGoogle).isPresent();

        if (enPartida) {
            log.warn("Jugador [{}] desconectado en partida. Iniciando tiempo de gracia de {}s...", idGoogle, timeoutReconexion);
            jugadorService.actualizarDesconexion(idGoogle, java.time.LocalDateTime.now());

            ScheduledFuture<?> task = taskScheduler.schedule(
                () -> ejecutarAbandonoDefinitivo(idGoogle), 
                Instant.now().plusSeconds(timeoutReconexion)
            );
            disconnectTasks.put(idGoogle, task);
        } else {
            // Fuera de partida: limpieza inmediata
            log.info("Jugador [{}] desconectado fuera de partida. Limpieza inmediata.", idGoogle);
            jugadorService.actualizarDesconexion(idGoogle, null);
        }
    }

    /**
     * PASO 3: LA LÓGICA DE ABANDONO DEFINITIVA
     * Solo se aplica si la partida está en curso.
     */
    private void ejecutarAbandonoDefinitivo(String idGoogle) {
        // Consultamos en BDD el estado actual del jugador
        jugadorRepository.findById(idGoogle).ifPresent(jugador -> {
            if (jugador.getDisconnectedAt() == null) {
                log.info("Jugador [{}] ya se reconectó. Abortando abandono definitivo.", idGoogle);
                return;
            }

            log.info("Tiempo expirado ({}s) para jugador [{}]. Verificando estado de partida...", timeoutReconexion, idGoogle);
            
            // Limpiamos el mapa por seguridad
            disconnectTasks.remove(idGoogle);

            // Buscamos las partidas en las que el jugador está activo
            jugadorPartidaRepository.findByJugador_IdGoogleAndAbandonoFalse(idGoogle).ifPresent(jp -> {
                Partida partida = jp.getPartida();
                Integer idPartida = partida.getIdPartida();

                try {
                    // FILTRO: Solo aplicamos la lógica de abandono si la partida está en curso
                    if (Partida.EstadoPartida.en_curso.equals(partida.getEstado())) {
                        partidaService.abandonar(idPartida, idGoogle);
                        log.info("Jugador [{}] abandonó definitivamente la partida en curso [{}].", idGoogle, idPartida);
                    } else {
                        log.info("Jugador [{}] está en Lobby [{}]. Timeout de abandono ignorado.", idGoogle, idPartida);
                    }
                } catch (Exception e) {
                    log.error("Error al procesar el abandono definitivo del jugador [{}]: {}", idGoogle, e.getMessage());
                }
            });
        });
    }
}