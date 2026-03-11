package com.secretpanda.codenames.dto.juego;

import com.secretpanda.codenames.model.Turno;

public class PistaDTO {
    
    private String palabra;
    private int numero;
    private String equipoLider;

    // Constructor vacío
    public PistaDTO() {}

    // Constructor con parámetros para coger los datos del turno
    public PistaDTO(Turno turno) {
        this.palabra = turno.getPalabraPista();
        this.numero = turno.getPistaNumero();
        this.equipoLider = turno.getJugadorPartida().getEquipo().name();
    }

    // Getters y Setters
    public String getPalabra() { return palabra; }
    public void setPalabra(String palabra) { this.palabra = palabra; }

    public int getNumero() { return numero; }
    public void setNumero(int numero) { this.numero = numero; }

    public String getEquipoLider() { return equipoLider; }
    public void setEquipoLider(String equipoLider) { this.equipoLider = equipoLider; }
}
