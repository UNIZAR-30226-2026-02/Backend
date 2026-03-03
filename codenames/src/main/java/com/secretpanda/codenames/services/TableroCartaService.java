package com.secretpanda.codenames.services;

import com.secretpanda.codenames.models.*;
import com.secretpanda.codenames.repositories.TableroCartaRepository;
import com.secretpanda.codenames.repositories.PartidaRepository;
import com.secretpanda.codenames.repositories.PalabraTemaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class TableroCartaService {

    @Autowired
    private TableroCartaRepository tableroCartaRepository;

    @Autowired
    private PartidaRepository partidaRepository;

    @Autowired
    private PalabraTemaRepository palabraTemaRepository;

    public List<TableroCarta> obtenerTableroDePartida(Integer idPartida) {
        return tableroCartaRepository.findByPartida_IdPartida(idPartida);
    }

    public Optional<TableroCarta> obtenerCartaPorId(Integer idCartaTablero) {
        return tableroCartaRepository.findById(idCartaTablero);
    }

    @Transactional
    public List<TableroCarta> generarTablero(Integer idPartida) {
        Partida partida = partidaRepository.findById(idPartida)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Partida no encontrada"));

        // 1. Obtener palabras del tema
        List<PalabraTema> palabras = palabraTemaRepository.findByTema_IdTema(partida.getTema().getIdTema());
        if (palabras.size() < 25) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El tema no tiene suficientes palabras (mínimo 25)");
        }

        // 2. Barajar y seleccionar 25
        Collections.shuffle(palabras);
        List<PalabraTema> seleccionadas = palabras.subList(0, 25);

        // 3. Preparar los tipos de carta (9 rojo, 8 azul, 7 civil, 1 asesino)
        List<String> tipos = new ArrayList<>();
        for (int i = 0; i < 9; i++) tipos.add("rojo");
        for (int i = 0; i < 8; i++) tipos.add("azul");
        for (int i = 0; i < 7; i++) tipos.add("civil");
        tipos.add("asesino");
        Collections.shuffle(tipos);

        // 4. Crear y guardar el tablero 5x5
        List<TableroCarta> tablero = new ArrayList<>();
        int index = 0;
        for (int f = 0; f < 5; f++) {
            for (int c = 0; c < 5; c++) {
                TableroCarta carta = new TableroCarta();
                carta.setPartida(partida);
                carta.setPalabra(seleccionadas.get(index));
                carta.setFila(f);
                carta.setColumna(c);
                carta.setEstado("oculta");
                carta.setTipo(tipos.get(index));
                tablero.add(carta);
                index++;
            }
        }

        try {
            return tableroCartaRepository.saveAll(tablero);
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error al guardar el tablero: " + ex.getMostSpecificCause().getMessage());
        }
    }

    @Transactional
    public TableroCarta revelarCarta(Integer idCartaTablero, String nuevoEstado) {
        TableroCarta carta = tableroCartaRepository.findById(idCartaTablero)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Carta no encontrada"));
        
        carta.setEstado(nuevoEstado);
        
        try {
            return tableroCartaRepository.save(carta);
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMostSpecificCause().getMessage());
        }
    }
}