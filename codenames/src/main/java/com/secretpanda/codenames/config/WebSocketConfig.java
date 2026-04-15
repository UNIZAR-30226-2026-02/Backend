package com.secretpanda.codenames.config;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import com.secretpanda.codenames.security.JwtService;

/**
 * Configuración de WebSocket con protocolo STOMP.
 *
 * Arquitectura de mensajería:
 *
 *   Cliente → /app/<destino>         Llega al @MessageMapping del servidor
 *   Servidor → /topic/<destino>      Broadcast a todos los suscritos (partida completa)
 *   Servidor → /queue/<destino>      Mensaje privado a un usuario concreto
 *
 * Ejemplos de topics usados en la partida:
 *   /topic/partida/{id}/tablero      → actualización del tablero en tiempo real
 *   /topic/partida/{id}/turno        → cambio de turno
 *   /topic/partida/{id}/chat/{equipo}→ chat de equipo (ROJO / AZUL)
 *   /topic/partida/{id}/fin          → resultado final de la partida
 *
 * Autenticación:
 *   El JWT se envía en la trama STOMP CONNECT dentro del header "Authorization".
 *   El interceptor JwtChannelInterceptor lo valida y autentica la conexión.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Autowired
    private JwtService jwtService;

    /** Orígenes permitidos para el handshake WebSocket (igual que CORS REST) */
    @Value("${cors.allowed-origins:https://codenamesreactweb-hgebahh0bvg6aah6.spaincentral-01.azurewebsites.net,http://localhost:5173}")
    private String[] allowedOrigins ;   
    
    // ──────────────────────────────────────────────────────────────────────────
    // 1. Endpoint de conexión WebSocket
    // ──────────────────────────────────────────────────────────────────────────

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")             // ws://host/ws  (WSS en producción)
                .setAllowedOrigins(allowedOrigins)
                .withSockJS();                  // Fallback para navegadores sin WS nativo
    }

    // ──────────────────────────────────────────────────────────────────────────
    // 2. Broker de mensajes
    // ──────────────────────────────────────────────────────────────────────────

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Prefijo para mensajes que van al servidor (a un @MessageMapping)
        registry.setApplicationDestinationPrefixes("/app");

        // Broker en memoria para distribuir mensajes a los clientes suscritos.
        // /topic → broadcast (1 a N), /queue → unicast (1 a 1)
        registry.enableSimpleBroker("/topic", "/queue");

        // Prefijo para mensajes privados dirigidos a un usuario concreto
        // p. ej.: /user/{sessionId}/queue/errores
        registry.setUserDestinationPrefix("/user");
    }

    // ──────────────────────────────────────────────────────────────────────────
    // 3. Interceptor de autenticación JWT en el canal de entrada
    // ──────────────────────────────────────────────────────────────────────────

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {

            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor =
                        MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

                // Solo procesamos la trama CONNECT (primer mensaje de la sesión WS)
                if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {

                    String authHeader = accessor.getFirstNativeHeader("Authorization");

                    if (authHeader != null && authHeader.startsWith("Bearer ")) {
                        String token = authHeader.substring(7);

                        if (jwtService.esTokenValido(token)) {
                            String idGoogle = jwtService.extraerIdGoogle(token);

                            // Autenticamos la sesión WebSocket con el idGoogle del jugador
                            UsernamePasswordAuthenticationToken auth =
                                    new UsernamePasswordAuthenticationToken(
                                            idGoogle,
                                            null,
                                            Collections.emptyList()
                                    );
                            accessor.setUser(auth);
                        }
                        // Si el token no es válido simplemente no se autentica;
                        // Spring rechazará automáticamente las suscripciones protegidas.
                    }
                }

                return message;
            }
        });
    }
}