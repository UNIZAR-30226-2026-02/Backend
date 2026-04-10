package com.secretpanda.codenames.controller;

import java.security.Principal;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.secretpanda.codenames.dto.social.AmistadDTO;
import com.secretpanda.codenames.dto.social.RankingDTO;
import com.secretpanda.codenames.service.AmistadService;

@RestController
@RequestMapping("/api")
public class AmistadController {

    private final AmistadService amistadService;

    public AmistadController(AmistadService amistadService) {
        this.amistadService = amistadService;
    }

    @GetMapping("/amigos")
    public ResponseEntity<List<RankingDTO>> getAmigos(Principal principal) {
        return ResponseEntity.ok(amistadService.getAmigosAceptados(principal.getName()));
    }

    @GetMapping("/amigos/solicitudes")
    public ResponseEntity<List<AmistadDTO>> getSolicitudes(Principal principal) {
        return ResponseEntity.ok(amistadService.getSolicitudesPendientes(principal.getName()));
    }

    @PostMapping("/amigos/solicitudes")
    public ResponseEntity<Void> enviarSolicitud(@RequestBody Map<String, String> body, Principal principal) {
        amistadService.enviarSolicitud(principal.getName(), body.get("tag_receptor"));
        return ResponseEntity.ok().build();
    }

    @PutMapping("/amigos/solicitudes")
    public ResponseEntity<Void> gestionarSolicitud(@RequestBody Map<String, String> body, Principal principal) {
        amistadService.gestionarSolicitud(principal.getName(), body.get("id_solicitante"), body.get("estado"));
        return ResponseEntity.ok().build();
    }

    @GetMapping("/jugadores/buscar")
    public ResponseEntity<List<RankingDTO>> buscarJugadores(@RequestParam("tag") String tag, Principal principal) {
        return ResponseEntity.ok(amistadService.buscarJugadores(tag, principal.getName()));
    }
}