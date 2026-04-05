package com.secretpanda.codenames.controller;

import java.security.Principal;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.secretpanda.codenames.dto.partida.CrearPartidaDTO;
import com.secretpanda.codenames.dto.partida.LobbyStatusDTO;
import com.secretpanda.codenames.dto.partida.RolPartidaDTO;
import com.secretpanda.codenames.exception.NotFoundException;
import com.secretpanda.codenames.model.Partida;
import com.secretpanda.codenames.repository.PartidaRepository;
import com.secretpanda.codenames.service.LobbyService;
import com.secretpanda.codenames.service.PartidaService;

/**
 * POST /api/partidas/                              → crear partida
 * POST /api/partidas/{codigo_partida}/unirse/privada → unirse con código (Contrato actualizado)
 * POST /api/partidas/{id_partida}/unirse/publica     → unirse a pública
 * DELETE /api/partidas/{id_partida}/participantes    → abandonar
 * GET  /api/partidas/{id_partida}/participantes/rol  → rol y equipo del jugador
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

    // ─── Unirse a privada (Contrato: api/partidas/{codigo_partida}/unirse/privada) ──

    /**
     * Endpoint actualizado: recibe codigoPartida en el path.
     * El jugador solo necesita el código que le comparte el creador, sin necesitar
     * el idPartida interno, que no conoce.
     */
    @PostMapping("/{codigo_partida}/unirse/privada")
    public ResponseEntity<Integer> unirsePrivada(
            @PathVariable("codigo_partida") String codigoPartida,
            Principal principal) {
        
        // 1. Ejecutar la unión en el servicio (busca por código)
        Integer idPartida = partidaService.unirsePartidaPrivada(codigoPartida, principal.getName());

        // 2. Resolver la partida para poder notificar al lobby por WebSocket
        Partida partida = partidaRepository.findByCodigoPartida(codigoPartida.toUpperCase().trim())
                .orElseThrow(() -> new NotFoundException(
                        "No existe ninguna partida con el código: " + codigoPartida));

        // 3. Notificar a todos los del lobby
        lobbyService.broadcastLobby(partida);

        return ResponseEntity.ok(idPartida);
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
                .orElseThrow(() -> new NotFoundException("Partida no encontrada."));
    }
}