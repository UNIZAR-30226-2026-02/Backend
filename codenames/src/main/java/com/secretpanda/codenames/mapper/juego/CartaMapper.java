package com.secretpanda.codenames.mapper.juego;

import com.secretpanda.codenames.dto.juego.CartaDTO;
import com.secretpanda.codenames.model.TableroCarta;
import com.secretpanda.codenames.model.TableroCarta.EstadoCarta;

import java.util.List;
import java.util.stream.Collectors;

// Mapper para la entidad TableroCarta.
public class CartaMapper {

    private CartaMapper() {}

    public static CartaDTO toDTO(TableroCarta carta, boolean esLider) {
        if (carta == null) return null;

        CartaDTO dto = new CartaDTO();
        dto.setIdCarta(carta.getIdCartaTablero());
        dto.setPalabra(carta.getPalabra().getValor());
        dto.setFila(carta.getFila());
        dto.setColumna(carta.getColumna());
        dto.setEstado(carta.getEstado().name());

        // El tipo solo lo revelamos al líder o si la carta ya está revelada (es importante que lo controlemos)
        boolean cartaRevelada = EstadoCarta.revelada.equals(carta.getEstado());
        if (esLider || cartaRevelada) {
            dto.setTipo(carta.getTipo().name());
        } else {
            dto.setTipo(null); // El agente no puede ver el tipo de cartas ocultas
        }

        return dto;
    }

    // Convertimos la lista de TableroCarta a CartaDTO para controlar la visibilidad de las cartas
    public static List<CartaDTO> toDTOList(List<TableroCarta> cartas, boolean esLider) {
        return cartas.stream()
                .map(carta -> toDTO(carta, esLider))
                .collect(Collectors.toList());
    }
}