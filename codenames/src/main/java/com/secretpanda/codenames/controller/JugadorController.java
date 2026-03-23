package com.secretpanda.codenames.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.secretpanda.codenames.dto.jugador.ActualizarPerfilDTO;
import com.secretpanda.codenames.dto.jugador.HistorialDTO;
import com.secretpanda.codenames.dto.jugador.JugadorDTO;
import com.secretpanda.codenames.dto.jugador.TemaInventarioDTO;
import com.secretpanda.codenames.dto.tienda.LogroDTO;
import com.secretpanda.codenames.service.JugadorService;

/**
 * GET  /api/jugadores            → perfil completo (UserContext)
 * PUT  /api/jugadores            → actualizar tag y foto
 * GET  /api/jugadores/temas      → temas adquiridos (para crear/filtrar partidas)
 * GET  /api/jugadores/historial  → últimas 30 partidas
 * GET  /api/jugadores/logros     → colección de logros y medallas
 */
@RestController
@RequestMapping("/api/jugadores")
public class JugadorController {

    private final JugadorService jugadorService;

    public JugadorController(JugadorService jugadorService) {
        this.jugadorService = jugadorService;
    }

    /** Obtiene el perfil completo del jugador autenticado. */
    @GetMapping
    public ResponseEntity<JugadorDTO> getPerfil(Principal principal) {
        return ResponseEntity.ok(jugadorService.getPerfil(principal.getName()));
    }

    /** Modifica tag y/o foto de perfil. */
    @PutMapping
    public ResponseEntity<JugadorDTO> actualizarPerfil(
            @RequestBody ActualizarPerfilDTO dto,
            Principal principal) {
        return ResponseEntity.ok(jugadorService.actualizarPerfil(dto, principal.getName()));
    }

    /** Temas adquiridos. Usado en "Crear Partida" y filtro de partidas públicas. */
    @GetMapping("/temas")
    public ResponseEntity<List<TemaInventarioDTO>> getTemas(Principal principal) {
        return ResponseEntity.ok(jugadorService.getTemasAdquiridos(principal.getName()));
    }

    /** Historial de las últimas 30 partidas. */
    @GetMapping("/historial")
    public ResponseEntity<HistorialDTO> getHistorial(Principal principal) {
        return ResponseEntity.ok(jugadorService.getHistorial(principal.getName()));
    }

    /** Colección de logros y medallas con progreso. */
    @GetMapping("/logros")
    public ResponseEntity<List<LogroDTO>> getLogros(Principal principal) {
        return ResponseEntity.ok(jugadorService.getLogros(principal.getName()));
    }
}
