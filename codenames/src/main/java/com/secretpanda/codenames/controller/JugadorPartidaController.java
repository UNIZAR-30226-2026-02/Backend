package com.secretpanda.codenames.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.secretpanda.codenames.model.JugadorPartida;
import com.secretpanda.codenames.model.JugadorPartida.Equipo;
import com.secretpanda.codenames.model.JugadorPartida.Rol;
import com.secretpanda.codenames.service.JugadorPartidaService;

@RestController
@RequestMapping("/api/participantes")
public class JugadorPartidaController {

    @Autowired
    private JugadorPartidaService jugadorPartidaService;

    // Listar todos los jugadores que están dentro de una partida
    @GetMapping("/partida/{idPartida}")
    public List<JugadorPartida> listarJugadoresDePartida(@PathVariable Integer idPartida) {
        return jugadorPartidaService.obtenerJugadoresDePartida(idPartida);
    }

    // Ver el historial de un jugador
    @GetMapping("/jugador/{idJugador}")
    public List<JugadorPartida> listarHistorialJugador(@PathVariable String idJugador) {
        return jugadorPartidaService.obtenerHistorialJugador(idJugador);
    }

    // Jugador se une a una sala
    @PostMapping("/unirse/{idPartida}/{idJugador}")
    public ResponseEntity<JugadorPartida> unirse(
            @PathVariable Integer idPartida,
            @PathVariable String idJugador,
            @RequestParam Equipo equipo,
            @RequestParam Rol rol) {
        try {
            return ResponseEntity.ok(jugadorPartidaService.unirseAPartida(idJugador, idPartida, equipo, rol));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Cambiar de equipo/rol en el lobby
    @PutMapping("/{idJugadorPartida}/cambiar-rol")
    public ResponseEntity<JugadorPartida> cambiarRol(
            @PathVariable Integer idJugadorPartida,
            @RequestParam Equipo equipo,
            @RequestParam Rol rol) {
        try {
            return ResponseEntity.ok(jugadorPartidaService.actualizarRolEquipo(idJugadorPartida, equipo, rol));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Marcar como que ha tirado del cable (abandono a mitad partida)
    @PutMapping("/{idJugadorPartida}/abandonar")
    public ResponseEntity<JugadorPartida> abandonar(@PathVariable Integer idJugadorPartida) {
        try {
            return ResponseEntity.ok(jugadorPartidaService.abandonarPartida(idJugadorPartida));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // El jugador sale de la sala de espera por su cuenta (o es expulsado por el líder)
    @DeleteMapping("/{idJugadorPartida}")
    public ResponseEntity<Void> salirDeSala(@PathVariable Integer idJugadorPartida) {
        jugadorPartidaService.expulsarOSalirDeSala(idJugadorPartida);
        return ResponseEntity.noContent().build();
    }
}