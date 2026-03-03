package com.secretpanda.codenames.controllers;

import com.secretpanda.codenames.models.Partida;
import com.secretpanda.codenames.services.PartidaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/partidas")
public class PartidaController {

    @Autowired
    private PartidaService partidaService;

    @GetMapping("/publicas")
    public List<Partida> listarPublicasEnEspera() {
        return partidaService.obtenerPartidasPublicasEnEspera();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Partida> obtenerPorId(@PathVariable Integer id) {
        return partidaService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/codigo/{codigo}")
    public ResponseEntity<Partida> obtenerPorCodigo(@PathVariable String codigo) {
        return partidaService.obtenerPorCodigo(codigo)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/crear/{idTema}/{idCreador}")
    public ResponseEntity<Partida> crearPartida(
            @PathVariable Integer idTema,
            @PathVariable String idCreador,
            @RequestBody Partida partida) {
        return ResponseEntity.ok(partidaService.crearPartida(idTema, idCreador, partida));
    }

    @PutMapping("/{id}/comenzar")
    public ResponseEntity<Partida> comenzarPartida(@PathVariable Integer id) {
        return ResponseEntity.ok(partidaService.comenzarPartida(id));
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<Partida> cambiarEstado(
            @PathVariable Integer id,
            @RequestParam Partida.EstadoPartida estado) { 
        return ResponseEntity.ok(partidaService.actualizarEstado(id, estado));
    }

    @PutMapping("/{id}/finalizar")
    public ResponseEntity<Partida> finalizarPartida(
            @PathVariable Integer id,
            @RequestParam Boolean rojoGana) {
        return ResponseEntity.ok(partidaService.declararGanador(id, rojoGana));
    }
}