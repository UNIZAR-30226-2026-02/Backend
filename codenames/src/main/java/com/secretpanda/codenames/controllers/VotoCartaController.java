package com.secretpanda.codenames.controllers;

import com.secretpanda.codenames.models.VotoCarta;
import com.secretpanda.codenames.services.VotoCartaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/votos")
public class VotoCartaController {

    @Autowired
    private VotoCartaService votoCartaService;

    @GetMapping("/turno/{idTurno}")
    public List<VotoCarta> listarVotosDeTurno(@PathVariable Integer idTurno) {
        return votoCartaService.obtenerVotosPorTurno(idTurno);
    }

    @PostMapping("/emitir")
    public ResponseEntity<VotoCarta> emitirVoto(
            @RequestParam Integer idTurno,
            @RequestParam Integer idJugadorPartida,
            @RequestParam Integer idCartaTablero) {
        return ResponseEntity.ok(votoCartaService.emitirVoto(idTurno, idJugadorPartida, idCartaTablero));
    }
}