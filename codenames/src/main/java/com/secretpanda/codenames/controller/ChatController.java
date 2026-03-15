package com.secretpanda.codenames.controller;

import java.security.Principal;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.secretpanda.codenames.dto.social.ChatMessageDTO;
import com.secretpanda.codenames.dto.social.EnviarMensajeDTO;
import com.secretpanda.codenames.service.ChatService;

/**
 * Controlador de WebSockets para el chat en tiempo real.
 */
@Controller
public class ChatController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    // Inyección por constructor (Buenas prácticas)
    public ChatController(ChatService chatService, SimpMessagingTemplate messagingTemplate) {
        this.chatService = chatService;
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * WS: /app/partidas/{id_partida}/chat
     * Contrato: Recibe el mensaje, lo filtra, lo guarda y lo difunde EXCLUSIVAMENTE 
     * al canal de escucha del equipo (/topic/partidas/{id_partida}/chat/rojo o azul).
     */
    @MessageMapping("/partidas/{id_partida}/chat")
    public void enviarMensajeChat(@DestinationVariable("id_partida") Integer idPartida, 
                                  EnviarMensajeDTO dto, 
                                  Principal principal) {
        
        // 1. Aseguramos que el DTO tiene el ID de la partida correcto sacado de la URL
        dto.setIdPartida(idPartida);
        
        // 2. El servicio valida que el jugador (extraído del JWT de forma segura) 
        // esté en la partida, aplica el filtro de toxicidad y lo guarda en BD.
        ChatMessageDTO mensajeProcesado = chatService.procesarMensaje(dto, principal.getName());
        
        // 3. Construimos el topic de destino dinámicamente según el equipo del jugador
        // Ej: /topic/partidas/5/chat/rojo
        String topicDestino = "/topic/partidas/" + idPartida + "/chat/" + mensajeProcesado.getEquipo().toLowerCase();
        
        // 4. Difundimos el mensaje al canal exclusivo
        messagingTemplate.convertAndSend(topicDestino, mensajeProcesado);
    }
}