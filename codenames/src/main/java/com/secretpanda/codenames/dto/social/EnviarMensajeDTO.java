package com.secretpanda.codenames.dto.social;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO para recibir la solicitud de envío de un nuevo mensaje de chat desde la app.
 * NOTA: El id del jugador se extrae de forma segura del token JWT en el controlador.
 */
public class EnviarMensajeDTO {

    @NotNull(message = "El id de la partida es obligatorio")
    @JsonProperty("id_partida")
    private Integer idPartida; 

    @NotBlank(message = "El mensaje no puede estar vacío")
    @Size(max = 255, message = "El mensaje es demasiado largo")
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