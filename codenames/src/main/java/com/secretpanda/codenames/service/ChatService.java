package com.secretpanda.codenames.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.secretpanda.codenames.model.Chat;
import com.secretpanda.codenames.model.JugadorPartida;
import com.secretpanda.codenames.model.Partida;
import com.secretpanda.codenames.repository.ChatRepository;
import com.secretpanda.codenames.repository.JugadorPartidaRepository;
import com.secretpanda.codenames.repository.PartidaRepository;

@Service
public class ChatService {

    @Autowired private ChatRepository chatRepository;
    @Autowired private PartidaRepository partidaRepository;
    @Autowired private JugadorPartidaRepository jugadorPartidaRepository;

    public List<Chat> obtenerMensajesDePartida(Integer idPartida) {
        return chatRepository.findByPartida_IdPartidaOrderByFechaAsc(idPartida);
    }

    @Transactional
    public Chat enviarMensaje(Integer idPartida, Integer idJugadorPartida, String contenido) {
        Partida partida = partidaRepository.findById(idPartida)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Partida no encontrada"));
        JugadorPartida jugador = jugadorPartidaRepository.findById(idJugadorPartida)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Jugador no encontrado en la partida"));

        Chat mensaje = new Chat();
        mensaje.setPartida(partida);
        mensaje.setJugadorPartida(jugador);
        mensaje.setMensaje(contenido);

        try {
            // El trigger fn_control_chat_equivocado asegurará que el jugador realmente esté en la partida indicada.
            return chatRepository.save(mensaje);
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMostSpecificCause().getMessage());
        }
    }
}