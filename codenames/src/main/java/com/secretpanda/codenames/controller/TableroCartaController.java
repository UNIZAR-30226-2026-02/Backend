package com.secretpanda.codenames.controller;

import com.secretpanda.codenames.model.TableroCarta;
import com.secretpanda.codenames.service.TableroCartaService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tablero")
public class TableroCartaController {

    @Autowired
    private TableroCartaService tableroCartaService;

    @GetMapping("/partida/{idPartida}")
    public List<TableroCarta> obtenerTablero(@PathVariable Integer idPartida) {
        return tableroCartaService.obtenerTableroDePartida(idPartida);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TableroCarta> obtenerCarta(@PathVariable Integer id) {
        return tableroCartaService.obtenerCartaPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/generar/{idPartida}")
    public ResponseEntity<List<TableroCarta>> generarTablero(@PathVariable Integer idPartida) {
        return ResponseEntity.ok(tableroCartaService.generarTablero(idPartida));
    }

    @PutMapping("/revelar/{idCarta}")
    public ResponseEntity<TableroCarta> revelarCarta(
            @PathVariable Integer idCarta,
            @RequestParam String nuevoEstado) {
        return ResponseEntity.ok(tableroCartaService.revelarCarta(idCarta, nuevoEstado));
    }
}