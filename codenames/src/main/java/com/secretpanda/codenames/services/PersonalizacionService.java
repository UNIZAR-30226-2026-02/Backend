package com.secretpanda.codenames.services;

import com.secretpanda.codenames.models.Personalizacion;
import com.secretpanda.codenames.repositories.PersonalizacionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class PersonalizacionService {

    @Autowired
    private PersonalizacionRepository personalizacionRepository;

    public List<Personalizacion> obtenerTodas() {
        return personalizacionRepository.findAll();
    }

    public List<Personalizacion> obtenerActivas() {
        return personalizacionRepository.findByActivoTrue();
    }

    public List<Personalizacion> obtenerActivasPorTipo(String tipo) {
        return personalizacionRepository.findByTipoAndActivoTrue(tipo);
    }

    public Optional<Personalizacion> obtenerPorId(Integer id) {
        return personalizacionRepository.findById(id);
    }

    @Transactional
    public Personalizacion crearPersonalizacion(Personalizacion personalizacion) {
        if (personalizacionRepository.existsByNombre(personalizacion.getNombre())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe una personalización con ese nombre");
        }
        try {
            return personalizacionRepository.save(personalizacion);
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMostSpecificCause().getMessage());
        }
    }

    @Transactional
    public Personalizacion actualizarPersonalizacion(Integer id, Personalizacion detalles) {
        Personalizacion existente = personalizacionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Personalización no encontrada"));
        
        existente.setNombre(detalles.getNombre());
        existente.setDescripcion(detalles.getDescripcion());
        existente.setPrecioBala(detalles.getPrecioBala());
        existente.setTipo(detalles.getTipo());
        existente.setValorVisual(detalles.getValorVisual());
        existente.setActivo(detalles.getActivo());
        
        try {
            return personalizacionRepository.save(existente);
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMostSpecificCause().getMessage());
        }
    }

    @Transactional
    public void eliminarPersonalizacion(Integer id) {
        Personalizacion personalizacion = personalizacionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Personalización no encontrada"));
        
        personalizacion.setActivo(false);
        personalizacionRepository.save(personalizacion);
    }
}