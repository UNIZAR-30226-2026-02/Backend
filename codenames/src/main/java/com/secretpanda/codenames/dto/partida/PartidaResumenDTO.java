package com.secretpanda.codenames.dto.partida;

import java.time.LocalDateTime;

public class PartidaResumenDTO {

    private Integer idPartida;
    private String codigoPartida;
    private LocalDateTime fechaFin;
    private String estado;
    private Boolean rojoGana;
    private String equipo;
    private String rol;
    private boolean abandono;
    private int numAciertos;
    private int numFallos;

    public PartidaResumenDTO() {
    }

    // --- GETTERS Y SETTERS ---

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