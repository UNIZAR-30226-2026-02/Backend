package com.secretpanda.codenames.controller;

import com.secretpanda.codenames.model.Tema;
import com.secretpanda.codenames.service.TemaService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/temas")
public class TemaController {

    @Autowired
    private TemaService temaService;

    @GetMapping
    public List<Tema> listarTodos() {
        return temaService.obtenerTodos();
    }

    @GetMapping("/activos")
    public List<Tema> listarActivos() {
        return temaService.obtenerActivos();
    }

    @PostMapping
    public ResponseEntity<Tema> crear(@RequestBody Tema tema) {
        return ResponseEntity.ok(temaService.crearTema(tema));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Tema> actualizar(@PathVariable Integer id, @RequestBody Tema tema) {
        return ResponseEntity.ok(temaService.actualizarTema(id, tema));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        temaService.eliminarTema(id);
        return ResponseEntity.noContent().build();
    }
}