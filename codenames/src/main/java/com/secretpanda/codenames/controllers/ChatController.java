package com.secretpanda.codenames.controllers;

import com.secretpanda.codenames.models.Chat;
import com.secretpanda.codenames.services.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @GetMapping("/partida/{idPartida}")
    public List<Chat> obtenerChatDePartida(@PathVariable Integer idPartida) {
        return chatService.obtenerMensajesDePartida(idPartida);
    }

    @PostMapping("/enviar")
    public ResponseEntity<Chat> enviarMensaje(
            @RequestParam Integer idPartida,
            @RequestParam Integer idJugadorPartida,
            @RequestBody String contenido) {
        return ResponseEntity.ok(chatService.enviarMensaje(idPartida, idJugadorPartida, contenido));
    }
}