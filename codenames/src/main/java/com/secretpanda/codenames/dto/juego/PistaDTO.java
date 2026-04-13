package com.secretpanda.codenames.dto.juego;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.secretpanda.codenames.model.Turno;

public class PistaDTO {

    @JsonProperty("palabra_pista")
    private String palabraPista;

    @JsonProperty("pista_numero")
    private int pistaNumero;

    @JsonProperty("equipo_lider")
    private String equipoLider;

    @JsonProperty("aciertos_turno")
    private int aciertosTurno;

    // Constructor vacío
    public PistaDTO() {}

    // Constructor con parámetros para coger los datos del turno
    public PistaDTO(Turno turno) {
        this.palabraPista = turno.getPalabraPista(); 
        this.pistaNumero = turno.getPistaNumero();
        this.equipoLider = turno.getJugadorPartida().getEquipo().name();
    }

    // Getters y Setters
    public String getPalabraPista() { return palabraPista; } 
    public void setPalabraPista(String palabraPista) { this.palabraPista = palabraPista; }

    public int getPistaNumero() { return pistaNumero; } 
    public void setPistaNumero(int pistaNumero) { this.pistaNumero = pistaNumero; }

    public String getEquipoLider() { return equipoLider; }
    public void setEquipoLider(String equipoLider) { this.equipoLider = equipoLider; }

    public int getAciertosTurno() { return aciertosTurno; }
    public void setAciertosTurno(int aciertosTurno) { this.aciertosTurno = aciertosTurno; }
}