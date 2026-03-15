package com.secretpanda.codenames.dto.partida;

/**
 * DTO para recibir la solicitud de un jugador que intenta unirse a una partida mediante un código.
 * NOTA: El id del jugador se extrae de forma segura del token JWT en el controlador.
 */
public class UnirsePartidaDTO {

    private String codigoPartida; 

    public UnirsePartidaDTO() {
    }

    public String getCodigoPartida() { 
        return codigoPartida; 
    }

    public void setCodigoPartida(String codigoPartida) { 
        this.codigoPartida = codigoPartida; 
    }
}