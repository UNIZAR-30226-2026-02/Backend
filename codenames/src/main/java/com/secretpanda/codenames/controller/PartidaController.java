package com.secretpanda.codenames.controller;

import java.security.Principal;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
 * POST /api/partidas/{id}/unirse/privada           → unirse con código (mantiene compatibilidad)
 * POST /api/partidas/{id}/unirse/publica           → unirse a pública
 * POST /api/partidas/codigo/{codigo}/unirse/privada → FIX B4: unirse privada solo con código
 * DELETE /api/partidas/{id}/participantes          → abandonar
 * GET  /api/partidas/{id}/participantes/rol        → rol y equipo del jugador
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

    // ─── Unirse a privada (endpoint original — mantener para compatibilidad Android) ──

    /**
     * Endpoint original: recibe idPartida en el path y codigoPartida en el body.
     * Se mantiene para no romper clientes Android que ya lo usan.
     * El frontend web puede usar el nuevo endpoint por código (abajo).
     */
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

    // Unirse a privada por código (sin necesidad de conocer el id) ──

    /**
     * Endpoint que acepta únicamente el código de partida en el path.
     * El jugador solo necesita el código que le comparte el creador, sin necesitar
     * el idPartida interno, que no conoce.
     *
     * Endpoint: POST /api/partidas/codigo/{codigo_partida}/unirse/privada
     *
     * El backend resuelve el idPartida a partir del código y delega en la misma
     * lógica de PartidaService.unirsePartidaPrivada que el endpoint original.
     *
     * Nota: la ruta /codigo/{...}/unirse/privada evita colisiones con
     * /{id_partida}/unirse/privada porque "codigo" es literal (no un entero).
     */
    @PostMapping("/codigo/{codigo_partida}/unirse/privada")
    public ResponseEntity<Void> unirsePrivadaPorCodigo(
            @PathVariable("codigo_partida") String codigoPartida,
            Principal principal) {

        // Resolver la partida por su código (el jugador no conoce el id interno)
        Partida partida = partidaRepository.findByCodigoPartida(codigoPartida)
                .orElseThrow(() -> new NotFoundException(
                        "No existe ninguna partida con el código: " + codigoPartida));

        // Validar que sea privada (aunque PartidaService también lo valida, mejor fallar pronto)
        if (partida.isEsPublica()) {
            throw new com.secretpanda.codenames.exception.GameLogicException(
                    "Esta partida es pública. Usa el endpoint de unirse a pública.");
        }

        // Delegar en el servicio existente, pasando código para validación cruzada
        partidaService.unirsePartidaPrivada(
                partida.getIdPartida(), codigoPartida, principal.getName());

        // Notificar a todos los del lobby
        lobbyService.broadcastLobby(partida);

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
                .orElseThrow(() -> new NotFoundException("Partida no encontrada."));
    }

    public record UnirseConCodigoRequest(String codigoPartida) {}
}