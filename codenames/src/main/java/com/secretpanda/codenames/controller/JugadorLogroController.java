package com.secretpanda.codenames.controller;

import com.secretpanda.codenames.model.JugadorLogro;
import com.secretpanda.codenames.service.JugadorLogroService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/progreso-logros")
public class JugadorLogroController {

    @Autowired
    private JugadorLogroService jugadorLogroService;

    // Obtener todos los logros (en progreso y completados) de un jugador
    @GetMapping("/jugador/{idJugador}")
    public List<JugadorLogro> obtenerDeJugador(@PathVariable String idJugador) {
        return jugadorLogroService.obtenerLogrosDeJugador(idJugador);
    }

    // Obtener solo los completados de un jugador
    @GetMapping("/jugador/{idJugador}/completados")
    public List<JugadorLogro> obtenerCompletadosDeJugador(@PathVariable String idJugador) {
        return jugadorLogroService.obtenerLogrosCompletados(idJugador);
    }

    // Obtener el progreso de un jugador en un logro específico
    @GetMapping("/{idJugador}/{idLogro}")
    public ResponseEntity<JugadorLogro> obtenerProgreso(
            @PathVariable String idJugador, 
            @PathVariable Integer idLogro) {
        return jugadorLogroService.obtenerProgresoEspecifico(idJugador, idLogro)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Admin: Forzar una actualización de progreso
    @PostMapping
    public JugadorLogro actualizarProgresoManual(@RequestBody JugadorLogro jugadorLogro) {
        return jugadorLogroService.guardarOActualizar(jugadorLogro);
    }

    // Admin: Eliminar progreso
    @DeleteMapping("/{idJugador}/{idLogro}")
    public ResponseEntity<Void> eliminarProgreso(
            @PathVariable String idJugador, 
            @PathVariable Integer idLogro) {
        jugadorLogroService.eliminarRegistro(idJugador, idLogro);
        return ResponseEntity.noContent().build();
    }
}