package com.secretpanda.codenames.dto.juego;

import com.secretpanda.codenames.model.Turno;

public class PistaDTO {
    
    private String palabraPista;
    private int pistaNumero;
    private String equipoLider;

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
}