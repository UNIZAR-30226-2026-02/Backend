package com.secretpanda.codenames.controller;

import com.secretpanda.codenames.model.Logro;
import com.secretpanda.codenames.service.LogroService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/logros")
public class LogroController {

    @Autowired
    private LogroService logroService;

    // Obtener todos los logros
    @GetMapping
    public List<Logro> listarTodos() {
        return logroService.obtenerTodos();
    }

    // Obtener solo logros activos
    @GetMapping("/activos")
    public List<Logro> listarActivos() {
        return logroService.obtenerActivos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Logro> obtener(@PathVariable Integer id) {
        return logroService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Logro crear(@RequestBody Logro logro) {
        return logroService.crearLogro(logro);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Logro> actualizar(@PathVariable Integer id, @RequestBody Logro detalles) {
        try {
            return ResponseEntity.ok(logroService.actualizarLogro(id, detalles));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Hará un "Soft Delete" (marcarlo como activo = false) según definimos en el Service
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        logroService.eliminarLogro(id);
        return ResponseEntity.noContent().build();
    }
}