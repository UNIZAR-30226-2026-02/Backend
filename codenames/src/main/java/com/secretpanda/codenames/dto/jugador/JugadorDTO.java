package com.secretpanda.codenames.dto.jugador;

import com.secretpanda.codenames.model.Jugador;

/**
 * DTO Maestro de Jugador.
 * Contiene tanto los datos persistentes como los campos calculados por el Util.
 */
public class JugadorDTO {

    // Datos de identidad y perfil
    private String idGoogle;
    private String tag;
    private String fotoPerfil;
    private int balas;

    // Estadísticas base (procedentes de BD)
    private int partidasJugadas;
    private int victorias;
    private int numAciertos;
    private int numFallos;

    // Campos calculados
    private int derrotas;
    private double porcentajeVictorias;

    public JugadorDTO() {
    }

    /**
     * Constructor de mapeo básico. 
     * Los campos de 'derrotas' y 'porcentajeVictorias' se rellenarán 
     * externamente mediante el Util de cálculos.
     */
    public JugadorDTO(Jugador jugador) {
        this.idGoogle = jugador.getIdGoogle();
        this.tag = jugador.getTag();
        this.fotoPerfil = jugador.getFotoPerfil();
        this.balas = jugador.getBalas();
        this.partidasJugadas = jugador.getPartidasJugadas();
        this.victorias = jugador.getVictorias();
        this.numAciertos = jugador.getNumAciertos();
        this.numFallos = jugador.getNumFallos();
    }

    // Getters
    public String getIdGoogle() { return idGoogle; }
    public String getTag() { return tag; }
    public String getFotoPerfil() { return fotoPerfil; }
    public int getBalas() { return balas; }
    public int getPartidasJugadas() { return partidasJugadas; }
    public int getVictorias() { return victorias; }
    public int getNumAciertos() { return numAciertos; }
    public int getNumFallos() { return numFallos; }
    public int getDerrotas() { return derrotas; }
    public double getPorcentajeVictorias() { return porcentajeVictorias; }

    // Setters
    public void setIdGoogle(String idGoogle) { this.idGoogle = idGoogle; }
    public void setTag(String tag) { this.tag = tag; }
    public void setFotoPerfil(String fotoPerfil) { this.fotoPerfil = fotoPerfil; }
    public void setBalas(int balas) { this.balas = balas; }
    public void setPartidasJugadas(int partidasJugadas) { this.partidasJugadas = partidasJugadas; }
    public void setVictorias(int victorias) { this.victorias = victorias; }
    public void setNumAciertos(int numAciertos) { this.numAciertos = numAciertos; }
    public void setNumFallos(int numFallos) { this.numFallos = numFallos; }
    public void setDerrotas(int derrotas) { this.derrotas = derrotas; }
    public void setPorcentajeVictorias(double porcentajeVictorias) { this.porcentajeVictorias = porcentajeVictorias; }
}