package com.secretpanda.codenames.mapper.jugador;

import com.secretpanda.codenames.dto.tienda.LogroDTO;
import com.secretpanda.codenames.mapper.tienda.LogroMapper;
import com.secretpanda.codenames.model.JugadorLogro;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper estático para la entidad JugadorLogro
 *
 * Tabla intermedia entre Jugador y Logro que
 * registra el progreso de cada jugador en cada logro
 *
 * Reutilizamos LogroDTO en lugar de crear un DTO separado para tener una vista unificada.
 */
public class JugadorLogroMapper {

    private JugadorLogroMapper() {}

    // Añadimos la info del progreso del jugador en el logro
    public static LogroDTO toEnrichedDTO(JugadorLogro jugadorLogro) {
        if (jugadorLogro == null) return null;

        // Cargamos los datos estáticos de la medalla (nombre, descripción, objetivo...)
        LogroDTO dto = LogroMapper.toDTO(jugadorLogro.getLogro());

        // INYECTAMOS el progreso del jugador (¡esto es lo que faltaba!)
        dto.setProgresoActual(jugadorLogro.getProgresoActual());
        dto.setCompletado(jugadorLogro.isCompletado());
        dto.setFechaDesbloqueo(jugadorLogro.getFechaDesbloqueo());

        return dto;
    }

    // Convertimos la lista de JugadorLogro a una lista de LogroDTO enriquecidos
    public static List<LogroDTO> toEnrichedDTOList(List<JugadorLogro> jugadorLogros) {
        if (jugadorLogros == null) return null; // Pequeño seguro extra por si acaso
        
        return jugadorLogros.stream()
                .map(JugadorLogroMapper::toEnrichedDTO)
                .collect(Collectors.toList());
    }
}