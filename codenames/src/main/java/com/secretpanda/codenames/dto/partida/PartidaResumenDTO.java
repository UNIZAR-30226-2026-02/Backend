package com.secretpanda.codenames.dto.partida;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

public class PartidaResumenDTO {

    @JsonProperty("id_partida")
    private Integer idPartida;
    @JsonProperty("codigo_partida")
    private String codigoPartida;
    @JsonProperty("fecha_fin")
    private LocalDateTime fechaFin;
    @JsonProperty("estado")
    private String estado;
    @JsonProperty("rojo_gana")
    private Boolean rojoGana;
    @JsonProperty("equipo")
    private String equipo;
    @JsonProperty("rol")
    private String rol;
    @JsonProperty("abandono")
    private boolean abandono;
    @JsonProperty("num_aciertos")
    private int numAciertos;
    @JsonProperty("num_fallos")
    private int numFallos;
    @JsonProperty("tag_creador")
    private String tagCreador;

    public PartidaResumenDTO() {
    }

    // --- GETTERS Y SETTERS ---

    public String getTagCreador() {
        return tagCreador;
    }

    public void setTagCreador(String tagCreador) {
        this.tagCreador = tagCreador;
    }

    public Integer getIdPartida() {
        return idPartida;
    }

    public void setIdPartida(Integer idPartida) {
        this.idPartida = idPartida;
    }

    public String getCodigoPartida() {
        return codigoPartida;
    }

    public void setCodigoPartida(String codigoPartida) {
        this.codigoPartida = codigoPartida;
    }

    public LocalDateTime getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDateTime fechaFin) {
        this.fechaFin = fechaFin;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Boolean getRojoGana() {
        return rojoGana;
    }

    public void setRojoGana(Boolean rojoGana) {
        this.rojoGana = rojoGana;
    }

    public String getEquipo() {
        return equipo;
    }

    public void setEquipo(String equipo) {
        this.equipo = equipo;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public boolean isAbandono() {
        return abandono;
    }

    public void setAbandono(boolean abandono) {
        this.abandono = abandono;
    }

    public int getNumAciertos() {
        return numAciertos;
    }

    public void setNumAciertos(int numAciertos) {
        this.numAciertos = numAciertos;
    }

    public int getNumFallos() {
        return numFallos;
    }

    public void setNumFallos(int numFallos) {
        this.numFallos = numFallos;
    }
}