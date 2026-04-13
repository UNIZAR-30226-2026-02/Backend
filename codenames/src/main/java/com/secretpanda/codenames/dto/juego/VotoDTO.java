package com.secretpanda.codenames.dto.juego;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.secretpanda.codenames.model.VotoCarta;

public class VotoDTO {
    
    @JsonProperty("id_carta_tablero")
    private Integer idCartaTablero; //Carta votada
    @JsonProperty("tag")
    private String tag; // "nickname" del jugador que ha votado
    @JsonProperty("equipo")
    private String equipo; // El equipo del jugador que ha votado

    // Constructor vacío
    public VotoDTO() {}

    // Constructor con parámetros para coger los datos del voto
    public VotoDTO(VotoCarta voto) {
        this.idCartaTablero = voto.getCartaTablero().getIdCartaTablero();
        this.tag = voto.getJugadorPartida().getJugador().getTag();
        this.equipo = voto.getJugadorPartida().getEquipo().name();
    }

    // Getters y Setters
    public Integer getIdCartaTablero() { return idCartaTablero; }
    public void setIdCartaTablero(Integer idCartaTablero) { this.idCartaTablero = idCartaTablero; }

    public String getTag() { return tag; }
    public void setTag(String tag) { this.tag = tag; }

    public String getEquipo() { return equipo; }
    public void setEquipo(String equipo) { this.equipo = equipo; }
}