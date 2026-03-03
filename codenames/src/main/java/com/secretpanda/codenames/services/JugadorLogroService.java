package com.secretpanda.codenames.services;

import com.secretpanda.codenames.models.JugadorLogro;
import com.secretpanda.codenames.models.JugadorLogroId;
import com.secretpanda.codenames.repositories.JugadorLogroRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class JugadorLogroService {

    @Autowired
    private JugadorLogroRepository jugadorLogroRepository;

    public List<JugadorLogro> obtenerLogrosDeJugador(String idJugador) {
        return jugadorLogroRepository.findById_IdJugador(idJugador);
    }

    public List<JugadorLogro> obtenerLogrosCompletados(String idJugador) {
        return jugadorLogroRepository.findById_IdJugadorAndCompletadoTrue(idJugador);
    }

    public Optional<JugadorLogro> obtenerProgresoEspecifico(String idJugador, Integer idLogro) {
        JugadorLogroId id = new JugadorLogroId(idJugador, idLogro);
        return jugadorLogroRepository.findById(id);
    }

    @Transactional
    public JugadorLogro guardarOActualizar(JugadorLogro jugadorLogro) {
        try {
            return jugadorLogroRepository.save(jugadorLogro);
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMostSpecificCause().getMessage());
        }
    }

    @Transactional
    public void eliminarRegistro(String idJugador, Integer idLogro) {
        JugadorLogroId id = new JugadorLogroId(idJugador, idLogro);
        try {
            jugadorLogroRepository.deleteById(id);
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMostSpecificCause().getMessage());
        }
    }
}