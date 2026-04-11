package com.secretpanda.codenames.dto.jugador;

import com.secretpanda.codenames.model.Jugador;

/**
 * DTO Maestro de Jugador.
 * Contiene datos persistentes, campos calculados y configuración visual equipada.
 */
public class JugadorDTO {

    private String idGoogle;
    private String tag;
    private String fotoPerfil;
    private int balas;
    private boolean activo;

    private int partidasJugadas;
    private int victorias;
    private int numAciertos;
    private int numFallos;

    private int derrotas;
    private double porcentajeVictorias;

    private String marcoCartaEquipado;
    private String fondoTableroEquipado;

    public JugadorDTO() {
    }

    public JugadorDTO(Jugador jugador) {
        this.idGoogle = jugador.getIdGoogle();
        this.tag = jugador.getTag();
        this.fotoPerfil = jugador.getFotoPerfil();
        this.balas = jugador.getBalas();
        this.activo = jugador.isActivo();
        this.partidasJugadas = jugador.getPartidasJugadas();
        this.victorias = jugador.getVictorias();
        this.numAciertos = jugador.getNumAciertos();
        this.numFallos = jugador.getNumFallos();
    }

    // --- GETTERS ---

    public String getIdGoogle() {
        return idGoogle;
    }

    public String getTag() {
        return tag;
    }

    public String getFotoPerfil() {
        return fotoPerfil;
    }

    public int getBalas() {
        return balas;
    }

    public boolean isActivo() {
        return activo;
    }

    public int getPartidasJugadas() {
        return partidasJugadas;
    }

    public int getVictorias() {
        return victorias;
    }

    public int getNumAciertos() {
        return numAciertos;
    }

    public int getNumFallos() {
        return numFallos;
    }

    public int getDerrotas() {
        return derrotas;
    }

    public double getPorcentajeVictorias() {
        return porcentajeVictorias;
    }

    public String getMarcoCartaEquipado() {
        return marcoCartaEquipado;
    }

    public String getFondoTableroEquipado() {
        return fondoTableroEquipado;
    }

    // --- SETTERS ---

    public void setIdGoogle(String idGoogle) {
        this.idGoogle = idGoogle;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public void setFotoPerfil(String fotoPerfil) {
        this.fotoPerfil = fotoPerfil;
    }

    public void setBalas(int balas) {
        this.balas = balas;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public void setPartidasJugadas(int partidasJugadas) {
        this.partidasJugadas = partidasJugadas;
    }

    public void setVictorias(int victorias) {
        this.victorias = victorias;
    }

    public void setNumAciertos(int numAciertos) {
        this.numAciertos = numAciertos;
    }

    public void setNumFallos(int numFallos) {
        this.numFallos = numFallos;
    }

    public void setDerrotas(int derrotas) {
        this.derrotas = derrotas;
    }

    public void setPorcentajeVictorias(double porcentajeVictorias) {
        this.porcentajeVictorias = porcentajeVictorias;
    }

    public void setMarcoCartaEquipado(String marcoCartaEquipado) {
        this.marcoCartaEquipado = marcoCartaEquipado;
    }

    public void setFondoTableroEquipado(String fondoTableroEquipado) {
        this.fondoTableroEquipado = fondoTableroEquipado;
    }
}