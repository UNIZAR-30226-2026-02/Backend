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

    private final Map<String, ScheduledFuture<?>> disconnectTasks = new ConcurrentHashMap<>();

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
        Principal principal = sha.getUser();
        if (principal == null) return;

        String idGoogle = principal.getName();

        ScheduledFuture<?> scheduledTask = disconnectTasks.remove(idGoogle);
        if (scheduledTask != null) {
            scheduledTask.cancel(false);
            log.info("Jugador [{}] reconectado a tiempo. Temporizador de abandono cancelado.", idGoogle);
        } else {
            log.info("Jugador [{}] conectado con éxito.", idGoogle);
        }
    }

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
        Principal principal = sha.getUser();
        
        log.info("Intento de desconexión. ID de sesión: {}, Principal: {}", 
                 sha.getSessionId(), (principal != null ? principal.getName() : "NULL"));

        if (principal == null) {
            log.warn("Desconexión detectada pero el Principal es NULL.");
            return;
        }
        
        String idGoogle = principal.getName();
        log.warn("Jugador [{}] perdió la conexión. Iniciando temporizador de {}s...", idGoogle, timeoutReconexion);

        ScheduledFuture<?> task = taskScheduler.schedule(
            () -> ejecutarAbandonoDefinitivo(idGoogle), 
            Instant.now().plusSeconds(timeoutReconexion)
        );

        disconnectTasks.put(idGoogle, task);
    }

    private void ejecutarAbandonoDefinitivo(String idGoogle) {
        log.error("Tiempo expirado ({}s) para jugador [{}]. Ejecutando abandono definitivo.", timeoutReconexion, idGoogle);
        
        disconnectTasks.remove(idGoogle);

        jugadorPartidaRepository.findFirstByJugador_IdGoogleAndPartida_EstadoInAndAbandonoFalse(idGoogle, List.of(Partida.EstadoPartida.esperando, Partida.EstadoPartida.en_curso)).ifPresent(jp -> {
                Partida partida = jp.getPartida();
                Integer idPartida = partida.getIdPartida();

                try {
                    if (Partida.EstadoPartida.esperando.equals(partida.getEstado())) {
                        lobbyService.abandonarLobby(idPartida, idGoogle, true);
                        log.info("Jugador [{}] expulsado del lobby [{}] correctamente.", idGoogle, idPartida);
                    } 
                    else if (Partida.EstadoPartida.en_curso.equals(partida.getEstado())) {
                        partidaService.abandonar(idPartida, idGoogle, true);
                        log.info("Jugador [{}] abandonó definitivamente la partida en curso [{}].", idGoogle, idPartida);
                    }
                } catch (Exception e) {
                    log.error("Error al procesar el abandono definitivo del jugador [{}] en la partida [{}]: {}", 
                              idGoogle, idPartida, e.getMessage());
                }
            });
    }
}