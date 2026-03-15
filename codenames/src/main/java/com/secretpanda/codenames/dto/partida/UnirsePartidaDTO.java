package com.secretpanda.codenames.dto.partida;

/**
 * DTO para recibir la solicitud de un jugador que intenta unirse a una partida mediante un código.
 */
public class UnirsePartidaDTO {

    private String idJugador; 
    private String codigoPartida; 

    public UnirsePartidaDTO() {
    }

    public String getIdJugador() { 
        return idJugador; 
    }

    public void setIdJugador(String idJugador) { 
        this.idJugador = idJugador; 
    }

    public String getCodigoPartida() { 
        return codigoPartida; 
    }

    public void setCodigoPartida(String codigoPartida) { 
        this.codigoPartida = codigoPartida; 
    }
}