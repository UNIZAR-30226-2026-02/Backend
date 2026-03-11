package com.secretpanda.codenames.dto.partida;

import java.util.List;

/**
 * DTO para enviar el estado actual de la sala de espera (Lobby),
 * incluyendo la configuración de la partida y los jugadores conectados.
 */
public class LobbyStatusDTO {

    // Información general de la partida
    private Integer idPartida;
    private String codigoPartida;
    private String estado; 
    private int maxJugadores;
    private String nombreTema; // Nombre del tema elegido

    // Lista de jugadores que están actualmente en el lobby
    private List<JugadorPartidaDTO> jugadores;

    public LobbyStatusDTO() {
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

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public int getMaxJugadores() {
        return maxJugadores;
    }

    public void setMaxJugadores(int maxJugadores) {
        this.maxJugadores = maxJugadores;
    }

    public String getNombreTema() {
        return nombreTema;
    }

    public void setNombreTema(String nombreTema) {
        this.nombreTema = nombreTema;
    }

    public List<JugadorPartidaDTO> getJugadores() {
        return jugadores;
    }

    public void setJugadores(List<JugadorPartidaDTO> jugadores) {
        this.jugadores = jugadores;
    }
}