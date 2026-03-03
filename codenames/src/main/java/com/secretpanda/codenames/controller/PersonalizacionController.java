package com.secretpanda.codenames.controller;

import com.secretpanda.codenames.model.Personalizacion;
import com.secretpanda.codenames.service.PersonalizacionService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/personalizaciones")
public class PersonalizacionController {

    @Autowired
    private PersonalizacionService personalizacionService;

    @GetMapping("/admin/todas")
    public List<Personalizacion> listarTodas() {
        return personalizacionService.obtenerTodas();
    }

    @GetMapping("/tienda")
    public List<Personalizacion> listarTienda() {
        return personalizacionService.obtenerActivas();
    }

    @GetMapping("/tienda/{tipo}")
    public List<Personalizacion> listarTiendaPorTipo(@PathVariable String tipo) {
        return personalizacionService.obtenerActivasPorTipo(tipo);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Personalizacion> obtener(@PathVariable Integer id) {
        return personalizacionService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Personalizacion> crear(@RequestBody Personalizacion personalizacion) {
        return ResponseEntity.ok(personalizacionService.crearPersonalizacion(personalizacion));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Personalizacion> actualizar(@PathVariable Integer id, @RequestBody Personalizacion detalles) {
        return ResponseEntity.ok(personalizacionService.actualizarPersonalizacion(id, detalles));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        personalizacionService.eliminarPersonalizacion(id);
        return ResponseEntity.noContent().build();
    }
}