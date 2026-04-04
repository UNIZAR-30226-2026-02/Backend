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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.secretpanda.codenames.dto.juego.GameStateDTO;
import com.secretpanda.codenames.service.ChatService;
import com.secretpanda.codenames.service.JuegoService;

/**
 * WS PUB:
 *   /app/partidas/{id}/pista     → Jefe da pista → inserta TURNO, broadcast estado + pista
 *   /app/partidas/{id}/votar     → Agente vota  → inserta VOTO_CARTA, broadcast estado
 *
 * REST:
 *   GET /api/partidas/{id}/estado    → GameState inicial al cargar la pantalla de juego
 *   GET /api/partidas/{id}/resultado → GameState de partida finalizada (FIX B2)
 *
 * @JsonProperty explícito en los records de payload para evitar que la
 * estrategia global SNAKE_CASE de Jackson deserialice incorrectamente.
 *
 * idTurno es ahora Optional (Integer, puede ser null). El backend resuelve
 * el turno activo automáticamente cuando el frontend no lo envía.
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
     * Acepta tanto estado en_curso como finalizada.
     */
    @GetMapping("/api/partidas/{id_partida}/estado")
    public ResponseEntity<GameStateDTO> getEstado(
            @PathVariable("id_partida") Integer idPartida,
            Principal principal) {
        return ResponseEntity.ok(juegoService.getGameState(idPartida, principal.getName()));
    }

    /**
     * GET /api/partidas/{id_partida}/resultado
     * Endpoint dedicado para la pantalla de fin de partida.
     * Devuelve el GameState aunque la partida esté en estado 'finalizada'.
     * Incluye rojoGana, cartasRojasRestantes, cartasAzulesRestantes y tablero completo.
     */
    @GetMapping("/api/partidas/{id_partida}/resultado")
    public ResponseEntity<GameStateDTO> getResultado(
            @PathVariable("id_partida") Integer idPartida,
            Principal principal) {
        return ResponseEntity.ok(juegoService.getGameStateFinalizado(idPartida, principal.getName()));
    }

    // ─── WS: pista ────────────────────────────────────────────────────────────

    /**
     * /app/partidas/{id_partida}/pista
     *
     * Payload esperado (snake_case por @JsonProperty):
     *   { "palabraPista": "animal", "pistaNumero": 3 }
     *
     * El backend:
     *  1. Inserta el TURNO
     *  2. Inicia el temporizador
     *  3. Emite /topic/partidas/{id}/pista  con la PistaDTO
     *  4. Emite por cola personal a cada jugador su GameState con su rol correcto
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
     *
     * Payload esperado (snake_case por @JsonProperty):
     *   { "id_carta_tablero": 15 }            ← idTurno es OPCIONAL
     *   { "id_carta_tablero": 15, "id_turno": 3 }  ← también válido
     *
     * idTurno puede ser null. El backend resuelve el turno activo
     * automáticamente a partir del idPartida si el frontend no lo envía.
     *
     * El backend:
     *  1. Resuelve el turno activo (o usa el idTurno si viene informado)
     *  2. Inserta o reemplaza el VOTO_CARTA del jugador
     *  3. Comprueba si todos los agentes votaron → resolverVotacion
     *  4. Emite GameState personalizado a cada jugador según su rol
     */
    @MessageMapping("/partidas/{id_partida}/votar")
    public void votar(
            @DestinationVariable("id_partida") Integer idPartida,
            @Payload VotarPayload payload,
            Principal principal) {
        juegoService.votar(idPartida,
                payload.idCartaTablero(),
                payload.idTurno(),       // puede ser null → el service lo resuelve
                principal.getName());
    }

    // ─── Payloads ─────────────────────────────────────────────────────────────
    //
    // @JsonProperty explícito en cada campo.
    //
    // PistaPayload:   { "palabraPista": "animal", "pistaNumero": 3 }
    //   → palabraPista y pistaNumero se mantienen en camelCase porque así
    //     los tiene el frontend. Si el frontend cambia a snake_case habría
    //     que actualizar a "palabraPista":"palabraPista" vs "palabra_pista".
    //     Acordar con frontend el formato y mantener @JsonProperty para fijar el contrato.
    //
    // VotarPayload:   { "id_carta_tablero": 15 }  o  { "id_carta_tablero": 15, "id_turno": 3 }

    public record PistaPayload(
            @JsonProperty("palabraPista") String palabraPista,
            @JsonProperty("pistaNumero") int pistaNumero) {}

    public record VotarPayload(
            @JsonProperty("id_carta_tablero") Integer idCartaTablero,
            @JsonProperty("id_turno") Integer idTurno) {}       // FIX W4: Integer (nullable)
}