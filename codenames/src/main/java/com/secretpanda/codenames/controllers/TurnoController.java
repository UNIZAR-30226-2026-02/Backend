package com.secretpanda.codenames.controllers;

import com.secretpanda.codenames.models.Turno;
import com.secretpanda.codenames.services.TurnoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/turnos")
public class TurnoController {

    @Autowired
    private TurnoService turnoService;

    @GetMapping("/partida/{idPartida}")
    public List<Turno> obtenerHistorialTurnos(@PathVariable Integer idPartida) {
        return turnoService.obtenerTurnosDePartida(idPartida);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Turno> obtenerTurno(@PathVariable Integer id) {
        return turnoService.obtenerTurnoPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/jugar/{idPartida}/{idJugadorPartida}")
    public ResponseEntity<Turno> registrarTurno(
            @PathVariable Integer idPartida,
            @PathVariable Integer idJugadorPartida,
            @RequestBody Turno turno) {
        return ResponseEntity.ok(turnoService.registrarTurno(idPartida, idJugadorPartida, turno));
    }
}