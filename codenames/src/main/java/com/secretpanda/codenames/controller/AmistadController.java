package com.secretpanda.codenames.controller;

import com.secretpanda.codenames.model.Amistad;
import com.secretpanda.codenames.service.AmistadService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/amistades")
public class AmistadController {

    @Autowired
    private AmistadService amistadService;

    @GetMapping
    public List<Amistad> listarTodas() {
        return amistadService.obtenerTodas();
    }

    @GetMapping("/recibidas/{idReceptor}")
    public List<Amistad> obtenerRecibidas(@PathVariable String idReceptor) {
        return amistadService.obtenerSolicitudesRecibidas(idReceptor);
    }

    @PostMapping("/enviar/{idSolicitante}/{idReceptor}")
    public ResponseEntity<Amistad> enviarSolicitud(@PathVariable String idSolicitante, @PathVariable String idReceptor) {
        return ResponseEntity.ok(amistadService.enviarSolicitud(idSolicitante, idReceptor));
    }

    @PutMapping("/aceptar/{idSolicitante}/{idReceptor}")
    public ResponseEntity<Amistad> aceptarSolicitud(@PathVariable String idSolicitante, @PathVariable String idReceptor) {
        return ResponseEntity.ok(amistadService.aceptarSolicitud(idSolicitante, idReceptor));
    }

    @DeleteMapping("/{idSolicitante}/{idReceptor}")
    public ResponseEntity<Void> eliminarAmistad(@PathVariable String idSolicitante, @PathVariable String idReceptor) {
        amistadService.eliminarAmistad(idSolicitante, idReceptor);
        return ResponseEntity.noContent().build();
    }
}