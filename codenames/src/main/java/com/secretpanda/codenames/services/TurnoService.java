package com.secretpanda.codenames.services;

import com.secretpanda.codenames.models.Turno;
import com.secretpanda.codenames.models.Partida;
import com.secretpanda.codenames.models.JugadorPartida;
import com.secretpanda.codenames.repositories.TurnoRepository;
import com.secretpanda.codenames.repositories.PartidaRepository;
import com.secretpanda.codenames.repositories.JugadorPartidaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class TurnoService {

    @Autowired
    private TurnoRepository turnoRepository;

    @Autowired
    private PartidaRepository partidaRepository;

    @Autowired
    private JugadorPartidaRepository jugadorPartidaRepository;

    public List<Turno> obtenerTurnosDePartida(Integer idPartida) {
        return turnoRepository.findByPartida_IdPartidaOrderByNumTurnoAsc(idPartida);
    }

    public Optional<Turno> obtenerTurnoPorId(Integer idTurno) {
        return turnoRepository.findById(idTurno);
    }

    @Transactional
    public Turno registrarTurno(Integer idPartida, Integer idJugadorPartida, Turno detalles) {
        Partida partida = partidaRepository.findById(idPartida)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Partida no encontrada"));
                
        JugadorPartida jugadorPartida = jugadorPartidaRepository.findById(idJugadorPartida)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Jugador no encontrado en la partida"));


        // Asignar automáticamente el número de turno siguiente
        List<Turno> turnosExistentes = turnoRepository.findByPartida_IdPartidaOrderByNumTurnoAsc(idPartida);
        int siguienteNumTurno = turnosExistentes.size() + 1;

        detalles.setPartida(partida);
        detalles.setJugadorPartida(jugadorPartida);
        detalles.setNumTurno(siguienteNumTurno);

        try {
            return turnoRepository.save(detalles);
        } catch (DataIntegrityViolationException ex) {
            // Capturamos el posible error del trigger 
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMostSpecificCause().getMessage());
        }
    }

    // Normalmente los turnos no se borran ni actualizan una vez jugados, 
    // por lo que no incluimos métodos de actualización/borrado para los jugadores.
}