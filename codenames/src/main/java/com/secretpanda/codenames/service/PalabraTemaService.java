package com.secretpanda.codenames.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.secretpanda.codenames.model.PalabraTema;
import com.secretpanda.codenames.model.Tema;
import com.secretpanda.codenames.repository.PalabraTemaRepository;
import com.secretpanda.codenames.repository.TemaRepository;

@Service
public class PalabraTemaService {

    @Autowired
    private PalabraTemaRepository palabraTemaRepository;

    @Autowired
    private TemaRepository temaRepository;

    public List<PalabraTema> obtenerPorTema(Integer idTema) {
        return palabraTemaRepository.findByTema_IdTema(idTema);
    }

    @Transactional
    public PalabraTema añadirPalabra(Integer idTema, PalabraTema palabra) {
        Tema tema = temaRepository.findById(idTema)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tema no encontrado"));
        
        palabra.setTema(tema);
        try {
            return palabraTemaRepository.save(palabra);
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error al añadir la palabra.");
        }
    }

    @Transactional
    public void eliminarPalabra(Integer id) {
        try {
            palabraTemaRepository.deleteById(id);
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No se puede eliminar la palabra porque está en uso en tableros activos.");
        }
    }
}