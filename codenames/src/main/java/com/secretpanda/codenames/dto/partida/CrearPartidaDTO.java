package com.secretpanda.codenames.dto.partida;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * DTO para recibir la solicitud de creación de una nueva partida desde la app.
 * NOTA: El id del creador se extrae de forma segura del token JWT en el controlador.
 */
public class CrearPartidaDTO {

    @NotNull(message = "El id del tema es obligatorio")
    @JsonProperty("id_tema")
    private Integer idTema; 

    @Min(value = 30, message = "El tiempo mínimo es 30 segundos")
    @Max(value = 120, message = "El tiempo máximo es 120 segundos")
    @JsonProperty("tiempo_espera")
    private int tiempoEspera; 

    @Min(value = 4, message = "Mínimo 4 jugadores")
    @Max(value = 16, message = "Máximo 16 jugadores")
    @JsonProperty("max_jugadores")
    private int maxJugadores; 

    @JsonProperty("es_publica")
    private boolean esPublica; 

    public CrearPartidaDTO() {
    }

    public Integer getIdTema() { 
        return idTema; 
    }

    public void setIdTema(Integer idTema) { 
        this.idTema = idTema; 
    }

    public int getTiempoEspera() { 
        return tiempoEspera; 
    }

    public void setTiempoEspera(int tiempoEspera) { 
        this.tiempoEspera = tiempoEspera; 
    }

    public int getMaxJugadores() { 
        return maxJugadores; 
    }

    public void setMaxJugadores(int maxJugadores) { 
        this.maxJugadores = maxJugadores; 
    }

    public boolean isEsPublica() { 
        return esPublica; 
    }

    public void setEsPublica(boolean esPublica) { 
        this.esPublica = esPublica; 
    }
}