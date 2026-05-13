package com.secretpanda.codenames.config;

import com.secretpanda.codenames.model.Partida;
import com.secretpanda.codenames.model.JugadorPartida;
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
    private com.secretpanda.codenames.service.AbandonedPlayerCleaner abandonedPlayerCleaner;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
        Principal principal = sha.getUser();
        if (principal == null) return;

        String idGoogle = principal.getName();
        abandonedPlayerCleaner.cancelarDesconexion(idGoogle);
        log.info("Jugador [{}] reconectado. Temporizador de abandono cancelado.", idGoogle);
    }

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
        Principal principal = sha.getUser();

        if (principal == null) return;

        String idGoogle = principal.getName();
        log.warn("Jugador [{}] perdió la conexión. Registrando desconexión en AbandonedPlayerCleaner...", idGoogle);
        abandonedPlayerCleaner.registrarDesconexion(idGoogle);
    }
}