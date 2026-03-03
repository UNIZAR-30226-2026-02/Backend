package com.secretpanda.codenames.services;

import com.secretpanda.codenames.models.Amistad;
import com.secretpanda.codenames.models.AmistadId;
import com.secretpanda.codenames.models.Jugador;
import com.secretpanda.codenames.repositories.AmistadRepository;
import com.secretpanda.codenames.repositories.JugadorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class AmistadService {

    @Autowired
    private AmistadRepository amistadRepository;

    @Autowired
    private JugadorRepository jugadorRepository;

    public List<Amistad> obtenerTodas() {
        return amistadRepository.findAll();
    }

    public List<Amistad> obtenerSolicitudesRecibidas(String idJugador) {
        return amistadRepository.findById_IdReceptor(idJugador);
    }

    @Transactional
    public Amistad enviarSolicitud(String idSolicitante, String idReceptor) {
        if (idSolicitante.equals(idReceptor)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No puedes enviarte una solicitud a ti mismo");
        }

        Jugador solicitante = jugadorRepository.findById(idSolicitante)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Solicitante no encontrado"));
        Jugador receptor = jugadorRepository.findById(idReceptor)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Receptor no encontrado"));

        AmistadId id = new AmistadId(idSolicitante, idReceptor);
        if (amistadRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La solicitud ya existe");
        }

        Amistad amistad = new Amistad();
        amistad.setId(id);
        amistad.setSolicitante(solicitante);
        amistad.setReceptor(receptor);
        // El estado "pendiente" y la fecha se asignan por defecto en el modelo

        try {
            return amistadRepository.save(amistad);
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMostSpecificCause().getMessage());
        }
    }

    @Transactional
    public Amistad aceptarSolicitud(String idSolicitante, String idReceptor) {
        AmistadId id = new AmistadId(idSolicitante, idReceptor);
        Amistad amistad = amistadRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Solicitud no encontrada"));
                
        amistad.setEstado(Amistad.EstadoAmistad.ACEPTADA);        
        
        try {
            return amistadRepository.save(amistad);
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMostSpecificCause().getMessage());
        }
    }

    @Transactional
    public void eliminarAmistad(String idSolicitante, String idReceptor) {
        AmistadId id = new AmistadId(idSolicitante, idReceptor);
        try {
            amistadRepository.deleteById(id);
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMostSpecificCause().getMessage());
        }
    }
}