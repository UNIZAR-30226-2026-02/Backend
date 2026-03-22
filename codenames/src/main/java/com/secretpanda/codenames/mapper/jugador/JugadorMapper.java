package com.secretpanda.codenames.mapper.jugador;

import java.util.List;
import java.util.stream.Collectors;

import com.secretpanda.codenames.dto.jugador.ActualizarPerfilDTO;
import com.secretpanda.codenames.dto.jugador.JugadorDTO;
import com.secretpanda.codenames.dto.social.RankingDTO;
import com.secretpanda.codenames.model.Jugador;
import com.secretpanda.codenames.util.EstadisticasCalculator;

// Mapper estático para la entidad Jugador.
public class JugadorMapper {

    private JugadorMapper() {}

    // Convertimos el jugador en el DTO Maestro (JugadorDTO) con TODOS sus datos y estadísticas
    // Inyectamos el EstadisticasCalculator que creamos previamente.
    public static JugadorDTO toDTO(Jugador jugador, EstadisticasCalculator calculator) {
        if (jugador == null) return null;

        JugadorDTO dto = new JugadorDTO();
        
        // Identidad (Entidad en camelCase -> DTO en snake_case)
        dto.setIdGoogle(jugador.getIdGoogle());
        dto.setTag(jugador.getTag());
        dto.setFotoPerfil(jugador.getFotoPerfil());
        dto.setBalas(jugador.getBalas());
        dto.setActivo(jugador.isActivo());

        // Estadísticas base procedentes de la BD
        dto.setPartidasJugadas(jugador.getPartidasJugadas());
        dto.setVictorias(jugador.getVictorias());
        dto.setNumAciertos(jugador.getNumAciertos());
        dto.setNumFallos(jugador.getNumFallos());

        // Cálculos delegados a nuestra clase Util
        if (calculator != null) {
            dto.setDerrotas(calculator.calcularDerrotas(jugador.getPartidasJugadas(), jugador.getVictorias()));
            dto.setPorcentajeVictorias(calculator.calcularWinrate(jugador.getVictorias(), jugador.getPartidasJugadas()));
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

    // Para aplicar cambios sobre un jugador existente (recibimos el DTO y actualizamos la Entidad)
    public static void applyUpdateDTO(ActualizarPerfilDTO dto, Jugador jugador) {
        if (dto == null || jugador == null) return;

        if (dto.getTag() != null) {
            jugador.setTag(dto.getTag());
        }
        if (dto.getFotoPerfil() != null) {
            jugador.setFotoPerfil(dto.getFotoPerfil());
        }
    }

    // Conversores de lista de jugadores a lista de DTOs maestros
    public static List<JugadorDTO> toDTOList(List<Jugador> jugadores, EstadisticasCalculator calculator) {
        if (jugadores == null) return null;
        return jugadores.stream()
                .map(j -> toDTO(j, calculator))
                .collect(Collectors.toList());
    }

    // Conversores para el Ranking
    public static List<RankingDTO> toRankingDTOList(List<Jugador> jugadores) {
        if (jugadores == null) return null;
        return jugadores.stream()
                .map(JugadorMapper::toRankingDTO)
                .collect(Collectors.toList());
    }
}