package com.secretpanda.codenames.mapper.social;

import com.secretpanda.codenames.dto.social.ChatMessageDTO;
import com.secretpanda.codenames.model.Chat;

import java.util.List;
import java.util.stream.Collectors;


// Mapper estático para la entidad Chat (necesita info de JugadorPartida y Jugador)
public class ChatMapper {

    private ChatMapper() {}

    // Convierte el chat en el DTO con la info que hace falta para mostrar el mensaje (en el lobby o dentro de la partida)
    public static ChatMessageDTO toDTO(Chat chat) {
        if (chat == null) return null;

        ChatMessageDTO dto = new ChatMessageDTO();
        dto.setIdMensaje(chat.getIdMensaje());
        dto.setMensaje(chat.getMensaje());
        dto.setFecha(chat.getFecha());

        dto.setIdPartida(chat.getPartida().getIdPartida());

        dto.setIdJugador(chat.getJugadorPartida().getJugador().getIdGoogle());
        dto.setTagJugador(chat.getJugadorPartida().getJugador().getTag());

        dto.setEquipo(chat.getJugadorPartida().getEquipo().name());

        return dto;
    }

    // Conversión de lista para el chat completo
    public static List<ChatMessageDTO> toDTOList(List<Chat> mensajes) {
        return mensajes.stream()
                .map(ChatMapper::toDTO)
                .collect(Collectors.toList());
    }
}