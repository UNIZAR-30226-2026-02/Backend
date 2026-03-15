package com.secretpanda.codenames.dto.social;

/**
 * DTO para recibir la solicitud de envío de un nuevo mensaje de chat desde la app.
 */
public class EnviarMensajeDTO {

    private Integer idPartida; 
    private String idJugador; 
    private String mensaje;

    public EnviarMensajeDTO() {
    }

    public Integer getIdPartida() { 
        return idPartida; 
    }

    public void setIdPartida(Integer idPartida) { 
        this.idPartida = idPartida; 
    }

    public String getIdJugador() { 
        return idJugador; 
    }

    public void setIdJugador(String idJugador) { 
        this.idJugador = idJugador; 
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
}