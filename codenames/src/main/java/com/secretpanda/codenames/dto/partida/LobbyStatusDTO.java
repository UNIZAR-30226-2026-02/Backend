package com.secretpanda.codenames.dto.partida;

import java.util.List;

/**
 * DTO para enviar el estado actual de la sala de espera (Lobby).
 * Incluye la configuración de la partida y los jugadores conectados.
 */
public class LobbyStatusDTO {

    private Integer idPartida; 
    private String codigoPartida; 
    private String estado; 
    private int maxJugadores; 
    private boolean esPublica; 
    private Integer idTema; 
    private String nombreTema;

    private List<JugadorPartidaDTO> jugadores;

    public LobbyStatusDTO() {
    }

    // Getters
    public Integer getIdPartida() { return idPartida; } 
    public String getCodigoPartida() { return codigoPartida; } 
    public String getEstado() { return estado; }
    public int getMaxJugadores() { return maxJugadores; } 
    public boolean isEsPublica() { return esPublica; } 
    public Integer getIdTema() { return idTema; } 
    public String getNombreTema() { return nombreTema; }
    public List<JugadorPartidaDTO> getJugadores() { return jugadores; }

    // Setters
    public void setIdPartida(Integer idPartida) { this.idPartida = idPartida; } 
    public void setCodigoPartida(String codigoPartida) { this.codigoPartida = codigoPartida; } 
    public void setEstado(String estado) { this.estado = estado; }
    public void setMaxJugadores(int maxJugadores) { this.maxJugadores = maxJugadores; } 
    public void setEsPublica(boolean esPublica) { this.esPublica = esPublica; } 
    public void setIdTema(Integer idTema) { this.idTema = idTema; } 
    public void setNombreTema(String nombreTema) { this.nombreTema = nombreTema; }
    public void setJugadores(List<JugadorPartidaDTO> jugadores) { this.jugadores = jugadores; }
}