package com.secretpanda.codenames.dto.jugador;

import com.secretpanda.codenames.model.Jugador;

public class JugadorStatsDTO {
    
    private String tag;
    private String fotoPerfil;
    
    // Estadísticas
    private int partidasJugadas;
    private int victorias;
    private int derrotas;
    
    // Métricas de rendimiento
    private double porcentajeVictorias;
    private int numAciertos;
    private int numFallos;

    // Constructor vacío
    public JugadorStatsDTO() {}

    // Constructor vacío para coger los datos del jugador y calcular las estadísticas
    public JugadorStatsDTO(Jugador jugador) {
        this.tag = jugador.getTag();
        this.fotoPerfil = jugador.getFotoPerfil();
        this.partidasJugadas = jugador.getPartidasJugadas();
        this.victorias = jugador.getVictorias();
        this.numAciertos = jugador.getNumAciertos();
        this.numFallos = jugador.getNumFallos();
        
        // Calculamos las derrotas y el porcentaje de victorias
        this.derrotas = this.partidasJugadas - this.victorias;
        
        if (this.partidasJugadas > 0) {
            // Usamos 2 decimales
            double ratio = ((double) this.victorias / this.partidasJugadas) * 100;
            this.porcentajeVictorias = Math.round(ratio * 100.0) / 100.0;
        } else {
            this.porcentajeVictorias = 0.0;
        }
    }

    // Getters y Setters
    public String getTag() { return tag; }
    public void setTag(String tag) { this.tag = tag; }

    public String getFotoPerfil() { return fotoPerfil; }
    public void setFotoPerfil(String fotoPerfil) { this.fotoPerfil = fotoPerfil; }

    public int getPartidasJugadas() { return partidasJugadas; }
    public void setPartidasJugadas(int partidasJugadas) { this.partidasJugadas = partidasJugadas; }

    public int getVictorias() { return victorias; }
    public void setVictorias(int victorias) { this.victorias = victorias; }

    public int getDerrotas() { return derrotas; }
    public void setDerrotas(int derrotas) { this.derrotas = derrotas; }

    public double getPorcentajeVictorias() { return porcentajeVictorias; }
    public void setPorcentajeVictorias(double porcentajeVictorias) { this.porcentajeVictorias = porcentajeVictorias; }

    public int getNumAciertos() { return numAciertos; }
    public void setNumAciertos(int numAciertos) { this.numAciertos = numAciertos; }

    public int getNumFallos() { return numFallos; }
    public void setNumFallos(int numFallos) { this.numFallos = numFallos; }
}
