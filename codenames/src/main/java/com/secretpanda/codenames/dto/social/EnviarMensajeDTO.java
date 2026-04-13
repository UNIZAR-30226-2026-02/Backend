package com.secretpanda.codenames.dto.social;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO para recibir la solicitud de envío de un nuevo mensaje de chat desde la app.
 * NOTA: El id del jugador se extrae de forma segura del token JWT en el controlador.
 */
public class EnviarMensajeDTO {

    @JsonProperty("id_partida")
    private Integer idPartida; 
    @JsonProperty("mensaje")
    private String mensaje;

    public EnviarMensajeDTO() {
    }

    public Integer getIdPartida() { 
        return idPartida; 
    }

    public void setIdPartida(Integer idPartida) { 
        this.idPartida = idPartida; 
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
}