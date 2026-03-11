package com.secretpanda.codenames.dto.partida;

import java.time.LocalDateTime;

/**
 * DTO para mostrar un resumen de una partida finalizada, 
 * ideal para el historial de partidas de un jugador.
 */
public class PartidaResumenDTO {

    private Integer idPartida;
    private String nombreTema;
    private LocalDateTime fechaFin;
    
    // Datos específicos de lo que hizo el jugador en esta partida
    private String equipoJugador; // "rojo" o "azul"
    private String rolJugador;    // "lider" o "agente"
    private boolean victoria;     
    
    // Estadísticas del jugador en esa partida concreta
    private int numAciertos;
    private int numFallos;

    public PartidaResumenDTO() {
    }

    public Integer getIdPartida() {
        return idPartida;
    }

    public void setIdPartida(Integer idPartida) {
        this.idPartida = idPartida;
    }

    public String getNombreTema() {
        return nombreTema;
    }

    public void setNombreTema(String nombreTema) {
        this.nombreTema = nombreTema;
    }

    public LocalDateTime getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDateTime fechaFin) {
        this.fechaFin = fechaFin;
    }

    public String getEquipoJugador() {
        return equipoJugador;
    }

    public void setEquipoJugador(String equipoJugador) {
        this.equipoJugador = equipoJugador;
    }

    public String getRolJugador() {
        return rolJugador;
    }

    public void setRolJugador(String rolJugador) {
        this.rolJugador = rolJugador;
    }

    public boolean isVictoria() {
        return victoria;
    }

    public void setVictoria(boolean victoria) {
        this.victoria = victoria;
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