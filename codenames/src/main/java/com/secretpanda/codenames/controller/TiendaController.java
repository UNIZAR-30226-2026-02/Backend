package com.secretpanda.codenames.controller;

import java.security.Principal;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.secretpanda.codenames.dto.tienda.CompraRequestDTO;
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
        return ResponseEntity.ok(tiendaService.getTemasTienda(principal.getName()));
    }

    @GetMapping("/personalizaciones/activas")
    public ResponseEntity<List<PersonalizacionDTO>> getPersonalizaciones(Principal principal) {
        return ResponseEntity.ok(tiendaService.getPersonalizacionesTienda(principal.getName()));
    }

    /**
     * Endpoint unificado según Contrato API 1.4
     * POST /tienda/comprar/{id_google}
     */
    @PostMapping("/tienda/comprar/{id_google}")
    public ResponseEntity<Map<String, Object>> comprar(
            @PathVariable("id_google") String idGooglePath,
            @RequestBody CompraRequestDTO request,
            Principal principal) {
        
        // Aunque el contrato pide id_google en la URL, usamos el del token por seguridad,
        // pero validamos que coincidan o simplemente usamos el del token si es el mismo usuario.
        String idGoogle = principal.getName();
        int restantes;

        if (request.getIdTema() != null) {
            restantes = tiendaService.comprarTema(idGoogle, request.getIdTema());
        } else if (request.getIdPersonalizacion() != null) {
            restantes = tiendaService.comprarPersonalizacion(idGoogle, request.getIdPersonalizacion());
        } else {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(Map.of(
            "balas_restantes", restantes,
            "mensaje", "Compra exitosa"
        ));
    }
}
