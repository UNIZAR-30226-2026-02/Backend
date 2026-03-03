package com.secretpanda.codenames.services;

import com.secretpanda.codenames.models.*;
import com.secretpanda.codenames.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class VotoCartaService {

    @Autowired private VotoCartaRepository votoCartaRepository;
    @Autowired private TurnoRepository turnoRepository;
    @Autowired private JugadorPartidaRepository jugadorPartidaRepository;
    @Autowired private TableroCartaRepository tableroCartaRepository;
    @Autowired private TableroCartaService tableroCartaService;

    public List<VotoCarta> obtenerVotosPorTurno(Integer idTurno) {
        return votoCartaRepository.findByTurno_IdTurno(idTurno);
    }

    @Transactional
    public VotoCarta emitirVoto(Integer idTurno, Integer idJugadorPartida, Integer idCartaTablero) {
        Turno turno = turnoRepository.findById(idTurno)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Turno no encontrado"));
        JugadorPartida jugador = jugadorPartidaRepository.findById(idJugadorPartida)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Jugador no encontrado"));
        TableroCarta carta = tableroCartaRepository.findById(idCartaTablero)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Carta no encontrada"));

        VotoCarta voto = new VotoCarta();
        voto.setTurno(turno);
        voto.setJugadorPartida(jugador);
        voto.setCartaTablero(carta);

        try {
            VotoCarta votoGuardado = votoCartaRepository.save(voto);
            procesarConsenso(idTurno, idCartaTablero, jugador.getEquipo());
            return votoGuardado;
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMostSpecificCause().getMessage());
        }
    }

    private void procesarConsenso(Integer idTurno, Integer idCartaTablero, String equipo) {
        Turno turno = turnoRepository.findById(idTurno).get();
        List<JugadorPartida> agentesEquipo = jugadorPartidaRepository
                .findByPartida_IdPartidaAndEquipo(turno.getPartida().getIdPartida(), equipo)
                .stream()
                .filter(jp -> "agente".equals(jp.getRol()))
                .toList();

        List<VotoCarta> votosCarta = votoCartaRepository.findByTurno_IdTurno(idTurno)
                .stream()
                .filter(v -> v.getCartaTablero().getIdCartaTablero().equals(idCartaTablero))
                .toList();

        if (votosCarta.size() >= agentesEquipo.size() && !agentesEquipo.isEmpty()) {
            tableroCartaService.revelarCarta(idCartaTablero, "revelada");
        }
    }
}