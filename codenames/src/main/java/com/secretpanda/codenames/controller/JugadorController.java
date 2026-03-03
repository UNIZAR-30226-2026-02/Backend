package com.secretpanda.codenames.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.secretpanda.codenames.model.Jugador;
import com.secretpanda.codenames.service.JugadorService;

@RestController // Indica que los métodos devuelven datos (JSON) y no vistas web
@RequestMapping("/api/jugadores") // URL base para todos los métodos de esta clase
public class JugadorController {

    @Autowired
    private JugadorService jugadorService;

    // GET: /api/jugadores -> Obtiene lista
    @GetMapping
    public List<Jugador> listar() {
        return jugadorService.obtenerTodos();
    }

    // GET: /api/jugadores/{id} -> Obtiene uno específico
    @GetMapping("/{id}")
    public ResponseEntity<Jugador> obtener(@PathVariable String id) {
        return jugadorService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST: /api/jugadores -> Crea uno nuevo
    @PostMapping
    public Jugador crear(@RequestBody Jugador jugador) {
        return jugadorService.crearJugador(jugador);
    }

    // PUT: /api/jugadores/{id} -> Actualiza uno existente
    @PutMapping("/{id}")
    public ResponseEntity<Jugador> actualizar(@PathVariable String id, @RequestBody Jugador detalles) {
        try {
            return ResponseEntity.ok(jugadorService.actualizarJugador(id, detalles));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // DELETE: /api/jugadores/{id} -> Borra uno
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable String id) {
        jugadorService.eliminarJugador(id);
        return ResponseEntity.noContent().build();
    }
}