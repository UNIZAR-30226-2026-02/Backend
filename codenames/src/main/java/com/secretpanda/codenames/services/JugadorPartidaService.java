package com.secretpanda.codenames.services;

import com.secretpanda.codenames.models.JugadorPartida;
import com.secretpanda.codenames.models.Jugador;
import com.secretpanda.codenames.models.Partida;
import com.secretpanda.codenames.repositories.JugadorPartidaRepository;
import com.secretpanda.codenames.repositories.JugadorRepository;
import com.secretpanda.codenames.repositories.PartidaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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
    public JugadorPartida unirseAPartida(String idJugador, Integer idPartida, String equipo, String rol) {
        if (jugadorPartidaRepository.existsByJugador_IdGoogleAndPartida_IdPartida(idJugador, idPartida)) {
            throw new RuntimeException("El jugador ya está en esta partida");
        }

        Jugador jugador = jugadorRepository.findById(idJugador)
                .orElseThrow(() -> new RuntimeException("Jugador no encontrado"));
        Partida partida = partidaRepository.findById(idPartida)
                .orElseThrow(() -> new RuntimeException("Partida no encontrada"));

        // Nota: Tienes un trigger en la BD que verifica aforos y estados, 
        // pero podemos hacer una validación previa aquí si queremos.

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
    public JugadorPartida actualizarRolEquipo(Integer idJugadorPartida, String nuevoEquipo, String nuevoRol) {
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