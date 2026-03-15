package com.secretpanda.codenames.service;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.secretpanda.codenames.dto.social.ChatMessageDTO;
import com.secretpanda.codenames.dto.social.EnviarMensajeDTO;
import com.secretpanda.codenames.exception.BadRequestException;
import com.secretpanda.codenames.exception.NotFoundException;
import com.secretpanda.codenames.mapper.social.ChatMapper;
import com.secretpanda.codenames.model.Chat;
import com.secretpanda.codenames.model.JugadorPartida;
import com.secretpanda.codenames.model.Partida;
import com.secretpanda.codenames.repository.ChatRepository;
import com.secretpanda.codenames.repository.JugadorPartidaRepository;
import com.secretpanda.codenames.repository.PartidaRepository;

@Service
public class ChatService {

    private final ChatRepository chatRepository;
    private final PartidaRepository partidaRepository;
    private final JugadorPartidaRepository jugadorPartidaRepository;

    // Lista básica de palabras prohibidas (simulación del filtro anti-toxicidad)
    private static final List<String> PALABRAS_PROHIBIDAS = Arrays.asList("insulto1", "insulto2", "palabrota");

    public ChatService(ChatRepository chatRepository, PartidaRepository partidaRepository, JugadorPartidaRepository jugadorPartidaRepository) {
        this.chatRepository = chatRepository;
        this.partidaRepository = partidaRepository;
        this.jugadorPartidaRepository = jugadorPartidaRepository;
    }

    @Transactional
    public ChatMessageDTO procesarMensaje(EnviarMensajeDTO dto, String idGoogleJugador) {
        if (dto.getMensaje() == null || dto.getMensaje().trim().isEmpty()) {
            throw new BadRequestException("El mensaje no puede estar vacío");
        }

        // 1. Filtro de palabras prohibidas (Contrato de API: Sección 2.4)
        String mensajeFiltrado = aplicarFiltroOfensivo(dto.getMensaje());

        Partida partida = partidaRepository.findById(dto.getIdPartida())
                .orElseThrow(() -> new NotFoundException("Partida no encontrada"));

        // Asegurarnos de que el jugador que envía el mensaje realmente está en esa partida
        JugadorPartida jp = jugadorPartidaRepository.findByJugador_IdGoogleAndPartida_IdPartida(idGoogleJugador, partida.getIdPartida())
                .orElseThrow(() -> new BadRequestException("No perteneces a esta partida"));

        // 2. Inserta el mensaje en la tabla CHAT
        Chat chat = new Chat();
        chat.setPartida(partida);
        chat.setJugadorPartida(jp);
        chat.setMensaje(mensajeFiltrado);

        chat = chatRepository.save(chat);

        // Devolvemos el DTO para que el Controlador (WebSocket) lo difunda al topic del equipo
        return ChatMapper.toDTO(chat);
    }

    /**
     * Reemplaza las palabras ofensivas por asteriscos para mantener un entorno de juego seguro.
     */
    private String aplicarFiltroOfensivo(String mensaje) {
        String mensajeLimpio = mensaje;
        for (String palabra : PALABRAS_PROHIBIDAS) {
            // Reemplaza las palabras ignorando mayúsculas/minúsculas con asteriscos
            mensajeLimpio = mensajeLimpio.replaceAll("(?i)" + palabra, "***");
        }
        return mensajeLimpio;
    }
}