package com.secretpanda.codenames.controller;

import com.secretpanda.codenames.model.PalabraTema;
import com.secretpanda.codenames.service.PalabraTemaService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/palabras")
public class PalabraTemaController {

    @Autowired
    private PalabraTemaService palabraTemaService;

    @GetMapping("/tema/{idTema}")
    public List<PalabraTema> listarPorTema(@PathVariable Integer idTema) {
        return palabraTemaService.obtenerPorTema(idTema);
    }

    @PostMapping("/tema/{idTema}")
    public ResponseEntity<PalabraTema> crear(@PathVariable Integer idTema, @RequestBody PalabraTema palabra) {
        return ResponseEntity.ok(palabraTemaService.añadirPalabra(idTema, palabra));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        palabraTemaService.eliminarPalabra(id);
        return ResponseEntity.noContent().build();
    }
}