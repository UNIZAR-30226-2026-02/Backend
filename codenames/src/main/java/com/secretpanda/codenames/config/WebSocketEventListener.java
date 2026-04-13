package com.secretpanda.codenames.config;

import com.secretpanda.codenames.model.Partida;
import com.secretpanda.codenames.repository.JugadorPartidaRepository;
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
    private JugadorPartidaRepository jugadorPartidaRepository;

    @Autowired
    private TaskScheduler taskScheduler;

    // Mapa para guardar las tareas de desconexión pendientes
    private final Map<String, ScheduledFuture<?>> disconnectTasks = new ConcurrentHashMap<>();

    /**
     * PASO 1: MANEJAR LA RECONEXIÓN
     * Si el usuario se vuelve a conectar antes de 30 segundos, cancelamos su abandono.
     */
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
        Principal principal = sha.getUser();
        if (principal == null) return;

        String idGoogle = principal.getName();

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
     * En lugar de abandonar de inmediato, iniciamos la cuenta atrás.
     */
    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
        Principal principal = sha.getUser();
        if (principal == null) return;
        
        String idGoogle = principal.getName();
        log.warn("Jugador [{}] perdió la conexión. Iniciando temporizador de 30s...", idGoogle);

        // Programamos la ejecución del abandono para dentro de 30 segundos
        ScheduledFuture<?> task = taskScheduler.schedule(
            () -> ejecutarAbandonoDefinitivo(idGoogle), 
            Instant.now().plusSeconds(30)
        );

        // Guardamos la tarea en el mapa usando su ID como llave
        disconnectTasks.put(idGoogle, task);
    }

    /**
     * PASO 3: LA LÓGICA DE ABANDONO DEFINITIVA (Solo se ejecuta si el timer de 30s expira)
     */
    private void ejecutarAbandonoDefinitivo(String idGoogle) {
        log.error("Tiempo expirado (30s) para jugador [{}]. Ejecutando abandono definitivo.", idGoogle);
        
        // Limpiamos el mapa por seguridad
        disconnectTasks.remove(idGoogle);

        // Buscamos las partidas en las que el jugador está activo (que no ha abandonado previamente)
        jugadorPartidaRepository.findByJugador_IdGoogleAndAbandonoFalse(idGoogle).forEach(jp -> {
                Partida partida = jp.getPartida();
                Integer idPartida = partida.getIdPartida();

                try {
                    // El usuario estaba en un LOBBY (Sala de espera)
                    if (Partida.EstadoPartida.esperando.equals(partida.getEstado())) {
                        lobbyService.abandonarLobby(idPartida, idGoogle);
                        log.info("Jugador [{}] expulsado del lobby [{}] correctamente.", idGoogle, idPartida);
                    } 
                    // El usuario estaba en plena PARTIDA
                    else if (Partida.EstadoPartida.en_curso.equals(partida.getEstado())) {
                        partidaService.abandonar(idPartida, idGoogle);
                        log.info("Jugador [{}] abandonó definitivamente la partida en curso [{}]. Se aplicaron penalizaciones.", idGoogle, idPartida);
                    }
                } catch (Exception e) {
                    log.error("Error al procesar el abandono definitivo del jugador [{}] en la partida [{}]: {}", 
                              idGoogle, idPartida, e.getMessage());
                }
            });
    }
}