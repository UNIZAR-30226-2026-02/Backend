package com.secretpanda.codenames.dto.social;

/**
 * DTO para recibir la solicitud de envío de un nuevo mensaje de chat desde la app.
 * NOTA: El id del jugador se extrae de forma segura del token JWT en el controlador.
 */
public class EnviarMensajeDTO {

    private Integer idPartida; 
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