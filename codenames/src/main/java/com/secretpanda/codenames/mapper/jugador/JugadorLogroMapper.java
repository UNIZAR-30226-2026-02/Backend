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
 * Vamos a reutilizar LogroDTO en lugar de crear un DTO separado (solo necesitaremos una vista del logro)
 */
public class JugadorLogroMapper {

    private JugadorLogroMapper() {}

    // Añadimos la info del progreso del jugador en el logro
    public static LogroDTO toEnrichedDTO(JugadorLogro jugadorLogro) {
        if (jugadorLogro == null) return null;

        LogroDTO dto = LogroMapper.toDTO(jugadorLogro.getLogro());

        return dto;
    }

    // Convertimos la lista de JugadorLogro a una lista de LogroDTO (al que hemos añadido el progreso del jugador)
    public static List<LogroDTO> toEnrichedDTOList(List<JugadorLogro> jugadorLogros) {
        return jugadorLogros.stream()
                .map(JugadorLogroMapper::toEnrichedDTO)
                .collect(Collectors.toList());
    }
}
