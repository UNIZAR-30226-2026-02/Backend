package com.secretpanda.codenames.controller;

import java.security.Principal;

import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.secretpanda.codenames.dto.juego.GameStateDTO;
import com.secretpanda.codenames.service.ChatService;
import com.secretpanda.codenames.service.JuegoService;

/**
 * WS PUB:
 *   /app/partidas/{id}/pista     → Jefe da pista → inserta TURNO, broadcast estado + pista
 *   /app/partidas/{id}/votar     → Agente vota  → inserta VOTO_CARTA, broadcast estado
 *   /app/partidas/{id}/chat      → Mensaje de chat → broadcast /topic/.../chat/{equipo}
 *
 * REST:
 *   GET /api/partidas/{id}/estado → GameState inicial al cargar la pantalla de juego
 */
@Controller
@RestController
public class JuegoController {

    private final JuegoService           juegoService;
    private final ChatService            chatService;
    private final SimpMessagingTemplate  messagingTemplate;

    public JuegoController(JuegoService juegoService,
                            ChatService chatService,
                            SimpMessagingTemplate messagingTemplate) {
        this.juegoService       = juegoService;
        this.chatService        = chatService;
        this.messagingTemplate  = messagingTemplate;
    }

    // ─── REST: estado inicial ─────────────────────────────────────────────────

    /**
     * GET /api/partidas/{id_partida}/estado
     * Carga el GameState inicial al entrar a la pantalla de juego.
     */
    @GetMapping("/api/partidas/{id_partida}/estado")
    public ResponseEntity<GameStateDTO> getEstado(
            @PathVariable("id_partida") Integer idPartida,
            Principal principal) {
        return ResponseEntity.ok(juegoService.getGameState(idPartida, principal.getName()));
    }

    // ─── WS: pista ────────────────────────────────────────────────────────────

    /**
     * /app/partidas/{id_partida}/pista
     * Payload: { "palabraPista": "animal", "pistaNumero": 3 }
     *
     * El backend:
     *  1. Inserta el TURNO
     *  2. Inicia el temporizador
     *  3. Emite /topic/partidas/{id}/pista  con la PistaDTO
     *  4. Emite /topic/partidas/{id}/estado con el GameState actualizado
     */
    @MessageMapping("/partidas/{id_partida}/pista")
    public void darPista(
            @DestinationVariable("id_partida") Integer idPartida,
            @Payload PistaPayload payload,
            Principal principal) {
        juegoService.darPista(idPartida,
                payload.palabraPista(),
                payload.pistaNumero(),
                principal.getName());
    }

    // ─── WS: votar ────────────────────────────────────────────────────────────

    /**
     * /app/partidas/{id_partida}/votar
     * Payload: { "idCartaTablero": 15, "idTurno": 3 }
     *
     * El backend:
     *  1. Inserta el VOTO_CARTA (o actualiza si ya votó)
     *  2. Comprueba si todos los agentes votaron → resolverVotacion
     *  3. Emite /topic/partidas/{id}/estado con votos actualizados
     */
    @MessageMapping("/partidas/{id_partida}/votar")
    public void votar(
            @DestinationVariable("id_partida") Integer idPartida,
            @Payload VotarPayload payload,
            Principal principal) {
        juegoService.votar(idPartida,
                payload.idCartaTablero(),
                payload.idTurno(),
                principal.getName());
    }

    // // ─── WS: chat ─────────────────────────────────────────────────────────────

    // /**
    //  * /app/partidas/{id_partida}/chat
    //  * Payload: { "mensaje": "creo que es el gato" }
    //  *
    //  * El backend:
    //  *  1. Filtra el mensaje
    //  *  2. Guarda en CHAT
    //  *  3. Emite a /topic/partidas/{id}/chat/{equipo}
    //  */
    // @MessageMapping("/partidas/{id_partida}/chat")
    // public void enviarMensaje(
    //         @DestinationVariable("id_partida") Integer idPartida,
    //         @Payload EnviarMensajeDTO dto,
    //         Principal principal) {
    //     dto.setIdPartida(idPartida);
    //     ChatMessageDTO mensaje = chatService.procesarMensaje(dto, principal.getName());
    //     messagingTemplate.convertAndSend(
    //             "/topic/partidas/" + idPartida + "/chat/" + mensaje.getEquipo().toLowerCase(),
    //             mensaje);
    // }

    // ─── Payloads ─────────────────────────────────────────────────────────────

    public record PistaPayload(String palabraPista, int pistaNumero) {}
    public record VotarPayload(Integer idCartaTablero, Integer idTurno) {}
}
