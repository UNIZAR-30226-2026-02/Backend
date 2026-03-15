package com.secretpanda.codenames.mapper.social;

import java.util.List;
import java.util.stream.Collectors;
import com.secretpanda.codenames.dto.social.ChatMessageDTO;
import com.secretpanda.codenames.model.Chat;

// Mapper estático para la entidad Chat (necesita info de JugadorPartida y Jugador)
public class ChatMapper {

    private ChatMapper() {}

    public static ChatMessageDTO toDTO(Chat chat) {
        if (chat == null) return null;

        ChatMessageDTO dto = new ChatMessageDTO();
        // Ajustados a snake_case
        dto.setIdMensaje(chat.getIdMensaje());
        dto.setMensaje(chat.getMensaje());
        dto.setFecha(chat.getFecha());
        dto.setIdPartida(chat.getPartida().getIdPartida());
        dto.setIdJugador(chat.getJugadorPartida().getJugador().getIdGoogle());
        dto.setTag(chat.getJugadorPartida().getJugador().getTag());
        dto.setEquipo(chat.getJugadorPartida().getEquipo().name());

        return dto;
    }

    public static List<ChatMessageDTO> toDTOList(List<Chat> mensajes) {
        if (mensajes == null) return null;
        return mensajes.stream()
                .map(ChatMapper::toDTO)
                .collect(Collectors.toList());
    }
}