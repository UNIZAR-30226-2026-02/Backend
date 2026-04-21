package com.secretpanda.codenames.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.secretpanda.codenames.dto.tienda.ComprarItemRequest;
import com.secretpanda.codenames.dto.tienda.PersonalizacionDTO;
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
        String idGoogle = (principal != null) ? principal.getName() : null;
        return ResponseEntity.ok(tiendaService.getTemasTienda(idGoogle));
    }

    @GetMapping("/personalizaciones/activas")
    public ResponseEntity<List<PersonalizacionDTO>> getPersonalizaciones(Principal principal) {
        String idGoogle = (principal != null) ? principal.getName() : null;
        return ResponseEntity.ok(tiendaService.getPersonalizacionesTienda(idGoogle));
    }

    /**
     * POST /api/tienda/comprar/tema
     */
    @PostMapping("/tienda/comprar/tema")
    public ResponseEntity<?> comprarTema(@RequestBody ComprarItemRequest dto, Principal principal) {
        int saldo = tiendaService.comprarTema(principal.getName(), dto.getIdTema());
        return ResponseEntity.ok().body(new java.util.HashMap<String, Integer>() {{
            put("balas", saldo);
        }});
    }

    /**
     * POST /api/tienda/comprar/personalizacion
     */
    @PostMapping("/tienda/comprar/personalizacion")
    public ResponseEntity<?> comprarPersonalizacion(@RequestBody ComprarItemRequest dto, Principal principal) {
        int saldo = tiendaService.comprarPersonalizacion(principal.getName(), dto.getIdPersonalizacion());
        return ResponseEntity.ok().body(new java.util.HashMap<String, Integer>() {{
            put("balas", saldo);
        }});
    }
}