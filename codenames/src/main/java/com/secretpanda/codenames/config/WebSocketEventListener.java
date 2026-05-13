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

    @Autowired
    private org.springframework.transaction.support.TransactionTemplate transactionTemplate;

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

    protected void ejecutarAbandonoDefinitivo(String idGoogle) {
        // Buscamos si el jugador aún existe en una partida activa (no abandonada)
        jugadorPartidaRepository.findFirstByJugador_IdGoogleAndPartida_EstadoInAndAbandonoFalse(
            idGoogle, List.of(Partida.EstadoPartida.esperando, Partida.EstadoPartida.en_curso)
        ).ifPresent(jp -> {
            Partida partida = jp.getPartida();
            Integer idPartida = partida.getIdPartida();

            log.info("Procesando abandono definitivo para jugador [{}] en partida [{}].", idGoogle, idPartida);
            
            try {
                if (Partida.EstadoPartida.esperando.equals(partida.getEstado())) {
                    lobbyService.abandonarLobby(idPartida, idGoogle, true);
                } else if (Partida.EstadoPartida.en_curso.equals(partida.getEstado())) {
                    partidaService.abandonar(idPartida, idGoogle, true);
                }
            } catch (Exception e) {
                log.warn("El abandono ya fue procesado o la partida cambió de estado para [{}]: {}", idGoogle, e.getMessage());
            }
        });
        
        // Siempre eliminamos la tarea del mapa
        disconnectTasks.remove(idGoogle);
    }
}