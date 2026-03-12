package com.secretpanda.codenames.mapper.jugador;

import com.secretpanda.codenames.dto.jugador.ActualizarPerfilDTO;
import com.secretpanda.codenames.dto.jugador.JugadorDTO;
import com.secretpanda.codenames.dto.jugador.JugadorStatsDTO;
import com.secretpanda.codenames.dto.social.RankingDTO;
import com.secretpanda.codenames.model.Jugador;

import java.util.List;
import java.util.stream.Collectors;

// Mapper estático para la entidad Jugador.
public class JugadorMapper {

    private JugadorMapper() {}

    // Convertimos el jugador en un DTO (JugadorDTO) solo con los datos del Id, del tag y la foto de perfil
    public static JugadorDTO toDTO(Jugador jugador) {
        if (jugador == null) return null;

        JugadorDTO dto = new JugadorDTO();
        dto.setIdGoogle(jugador.getIdGoogle());
        dto.setTag(jugador.getTag());
        dto.setFotoPerfil(jugador.getFotoPerfil());
        return dto;
    }

    // Convertimos el jugador en un DTO pero esta vez con sus estádisticas (las mostraremos en el perfil)
    public static JugadorStatsDTO toStatsDTO(Jugador jugador) {
        if (jugador == null) return null;

        JugadorStatsDTO dto = new JugadorStatsDTO();
        dto.setTag(jugador.getTag());
        dto.setFotoPerfil(jugador.getFotoPerfil());
        dto.setPartidasJugadas(jugador.getPartidasJugadas());
        dto.setVictorias(jugador.getVictorias());
        dto.setNumAciertos(jugador.getNumAciertos());
        dto.setNumFallos(jugador.getNumFallos());

        // Datos calculados (no los alamacenamos en la BD)
        int derrotas = jugador.getPartidasJugadas() - jugador.getVictorias();
        dto.setDerrotas(Math.max(derrotas, 0));

        if (jugador.getPartidasJugadas() > 0) {
            double ratio = ((double) jugador.getVictorias() / jugador.getPartidasJugadas()) * 100;
            dto.setPorcentajeVictorias(Math.round(ratio * 100.0) / 100.0);
        } else {
            dto.setPorcentajeVictorias(0.0);
        }

        return dto;
    }

    // Convierte el jugador en un DTO para mostrarlo en la tabla de clasificación global
    public static RankingDTO toRankingDTO(Jugador jugador) {
        if (jugador == null) return null;

        RankingDTO dto = new RankingDTO();
        dto.setTag(jugador.getTag());
        dto.setFotoPerfil(jugador.getFotoPerfil());
        dto.setVictorias(jugador.getVictorias());
        return dto;
    }

    // Para aplicar cambios sobre un jugador existente (recibimos el DTO)
    public static void applyUpdateDTO(ActualizarPerfilDTO dto, Jugador jugador) {
        if (dto == null || jugador == null) return;

        if (dto.getTag() != null) {
            jugador.setTag(dto.getTag());
        }
        if (dto.getFotoPerfil() != null) {
            jugador.setFotoPerfil(dto.getFotoPerfil());
        }
    }

    // Conversores de lista de jugadores a lista de DTOs (para el ranking o para los jugadores del lobby)
    public static List<JugadorDTO> toDTOList(List<Jugador> jugadores) {
        return jugadores.stream()
                .map(JugadorMapper::toDTO)
                .collect(Collectors.toList());
    }

    public static List<RankingDTO> toRankingDTOList(List<Jugador> jugadores) {
        return jugadores.stream()
                .map(JugadorMapper::toRankingDTO)
                .collect(Collectors.toList());
    }
}