package com.secretpanda.codenames.dto.juego;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class TableroDTO {
    // Listas de las cartas del tablero
    @JsonProperty("cartas")
    private List<CartaDTO> cartas;

    // Constructor vacío
    public TableroDTO() {}

    // Constructor con parámetros para coger las cartas del tablero
    public TableroDTO(List<CartaDTO> cartas) {
        this.cartas = cartas;
    }

    // Getters y Setters
    public List<CartaDTO> getCartas() { return cartas; }
    public void setCartas(List<CartaDTO> cartas) { this.cartas = cartas; }
}
