package com.secretpanda.codenames.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

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
        
        System.out.println("🐼 [CHAT] ¡Llegó un mensaje al servidor!");
        System.out.println("🐼 [CHAT] Partida ID: " + idPartida);
        System.out.println("🐼 [CHAT] Texto recibido: " + dto.getMensaje());

        // COMPROBACIÓN DEL ERROR FATAL
        if (principal == null) {
            System.out.println("❌ [ERROR] ¡El Principal es NULL! Android no ha enviado el Token.");
            return; // Cortamos aquí para que no explote el servidor
        }

        System.out.println("🐼 [CHAT] Usuario identificado: " + principal.getName());

        try {
            dto.setIdPartida(idPartida);
            ChatMessageDTO mensajeProcesado = chatService.procesarMensaje(dto, principal.getName());
            String topicDestino = "/topic/partidas/" + idPartida + "/chat/" + mensajeProcesado.getEquipo().toLowerCase();
            messagingTemplate.convertAndSend(topicDestino, mensajeProcesado);
            
            System.out.println("✅ [ÉXITO] Mensaje guardado en BD y reenviado.");
        } catch (Exception e) {
            System.out.println("❌ [ERROR] Falló al guardar: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @GetMapping("/api/partidas/{id_partida}/chat/{equipo}")
    @ResponseBody
    public ResponseEntity<List<ChatMessageDTO>> obtenerHistorialChat(
        @PathVariable("id_partida") Integer idPartida,
        @PathVariable("equipo") String equipo) {
    
        List<ChatMessageDTO> historial = chatService.obtenerHistorialChat(idPartida, equipo);
        return ResponseEntity.ok(historial);
    }
}