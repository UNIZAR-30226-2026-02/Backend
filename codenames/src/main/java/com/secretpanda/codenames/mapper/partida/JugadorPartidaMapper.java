package com.secretpanda.codenames.mapper.partida;

import com.secretpanda.codenames.dto.partida.JugadorPartidaDTO;
import com.secretpanda.codenames.model.JugadorPartida;

import java.util.List;
import java.util.stream.Collectors;

// Mapper estático para la entidad JugadorPartida (tabla entre Jugador y Partida para coger datos de ambas)
public class JugadorPartidaMapper {

    private JugadorPartidaMapper() {}

    // Pasamos de JugadorPartida al DTO (para el lobby o la lista de participantes)
    public static JugadorPartidaDTO toDTO(JugadorPartida jp) {
        if (jp == null) return null;

        JugadorPartidaDTO dto = new JugadorPartidaDTO();
        
        // Identificador (snake_case)
        dto.setId_jugador_partida(jp.getIdJugadorPartida());

        // Datos del jugador (snake_case)
        dto.setId_jugador(jp.getJugador().getIdGoogle());
        dto.setTag_jugador(jp.getJugador().getTag());
        dto.setFoto_jugador(jp.getJugador().getFotoPerfil());

        // Estado del jugador en la partida (snake_case)
        dto.setEquipo(jp.getEquipo().name());
        dto.setRol(jp.getRol().name());
        dto.setNum_aciertos(jp.getNumAciertos());
        dto.setNum_fallos(jp.getNumFallos());
        dto.setAbandono(jp.isAbandono());

        return dto;
    }

    // Conversión de lista
    public static List<JugadorPartidaDTO> toDTOList(List<JugadorPartida> participantes) {
        if (participantes == null) return null; // Seguro contra nulos
        
        return participantes.stream()
                .map(JugadorPartidaMapper::toDTO)
                .collect(Collectors.toList());
    }
}