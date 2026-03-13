package com.secretpanda.codenames.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.secretpanda.codenames.model.Jugador;
import com.secretpanda.codenames.model.JugadorPartida;
import com.secretpanda.codenames.model.Partida;
import com.secretpanda.codenames.model.JugadorPartida.Equipo;
import com.secretpanda.codenames.model.JugadorPartida.Rol;
import com.secretpanda.codenames.repository.JugadorPartidaRepository;
import com.secretpanda.codenames.repository.JugadorRepository;
import com.secretpanda.codenames.repository.PartidaRepository;

@Service
public class JugadorPartidaService {

    @Autowired
    private JugadorPartidaRepository jugadorPartidaRepository;

    @Autowired
    private JugadorRepository jugadorRepository;

    @Autowired
    private PartidaRepository partidaRepository;

    public List<JugadorPartida> obtenerJugadoresDePartida(Integer idPartida) {
        return jugadorPartidaRepository.findByPartida_IdPartida(idPartida);
    }

    public List<JugadorPartida> obtenerHistorialJugador(String idJugador) {
        return jugadorPartidaRepository.findByJugador_IdGoogle(idJugador);
    }

    public Optional<JugadorPartida> obtenerParticipacion(Integer idJugadorPartida) {
        return jugadorPartidaRepository.findById(idJugadorPartida);
    }

    // Unirse a una partida en la sala de espera
    public JugadorPartida unirseAPartida(String idJugador, Integer idPartida, Equipo equipo, Rol rol) {
        if (jugadorPartidaRepository.existsByJugador_IdGoogleAndPartida_IdPartida(idJugador, idPartida)) {
            throw new RuntimeException("El jugador ya está en esta partida");
        }

        Jugador jugador = jugadorRepository.findById(idJugador)
                .orElseThrow(() -> new RuntimeException("Jugador no encontrado"));
        Partida partida = partidaRepository.findById(idPartida)
                .orElseThrow(() -> new RuntimeException("Partida no encontrada"));

        JugadorPartida jugadorPartida = new JugadorPartida();
        jugadorPartida.setJugador(jugador);
        jugadorPartida.setPartida(partida);
        jugadorPartida.setEquipo(equipo);
        jugadorPartida.setRol(rol);
        jugadorPartida.setNumAciertos(0);
        jugadorPartida.setNumFallos(0);
        jugadorPartida.setAbandono(false);

        return jugadorPartidaRepository.save(jugadorPartida);
    }

    // Cambiarse de equipo o rol en la sala de espera
    public JugadorPartida actualizarRolEquipo(Integer idJugadorPartida, Equipo nuevoEquipo, Rol nuevoRol) {
        return jugadorPartidaRepository.findById(idJugadorPartida).map(jp -> {
            jp.setEquipo(nuevoEquipo);
            jp.setRol(nuevoRol);
            return jugadorPartidaRepository.save(jp);
        }).orElseThrow(() -> new RuntimeException("Registro no encontrado"));
    }

    // Marcar que un jugador ha abandonado a mitad de partida
    public JugadorPartida abandonarPartida(Integer idJugadorPartida) {
        return jugadorPartidaRepository.findById(idJugadorPartida).map(jp -> {
            jp.setAbandono(true);
            return jugadorPartidaRepository.save(jp);
        }).orElseThrow(() -> new RuntimeException("Registro no encontrado"));
    }

    // Si el jugador sale antes de que empiece, simplemente borramos el registro
    public void expulsarOSalirDeSala(Integer idJugadorPartida) {
        jugadorPartidaRepository.deleteById(idJugadorPartida);
    }
}