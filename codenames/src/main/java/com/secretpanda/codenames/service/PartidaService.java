package com.secretpanda.codenames.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.secretpanda.codenames.model.Jugador;
import com.secretpanda.codenames.model.JugadorPartida;
import com.secretpanda.codenames.model.Partida;
import com.secretpanda.codenames.model.Tema;
import com.secretpanda.codenames.repository.JugadorPartidaRepository;
import com.secretpanda.codenames.repository.JugadorRepository;
import com.secretpanda.codenames.repository.PartidaRepository;
import com.secretpanda.codenames.repository.TemaRepository;

@Service
public class PartidaService {

    @Autowired
    private PartidaRepository partidaRepository;

    @Autowired
    private JugadorRepository jugadorRepository;

    @Autowired
    private TemaRepository temaRepository;

    @Autowired
    private JugadorPartidaRepository jugadorPartidaRepository;

    @Autowired
    private TableroCartaService tableroCartaService;

    public List<Partida> obtenerPartidasPublicasEnEspera() {
        return partidaRepository.findByEsPublicaTrueAndEstado(Partida.EstadoPartida.ESPERANDO);
    }

    public Optional<Partida> obtenerPorId(Integer id) {
        return partidaRepository.findById(id);
    }

    public Optional<Partida> obtenerPorCodigo(String codigoPartida) {
        return partidaRepository.findByCodigoPartida(codigoPartida);
    }

    @Transactional
    public Partida crearPartida(Integer idTema, String idCreador, Partida detalles) {
        Tema tema = temaRepository.findById(idTema)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tema no encontrado"));
        Jugador creador = jugadorRepository.findById(idCreador)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Jugador creador no encontrado"));

        detalles.setTema(tema);
        detalles.setCreador(creador);
        detalles.setEstado(Partida.EstadoPartida.ESPERANDO);
        
        if (detalles.getCodigoPartida() == null || detalles.getCodigoPartida().isEmpty()) {
            String codigo;
            do {
                codigo = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
            } while (partidaRepository.existsByCodigoPartida(codigo));
            detalles.setCodigoPartida(codigo);
        }

        try {
            return partidaRepository.save(detalles);
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMostSpecificCause().getMessage());
        }
    }

    @Transactional
    public Partida comenzarPartida(Integer idPartida) {
        Partida partida = partidaRepository.findById(idPartida)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Partida no encontrada"));
                

        List<JugadorPartida> participantes = jugadorPartidaRepository.findByPartida_IdPartida(idPartida);
        if (participantes.size() < 4) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Se necesitan al menos 4 jugadores para empezar");
        }

        if (Partida.EstadoPartida.ESPERANDO != partida.getEstado()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La partida ya ha comenzado");
        }
        partida.setEstado(Partida.EstadoPartida.EN_CURSO);
        Partida partidaActualizada = partidaRepository.save(partida);

        // Sincronizado con la nueva firma de TableroCartaService
        tableroCartaService.generarTablero(partidaActualizada.getIdPartida());

        return partidaActualizada;
    }

    @Transactional
    public Partida actualizarEstado(Integer id, Partida.EstadoPartida nuevoEstado) {
        Partida partida = partidaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Partida no encontrada"));
                
        partida.setEstado(nuevoEstado); 
        if (Partida.EstadoPartida.FINALIZADA == nuevoEstado) {
            partida.setFechaFin(LocalDateTime.now());
        }
        
        try {
            return partidaRepository.save(partida);
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMostSpecificCause().getMessage());
        }
    }

    @Transactional
    public Partida declararGanador(Integer id, Boolean rojoGana) {
        Partida partida = partidaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Partida no encontrada"));
                
        partida.setRojoGana(rojoGana);
        partida.setEstado(Partida.EstadoPartida.FINALIZADA);
        partida.setFechaFin(LocalDateTime.now());
        
        try {
            return partidaRepository.save(partida);
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMostSpecificCause().getMessage());
        }
    }
}