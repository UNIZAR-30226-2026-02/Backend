package com.secretpanda.codenames.controller;

import java.security.Principal;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.secretpanda.codenames.dto.partida.CrearPartidaDTO;
import com.secretpanda.codenames.dto.partida.LobbyStatusDTO;
import com.secretpanda.codenames.dto.partida.RolPartidaDTO;
import com.secretpanda.codenames.model.Partida;
import com.secretpanda.codenames.repository.PartidaRepository;
import com.secretpanda.codenames.service.LobbyService;
import com.secretpanda.codenames.service.PartidaService;

/**
 * POST /api/partidas/                         → crear partida
 * POST /api/partidas/{id}/unirse/privada      → unirse con código
 * POST /api/partidas/{id}/unirse/publica      → unirse a pública
 * DELETE /api/partidas/{id}/participantes     → abandonar
 * GET  /api/partidas/{id}/participantes/rol   → rol y equipo del jugador
 */
@RestController
@RequestMapping("/api/partidas")
public class PartidaController {

    private final PartidaService partidaService;
    private final LobbyService lobbyService;
    private final PartidaRepository partidaRepository;

    public PartidaController(PartidaService partidaService,
                              LobbyService lobbyService,
                              PartidaRepository partidaRepository) {
        this.partidaService = partidaService;
        this.lobbyService = lobbyService;
        this.partidaRepository = partidaRepository;
    }

    // ─── Crear partida ─────────────────────────────────────────────────────────

    @PostMapping("/")
    public ResponseEntity<LobbyStatusDTO> crearPartida(
            @RequestBody CrearPartidaDTO dto,
            Principal principal) {
        LobbyStatusDTO response = partidaService.crearPartida(dto, principal.getName());
        if (dto.isEsPublica()) {
            lobbyService.broadcastPartidasPublicas();
        }
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // ─── Unirse a privada ──────────────────────────────────────────────────────

    @PostMapping("/{id_partida}/unirse/privada")
    public ResponseEntity<Void> unirsePrivada(
            @PathVariable("id_partida") Integer idPartida,
            @RequestBody UnirseConCodigoRequest body,
            Principal principal) {
        partidaService.unirsePartidaPrivada(idPartida, body.codigoPartida(), principal.getName());
        Partida p = requirePartida(idPartida);
        lobbyService.broadcastLobby(p);
        return ResponseEntity.ok().build();
    }

    // ─── Unirse a pública ──────────────────────────────────────────────────────

    @PostMapping("/{id_partida}/unirse/publica")
    public ResponseEntity<Void> unirsePublica(
            @PathVariable("id_partida") Integer idPartida,
            Principal principal) {
        partidaService.unirsePartidaPublica(idPartida, principal.getName());
        Partida p = requirePartida(idPartida);
        lobbyService.broadcastLobby(p);
        lobbyService.broadcastPartidasPublicas();
        return ResponseEntity.ok().build();
    }

    // ─── Abandonar ─────────────────────────────────────────────────────────────

    @DeleteMapping("/{id_partida}/participantes")
    public ResponseEntity<Void> abandonar(
            @PathVariable("id_partida") Integer idPartida,
            Principal principal) {
        partidaService.abandonar(idPartida, principal.getName());
        return ResponseEntity.ok().build();
    }

    // ─── Rol del jugador ───────────────────────────────────────────────────────

    @GetMapping("/{id_partida}/participantes/rol")
    public ResponseEntity<RolPartidaDTO> getRol(
            @PathVariable("id_partida") Integer idPartida,
            Principal principal) {
        return ResponseEntity.ok(partidaService.getRolJugador(idPartida, principal.getName()));
    }

    // ─── Helper ────────────────────────────────────────────────────────────────

    private Partida requirePartida(Integer idPartida) {
        return partidaRepository.findById(idPartida)
                .orElseThrow(() -> new com.secretpanda.codenames.exception.NotFoundException("Partida no encontrada."));
    }

    public record UnirseConCodigoRequest(String codigoPartida) {}
}
