package com.secretpanda.codenames.controller;

import java.util.List;

import com.secretpanda.codenames.model.Chat;
import com.secretpanda.codenames.service.ChatService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;



@Controller
public class ChatController {

    @Autowired
    private ChatService chatService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @GetMapping("/api/chat/partida/{idPartida}")
    @ResponseBody
    public List<Chat> obtenerChatDePartida(@PathVariable Integer idPartida) {
        return chatService.obtenerMensajesDePartida(idPartida);
    }

    @MessageMapping("/chat/{idPartida}")
    public void enviarMensaje(
            @DestinationVariable Integer idPartida,
            ChatMessageRequest request) {
        
        Chat mensajeGuardado = chatService.enviarMensaje(idPartida, request.getIdJugadorPartida(), request.getContenido());
        messagingTemplate.convertAndSend("/topic/partida/" + idPartida + "/chat", mensajeGuardado);
    }

    public static class ChatMessageRequest {
        private Integer idJugadorPartida;
        private String contenido;

        public Integer getIdJugadorPartida() { 
            return idJugadorPartida; 
        }
        
        public void setIdJugadorPartida(Integer idJugadorPartida) { 
            this.idJugadorPartida = idJugadorPartida; 
        }
        
        public String getContenido() { 
            return contenido; 
        }
        
        public void setContenido(String contenido) { 
            this.contenido = contenido; 
        }
    }
}