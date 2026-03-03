package com.secretpanda.codenames.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.secretpanda.codenames.model.InventarioPersonalizacion;
import com.secretpanda.codenames.model.InventarioPersonalizacionId;
import com.secretpanda.codenames.model.Jugador;
import com.secretpanda.codenames.model.Personalizacion;
import com.secretpanda.codenames.repository.InventarioPersonalizacionRepository;
import com.secretpanda.codenames.repository.JugadorRepository;
import com.secretpanda.codenames.repository.PersonalizacionRepository;

@Service
public class InventarioPersonalizacionService {

    @Autowired
    private InventarioPersonalizacionRepository inventarioRepository;

    @Autowired
    private JugadorRepository jugadorRepository;

    @Autowired
    private PersonalizacionRepository personalizacionRepository;

    public List<InventarioPersonalizacion> obtenerInventario(String idJugador) {
        return inventarioRepository.findById_IdJugador(idJugador);
    }

    public List<InventarioPersonalizacion> obtenerEquipados(String idJugador) {
        return inventarioRepository.findById_IdJugadorAndEquipadoTrue(idJugador);
    }

    // Lógica para comprar o adquirir un objeto
    @Transactional
    public InventarioPersonalizacion adquirirObjeto(String idJugador, Integer idPersonalizacion) {
        InventarioPersonalizacionId id = new InventarioPersonalizacionId(idJugador, idPersonalizacion);
        
        if (inventarioRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El jugador ya posee esta personalización");
        }

        Jugador jugador = jugadorRepository.findById(idJugador)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Jugador no encontrado"));
                
        Personalizacion personalizacion = personalizacionRepository.findById(idPersonalizacion)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Personalización no encontrada"));

        if (jugador.getBalas() < personalizacion.getPrecioBala()) { 
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Balas insuficientes"); 
        }
        
        jugador.setBalas(jugador.getBalas() - personalizacion.getPrecioBala());
        jugadorRepository.save(jugador);

        InventarioPersonalizacion inventario = new InventarioPersonalizacion();
        inventario.setId(id);
        inventario.setJugador(jugador);
        inventario.setPersonalizacion(personalizacion);
        inventario.setEquipado(false);

        try {
            return inventarioRepository.save(inventario);
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMostSpecificCause().getMessage());
        }
    }

    // Equipa un objeto. El Trigger de la BD se encargará de desequipar el resto de su misma categoría.
    @Transactional
    public InventarioPersonalizacion equiparObjeto(String idJugador, Integer idPersonalizacion) {
        InventarioPersonalizacionId id = new InventarioPersonalizacionId(idJugador, idPersonalizacion);
        
        InventarioPersonalizacion inventario = inventarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "El jugador no posee esta personalización"));
                
        inventario.setEquipado(true);
        
        try {
            return inventarioRepository.save(inventario);
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMostSpecificCause().getMessage());
        }
    }
}