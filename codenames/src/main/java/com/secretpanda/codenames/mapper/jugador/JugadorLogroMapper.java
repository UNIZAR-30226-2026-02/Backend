package com.secretpanda.codenames.mapper.jugador;

import java.util.List;
import java.util.stream.Collectors;

import com.secretpanda.codenames.dto.tienda.LogroDTO;
import com.secretpanda.codenames.mapper.tienda.LogroMapper;
import com.secretpanda.codenames.model.JugadorLogro;

public class JugadorLogroMapper {

    private JugadorLogroMapper() {}

    /**
     * Crea un LogroDTO completo (Enriquecido) con el progreso del jugador.
     */
    public static LogroDTO toEnrichedDTO(JugadorLogro jl) {
        if (jl == null) return null;

        // Usamos el LogroMapper para traer los datos estáticos (Nombre, desc, recompensas...)
        LogroDTO dto = LogroMapper.toDTO(jl.getLogro());

        // Inyectamos los datos dinámicos del jugador
        dto.setProgresoActual(jl.getProgresoActual());
        dto.setCompletado(jl.isCompletado());

        return dto;
    }

    public static List<LogroDTO> toEnrichedDTOList(List<JugadorLogro> lista) {
        if (lista == null) return null;
        return lista.stream()
                .map(JugadorLogroMapper::toEnrichedDTO)
                .collect(Collectors.toList());
    }
}