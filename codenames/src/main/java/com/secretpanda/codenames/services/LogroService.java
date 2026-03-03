package com.secretpanda.codenames.services;

import com.secretpanda.codenames.models.Logro;
import com.secretpanda.codenames.repositories.LogroRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class LogroService {

    @Autowired
    private LogroRepository logroRepository;

    public List<Logro> obtenerTodos() {
        return logroRepository.findAll();
    }

    public List<Logro> obtenerActivos() {
        return logroRepository.findByActivoTrue();
    }

    public Optional<Logro> obtenerPorId(Integer id) {
        return logroRepository.findById(id);
    }

    @Transactional
    public Logro crearLogro(Logro logro) {
        if (logroRepository.existsByNombre(logro.getNombre())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un logro con ese nombre");
        }
        try {
            return logroRepository.save(logro);
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMostSpecificCause().getMessage());
        }
    }

    @Transactional
    public Logro actualizarLogro(Integer id, Logro detalles) {
        Logro existente = logroRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Logro no encontrado"));
        
        existente.setNombre(detalles.getNombre());
        existente.setDescripcion(detalles.getDescripcion());
        existente.setTipo(detalles.getTipo());
        existente.setEstadisticaClave(detalles.getEstadisticaClave());
        existente.setValorObjetivo(detalles.getValorObjetivo());
        existente.setBalasRecompensa(detalles.getBalasRecompensa());
        existente.setActivo(detalles.getActivo());
        
        try {
            return logroRepository.save(existente);
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMostSpecificCause().getMessage());
        }
    }

    @Transactional
    public void eliminarLogro(Integer id) {
        Logro logro = logroRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Logro no encontrado"));
        
        logro.setActivo(false);
        logroRepository.save(logro);
    }
}