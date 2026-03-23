package com.secretpanda.codenames.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.secretpanda.codenames.dto.partida.LobbyStatusDTO;
import com.secretpanda.codenames.service.LobbyService;
import com.secretpanda.codenames.service.LobbyService.PartidaPublicaDTO;

/**
 * REST:
 *   GET  /api/partidas/{id_partida}/lobby     → estado inicial del lobby
 *   PUT  /api/partida/{id_partida}/iniciar    → iniciar la partida
 *
 * STOMP PUB:
 *   /app/partida/{id}/participantes/equipo    → cambiar equipo
 *   /app/partida/{id}/tema                   → cambiar tema (creador, privada)
 *   /app/partida/{id}/tiempoTurno            → cambiar tiempo (creador, privada)
 *   /app/partida/{id}/abandonarLobby         → salir del lobby
 *
 * STOMP SUB (broadcast iniciado por el backend):
 *   /topic/partidas/{id}/lobby               → estado lobby actualizado
 *   /topic/partidas/publicas                 → lista de partidas públicas (con @SubscribeMapping)
 */
@Controller
@RestController  // Para los endpoints REST
@RequestMapping
public class LobbyController {

    private final LobbyService lobbyService;

    public LobbyController(LobbyService lobbyService) {
        this.lobbyService = lobbyService;
    }

    // ─── REST ─────────────────────────────────────────────────────────────────

    /** Estado inicial del lobby (para carga por URL directa o recarga). */
    @GetMapping("/api/partidas/{id_partida}/lobby")
    public ResponseEntity<LobbyStatusDTO> getLobby(
            @PathVariable("id_partida") Integer idPartida) {
        return ResponseEntity.ok(lobbyService.getLobby(idPartida));
    }

    /** Iniciar partida (PUT, solo el creador). */
    @PutMapping("/api/partida/{id_partida}/iniciar")
    public ResponseEntity<Void> iniciarPartida(
            @PathVariable("id_partida") Integer idPartida,
            Principal principal) {
        lobbyService.iniciarPartida(idPartida, principal.getName());
        return ResponseEntity.ok().build();
    }

    // ─── STOMP Subscribe → envía estado inicial al suscribirse ────────────────

    /**
     * Al suscribirse a /topic/partidas/publicas el cliente recibe la lista completa.
     * Después el servidor empuja actualizaciones via broadcastPartidasPublicas().
     */
    @SubscribeMapping("/partidas/publicas")
    public List<PartidaPublicaDTO> subscribePartidasPublicas() {
        return lobbyService.listarPartidasPublicas();
    }

    // ─── STOMP PUB ────────────────────────────────────────────────────────────

    /** El usuario cambia de equipo en el lobby. */
    @MessageMapping("/partida/{id_partida}/participantes/equipo")
    public void cambiarEquipo(
            @DestinationVariable("id_partida") Integer idPartida,
            @Payload CambiarEquipoPayload payload,
            Principal principal) {
        lobbyService.cambiarEquipo(idPartida, payload.equipo(), principal.getName());
    }

    /** El creador cambia el tema (solo partidas privadas). */
    @MessageMapping("/partida/{id_partida}/tema")
    public void cambiarTema(
            @DestinationVariable("id_partida") Integer idPartida,
            @Payload CambiarTemaPayload payload,
            Principal principal) {
        lobbyService.cambiarTema(idPartida, payload.idTema(), principal.getName());
    }

    /** El creador cambia el tiempo de turno (solo partidas privadas). */
    @MessageMapping("/partida/{id_partida}/tiempoTurno")
    public void cambiarTiempoTurno(
            @DestinationVariable("id_partida") Integer idPartida,
            @Payload CambiarTiempoPayload payload,
            Principal principal) {
        lobbyService.cambiarTiempoTurno(idPartida, payload.tiempoEspera(), principal.getName());
    }

    /** Alguien abandona el lobby. */
    @MessageMapping("/partida/{id_partida}/abandonarLobby")
    public void abandonarLobby(
            @DestinationVariable("id_partida") Integer idPartida,
            Principal principal) {
        lobbyService.abandonarLobby(idPartida, principal.getName());
    }

    // ─── Payloads (records para Jackson) ──────────────────────────────────────

    public record CambiarEquipoPayload(String equipo) {}
    public record CambiarTemaPayload(Integer idTema) {}
    public record CambiarTiempoPayload(int tiempoEspera) {}
}
