package com.secretpanda.codenames.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.secretpanda.codenames.dto.partida.CrearPartidaDTO;
import com.secretpanda.codenames.dto.partida.JugadorPartidaDTO;
import com.secretpanda.codenames.dto.partida.LobbyStatusDTO;
import com.secretpanda.codenames.dto.partida.UnirsePartidaDTO;
import com.secretpanda.codenames.service.PartidaService;

/**
 * Controlador REST para la gestión de partidas antes de que empiecen (Lobby / Matchmaking).
 */
@RestController
@RequestMapping("/api/partidas")
public class PartidaController {

    private final PartidaService partidaService;

    // Inyección de dependencias por constructor (buenas prácticas)
    public PartidaController(PartidaService partidaService) {
        this.partidaService = partidaService;
    }

    /**
     * Endpoint: POST /api/partidas
     * Contrato: Crea una sala nueva y autogenera el código.
     */
    @PostMapping
    public ResponseEntity<LobbyStatusDTO> crearPartida(@RequestBody CrearPartidaDTO dto, Principal principal) {
        // principal.getName() contiene el idGoogle del token validado, nadie puede suplantar al creador
        LobbyStatusDTO response = partidaService.crearPartida(dto, principal.getName());
        return new ResponseEntity<>(response, HttpStatus.CREATED); // Devuelve 201 Created
    }

    /**
     * Endpoint: GET /api/partidas/publicas
     * Contrato: Listado de partidas con esPublica=true y estado=ESPERANDO para el matchmaking.
     */
    @GetMapping("/publicas")
    public ResponseEntity<List<LobbyStatusDTO>> listarPartidasPublicas() {
        // Obtenemos la lista segura y mapeada a DTOs
        List<LobbyStatusDTO> publicas = partidaService.listarPartidasPublicasDisponibles();
        return ResponseEntity.ok(publicas); // Devuelve 200 OK
    }

    /**
     * Endpoint: POST /api/partidas/{id_partida}/unirse
     * Contrato: Entrar al lobby de una partida. Recibe el código si es privada.
     */
    @PostMapping("/{id_partida}/unirse")
    public ResponseEntity<JugadorPartidaDTO> unirsePartida(
            @PathVariable("id_partida") Integer idPartida,
            @RequestBody(required = false) UnirsePartidaDTO dto, 
            Principal principal) {
        
        // Si la partida es pública y el frontend envía un body vacío, evitamos el NullPointerException
        if (dto == null) {
            dto = new UnirsePartidaDTO();
        }
        
        // Unimos al jugador (extraído del token de forma segura) a la partida
        JugadorPartidaDTO response = partidaService.unirsePartida(idPartida, dto, principal.getName());
        return ResponseEntity.ok(response); // Devuelve 200 OK
    }
}