package com.secretpanda.codenames.controller;

import java.security.Principal;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.secretpanda.codenames.dto.tienda.TemaDTO;
import com.secretpanda.codenames.service.TiendaService;

@RestController
@RequestMapping("/api")
public class TiendaController {

    private final TiendaService tiendaService;

    public TiendaController(TiendaService tiendaService) {
        this.tiendaService = tiendaService;
    }

    @GetMapping("/temas/activos")
    public ResponseEntity<List<TemaDTO>> getTemas(Principal principal) {
        return ResponseEntity.ok(tiendaService.getTemasTienda(principal.getName()));
    }

    @PostMapping("/tienda/comprar/tema")
    public ResponseEntity<Map<String, Integer>> comprarTema(@RequestBody Map<String, Integer> body, Principal principal) {
        int restantes = tiendaService.comprarTema(principal.getName(), body.get("id_tema"));
        return ResponseEntity.ok(Map.of("balas", restantes));
    }

    @PostMapping("/tienda/comprar/personalizacion")
    public ResponseEntity<Map<String, Integer>> comprarPerso(@RequestBody Map<String, Integer> body, Principal principal) {
        int restantes = tiendaService.comprarPersonalizacion(principal.getName(), body.get("id_personalizacion"));
        return ResponseEntity.ok(Map.of("balas", restantes));
    }
}