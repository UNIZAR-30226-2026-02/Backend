package com.secretpanda.codenames.mapper.juego;

import com.secretpanda.codenames.dto.juego.CartaDTO;
import com.secretpanda.codenames.dto.juego.TableroDTO;
import com.secretpanda.codenames.model.TableroCarta;

import java.util.List;

// Mapper estático para construir el TableroDTO completo

public class TableroMapper {

    private TableroMapper() {}

    // PAsamos de una lista de cartas (con toda la info) a un DTO del tablero con una lista de cartas con su info según el rol
    public static TableroDTO toDTO(List<TableroCarta> cartas, boolean esLider) {
        if (cartas == null) return null;

        List<CartaDTO> cartasDTO = CartaMapper.toDTOList(cartas, esLider);
        return new TableroDTO(cartasDTO);
    }
}
