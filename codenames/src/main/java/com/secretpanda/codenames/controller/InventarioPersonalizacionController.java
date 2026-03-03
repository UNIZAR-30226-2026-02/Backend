package com.secretpanda.codenames.controller;

import com.secretpanda.codenames.model.InventarioPersonalizacion;
import com.secretpanda.codenames.service.InventarioPersonalizacionService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventario")
public class InventarioPersonalizacionController {

    @Autowired
    private InventarioPersonalizacionService inventarioService;

    // Ver todo el inventario de un jugador
    @GetMapping("/{idJugador}")
    public List<InventarioPersonalizacion> obtenerInventarioJugador(@PathVariable String idJugador) {
        return inventarioService.obtenerInventario(idJugador);
    }

    // Ver solo lo que lleva equipado (útil para cargar su perfil/tablero en el juego)
    @GetMapping("/{idJugador}/equipado")
    public List<InventarioPersonalizacion> obtenerEquipadosJugador(@PathVariable String idJugador) {
        return inventarioService.obtenerEquipados(idJugador);
    }

    // Comprar/Añadir un objeto al inventario
    @PostMapping("/comprar/{idJugador}/{idPersonalizacion}")
    public ResponseEntity<InventarioPersonalizacion> adquirirObjeto(
            @PathVariable String idJugador, 
            @PathVariable Integer idPersonalizacion) {
        try {
            return ResponseEntity.ok(inventarioService.adquirirObjeto(idJugador, idPersonalizacion));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Equipar un objeto
    @PutMapping("/equipar/{idJugador}/{idPersonalizacion}")
    public ResponseEntity<InventarioPersonalizacion> equiparObjeto(
            @PathVariable String idJugador, 
            @PathVariable Integer idPersonalizacion) {
        try {
            return ResponseEntity.ok(inventarioService.equiparObjeto(idJugador, idPersonalizacion));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}