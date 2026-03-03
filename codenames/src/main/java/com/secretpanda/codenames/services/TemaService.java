package com.secretpanda.codenames.services;

import com.secretpanda.codenames.models.Tema;
import com.secretpanda.codenames.repositories.TemaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class TemaService {

    @Autowired
    private TemaRepository temaRepository;

    public List<Tema> obtenerTodos() {
        return temaRepository.findAll();
    }

    public List<Tema> obtenerActivos() {
        return temaRepository.findByActivoTrue();
    }

    @Transactional
    public Tema crearTema(Tema tema) {
        try {
            return temaRepository.save(tema);
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error: El nombre del tema ya existe o los datos son inválidos.");
        }
    }

    @Transactional
    public Tema actualizarTema(Integer id, Tema detalles) {
        Tema tema = temaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tema no encontrado"));
        
        tema.setNombre(detalles.getNombre());
        tema.setDescripcion(detalles.getDescripcion());
        tema.setPrecioBalas(detalles.getPrecioBalas());
        tema.setActivo(detalles.getActivo());

        try {
            return temaRepository.save(tema);
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMostSpecificCause().getMessage());
        }
    }

    @Transactional
    public void eliminarTema(Integer id) {
        try {
            temaRepository.deleteById(id);
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No se puede eliminar el tema porque tiene palabras o partidas asociadas.");
        }
    }
}