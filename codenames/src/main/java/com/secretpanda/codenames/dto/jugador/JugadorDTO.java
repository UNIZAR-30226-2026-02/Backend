package com.secretpanda.codenames.dto.jugador;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.secretpanda.codenames.model.Jugador;

/**
 * DTO Maestro de Jugador.
 * Contiene datos persistentes, campos calculados y configuración visual equipada.
 */
public class JugadorDTO {

    @JsonProperty("id_google")
    private String idGoogle;
    @JsonProperty("tag")
    private String tag;
    @JsonProperty("foto_perfil")
    private String fotoPerfil;
    @JsonProperty("balas")
    private int balas;
    @JsonProperty("activo")
    private boolean activo;

    @JsonProperty("partidas_jugadas")
    private int partidasJugadas;
    @JsonProperty("victorias")
    private int victorias;
    @JsonProperty("num_aciertos")
    private int numAciertos;
    @JsonProperty("num_fallos")
    private int numFallos;

    @JsonProperty("derrotas")
    private int derrotas;
    @JsonProperty("porcentaje_victorias")
    private double porcentajeVictorias;

    @JsonProperty("marco_carta_equipado")
    private String marcoCartaEquipado;
    @JsonProperty("fondo_tablero_equipado")
    private String fondoTableroEquipado;

    @JsonProperty("partida_activa_id")
    private Integer partidaActivaId;

    public JugadorDTO() {
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

    public Integer getPartidaActivaId() {
        return partidaActivaId;
    }

    public void setPartidaActivaId(Integer partidaActivaId) {
        this.partidaActivaId = partidaActivaId;
    }
}