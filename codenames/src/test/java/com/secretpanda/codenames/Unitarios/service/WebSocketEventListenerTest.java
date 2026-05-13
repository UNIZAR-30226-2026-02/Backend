package com.secretpanda.codenames.Unitarios.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.security.Principal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import com.secretpanda.codenames.config.WebSocketEventListener;
import com.secretpanda.codenames.repository.JugadorPartidaRepository;
import com.secretpanda.codenames.service.LobbyService;
import com.secretpanda.codenames.service.PartidaService;

@ExtendWith(MockitoExtension.class)
public class WebSocketEventListenerTest {

    @Mock private LobbyService lobbyService;
    @Mock private PartidaService partidaService;
    @Mock private JugadorPartidaRepository jugadorPartidaRepository;
    @Mock private TaskScheduler taskScheduler;
    @Mock private org.springframework.transaction.support.TransactionTemplate transactionTemplate;

    @InjectMocks
    private WebSocketEventListener webSocketEventListener;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(webSocketEventListener, "timeoutReconexion", 60);
    }

    @Test
    void testMultiTabConnection_ShouldNotScheduleAbandonmentWhenOneTabRemains() {
        String idGoogle = "user1";
        Principal principal = () -> idGoogle;

        // --- SIMULAR CONEXIÓN TAB 1 ---
        SessionConnectedEvent connectEvent1 = createConnectEvent(principal, "session1");
        webSocketEventListener.handleWebSocketConnectListener(connectEvent1);

        // --- SIMULAR CONEXIÓN TAB 2 ---
        SessionConnectedEvent connectEvent2 = createConnectEvent(principal, "session2");
        webSocketEventListener.handleWebSocketConnectListener(connectEvent2);

        // --- SIMULAR DESCONEXIÓN TAB 1 ---
        SessionDisconnectEvent disconnectEvent1 = createDisconnectEvent(principal, "session1");
        webSocketEventListener.handleDisconnect(disconnectEvent1);

        // VERIFICACIÓN: No se debe haber programado ninguna tarea de abandono porque queda 1 pestaña
        verify(taskScheduler, org.mockito.Mockito.never()).schedule(any(Runnable.class), any(java.time.Instant.class));
    }

    @Test
    void testLastTabDisconnection_ShouldScheduleAbandonment() {
        String idGoogle = "user1";
        Principal principal = () -> idGoogle;

        // --- CONECTAR Y DESCONECTAR ÚNICA TAB ---
        webSocketEventListener.handleWebSocketConnectListener(createConnectEvent(principal, "session1"));
        
        when(taskScheduler.schedule(any(Runnable.class), any(java.time.Instant.class)))
            .thenReturn(mock(ScheduledFuture.class));

        webSocketEventListener.handleDisconnect(createDisconnectEvent(principal, "session1"));

        // VERIFICACIÓN: Se programa el abandono
        verify(taskScheduler).schedule(any(Runnable.class), any(java.time.Instant.class));
    }

    @Test
    void testReconnection_ShouldCancelScheduledTask() {
        String idGoogle = "user1";
        Principal principal = () -> idGoogle;
        ScheduledFuture<?> mockTask = mock(ScheduledFuture.class);

        // 1. Conectar y desconectar para programar tarea
        webSocketEventListener.handleWebSocketConnectListener(createConnectEvent(principal, "session1"));
        when(taskScheduler.schedule(any(Runnable.class), any(java.time.Instant.class))).thenReturn((ScheduledFuture)mockTask);
        webSocketEventListener.handleDisconnect(createDisconnectEvent(principal, "session1"));

        // 2. Reconectar
        webSocketEventListener.handleWebSocketConnectListener(createConnectEvent(principal, "session2"));

        // VERIFICACIÓN: La tarea se cancela
        verify(mockTask).cancel(false);
    }

    // --- Helpers para crear eventos de Spring ---

    private SessionConnectedEvent createConnectEvent(Principal principal, String sessionId) {
        StompHeaderAccessor sha = StompHeaderAccessor.create(org.springframework.messaging.simp.stomp.StompCommand.CONNECT);
        sha.setSessionId(sessionId);
        sha.setUser(principal);
        Message<byte[]> message = org.springframework.messaging.support.MessageBuilder.withPayload(new byte[0])
                .setHeaders(sha)
                .build();
        return new SessionConnectedEvent(this, message, principal);
    }

    private SessionDisconnectEvent createDisconnectEvent(Principal principal, String sessionId) {
        StompHeaderAccessor sha = StompHeaderAccessor.create(org.springframework.messaging.simp.stomp.StompCommand.DISCONNECT);
        sha.setSessionId(sessionId);
        sha.setUser(principal);
        Message<byte[]> message = org.springframework.messaging.support.MessageBuilder.withPayload(new byte[0])
                .setHeaders(sha)
                .build();
        return new SessionDisconnectEvent(this, message, sessionId, org.springframework.web.socket.CloseStatus.NORMAL);
    }
}
