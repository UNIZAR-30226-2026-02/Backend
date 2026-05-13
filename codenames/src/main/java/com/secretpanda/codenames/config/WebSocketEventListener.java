package com.secretpanda.codenames.config;

import com.secretpanda.codenames.model.Partida;
import com.secretpanda.codenames.repository.JugadorPartidaRepository;
import com.secretpanda.codenames.service.LobbyService;
import com.secretpanda.codenames.service.PartidaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;
import java.time.Instant;
import java.util.Map;
import java.util.List;
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
    private JugadorPartidaRepository jugadorPartidaRepository;

    @Autowired
    @Qualifier("webSocketTaskScheduler")
    private TaskScheduler taskScheduler;

    @org.springframework.beans.factory.annotation.Value("${game.timeout-reconexion:60}")
    private int timeoutReconexion;

    // Mapa para rastrear tareas de abandono: idGoogle -> ScheduledFuture
    private final Map<String, ScheduledFuture<?>> disconnectTasks = new ConcurrentHashMap<>();
    
    // Mapa para contar sesiones activas: idGoogle -> cantidad de sesiones (pestañas)
    private final Map<String, Integer> userSessions = new ConcurrentHashMap<>();

    @Autowired
    private org.springframework.transaction.support.TransactionTemplate transactionTemplate;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
        Principal principal = sha.getUser();
        if (principal == null) return;

        String idGoogle = principal.getName();
        
        // Incrementar contador de sesiones
        userSessions.merge(idGoogle, 1, Integer::sum);
        int activeSessions = userSessions.get(idGoogle);

        log.info("Jugador [{}] conectado. Sesiones activas: {}", idGoogle, activeSessions);

        ScheduledFuture<?> scheduledTask = disconnectTasks.remove(idGoogle);
        if (scheduledTask != null) {
            scheduledTask.cancel(false);
            log.info("Jugador [{}] reconectado. Temporizador de abandono cancelado.", idGoogle);
        }
    }

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
        Principal principal = sha.getUser();
        
        if (principal == null) {
            return;
        }
        
        String idGoogle = principal.getName();
        
        // Decrementar contador de sesiones
        Integer currentSessions = userSessions.computeIfPresent(idGoogle, (k, v) -> v > 1 ? v - 1 : null);
        int remainingSessions = (currentSessions == null) ? 0 : currentSessions;

        log.info("Intento de desconexión. Jugador [{}], ID sesión: {}. Sesiones restantes: {}", 
                 idGoogle, sha.getSessionId(), remainingSessions);

        // Solo iniciamos el temporizador de abandono si no quedan sesiones activas
        if (remainingSessions == 0) {
            log.warn("Jugador [{}] perdió todas las conexiones. Iniciando temporizador de {}s...", idGoogle, timeoutReconexion);

            // IMPORTANTE: Si ya había una tarea (por un bug previo o rapidez), la cancelamos antes de poner la nueva
            ScheduledFuture<?> oldTask = disconnectTasks.remove(idGoogle);
            if (oldTask != null) {
                oldTask.cancel(false);
            }

            ScheduledFuture<?> task = taskScheduler.schedule(
                () -> ejecutarAbandonoDefinitivo(idGoogle), 
                Instant.now().plusSeconds(timeoutReconexion)
            );

            disconnectTasks.put(idGoogle, task);
        } else {
            log.info("Jugador [{}] cerró una sesión pero aún tiene {} activas. No se inicia temporizador.", idGoogle, remainingSessions);
        }
    }

    protected void ejecutarAbandonoDefinitivo(String idGoogle) {
        // Doble comprobación: si el usuario se reconectó justo antes de dispararse la tarea
        if (userSessions.containsKey(idGoogle)) {
            log.info("Cancelando abandono definitivo de [{}]: el usuario tiene sesiones activas.", idGoogle);
            disconnectTasks.remove(idGoogle);
            return;
        }

        transactionTemplate.execute(status -> {
            log.error("Tiempo expirado ({}s) para jugador [{}]. Ejecutando abandono definitivo.", timeoutReconexion, idGoogle);
            
            disconnectTasks.remove(idGoogle);

            jugadorPartidaRepository.findFirstByJugador_IdGoogleAndPartida_EstadoInAndAbandonoFalse(
                idGoogle, List.of(Partida.EstadoPartida.esperando, Partida.EstadoPartida.en_curso)
            ).ifPresent(jp -> {
                    Partida partida = jp.getPartida();
                    Integer idPartida = partida.getIdPartida();

                    try {
                        if (Partida.EstadoPartida.esperando.equals(partida.getEstado())) {
                            lobbyService.abandonarLobby(idPartida, idGoogle, true);
                            log.info("Jugador [{}] expulsado del lobby [{}] por inactividad.", idGoogle, idPartida);
                        } 
                        else if (Partida.EstadoPartida.en_curso.equals(partida.getEstado())) {
                            partidaService.abandonar(idPartida, idGoogle, true);
                            log.info("Jugador [{}] abandonó definitivamente por inactividad la partida [{}].", idGoogle, idPartida);
                        }
                    } catch (Exception e) {
                        log.error("Error al procesar el abandono de [{}] en partida [{}]: {}", 
                                  idGoogle, idPartida, e.getMessage());
                    }
                });
            return null;
        });
    }
}