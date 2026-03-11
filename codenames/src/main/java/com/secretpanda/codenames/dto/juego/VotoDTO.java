package com.secretpanda.codenames.dto.juego;

import com.secretpanda.codenames.model.VotoCarta;

public class VotoDTO {
    
    private Integer idCarta; //Carta votada
    private String tagJugador; // "nickname" del jugador que ha votado
    private String equipo; // El equipo del jugador que ha votado

    // Constructor vacío
    public VotoDTO() {}

    // Constructor con parámetros para coger los datos del voto
    public VotoDTO(VotoCarta voto) {
        this.idCarta = voto.getCartaTablero().getIdCartaTablero();
        this.tagJugador = voto.getJugadorPartida().getJugador().getTag();
        this.equipo = voto.getJugadorPartida().getEquipo().name();
    }

    // Getters y Setters
    public Integer getIdCarta() { return idCarta; }
    public void setIdCarta(Integer idCarta) { this.idCarta = idCarta; }

    public String getTagJugador() { return tagJugador; }
    public void setTagJugador(String tagJugador) { this.tagJugador = tagJugador; }

    public String getEquipo() { return equipo; }
    public void setEquipo(String equipo) { this.equipo = equipo; }
}