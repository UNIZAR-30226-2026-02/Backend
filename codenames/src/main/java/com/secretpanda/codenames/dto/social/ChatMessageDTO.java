package com.secretpanda.codenames.dto.social;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

/**
 * DTO de mensaje de chat. El campo esValido indica si superó el filtro de palabras.
 * Si esValido = false el frontend muestra un popup de advertencia. Esto de momento no etá en el frontend, lo haremos más adelante
 */
public class ChatMessageDTO {

    @JsonProperty("id_mensaje")
    private Integer idMensaje;
    @JsonProperty("id_partida")
    private Integer idPartida;

    @JsonProperty("id_jugador")
    private String idJugador;
    @JsonProperty("tag")
    private String tag;
    @JsonProperty("equipo")
    private String equipo;     // "rojo" o "azul" — define a qué topic se enruta

    @JsonProperty("mensaje")
    private String mensaje;
    @JsonProperty("fecha")
    private LocalDateTime fecha;
    @JsonProperty("es_valido")
    private boolean esValido;  // true = mensaje limpio, false = fue censurado

    public ChatMessageDTO() {}

    // Getters
    public Integer getIdMensaje()     { return idMensaje; }
    public Integer getIdPartida()     { return idPartida; }
    public String getIdJugador()      { return idJugador; }
    public String getTag()            { return tag; }
    public String getEquipo()         { return equipo; }
    public String getMensaje()        { return mensaje; }
    public LocalDateTime getFecha()   { return fecha; }
    public boolean isEsValido()       { return esValido; }

    // Setters
    public void setIdMensaje(Integer v)     { this.idMensaje = v; }
    public void setIdPartida(Integer v)     { this.idPartida = v; }
    public void setIdJugador(String v)      { this.idJugador = v; }
    public void setTag(String v)            { this.tag = v; }
    public void setEquipo(String v)         { this.equipo = v; }
    public void setMensaje(String v)        { this.mensaje = v; }
    public void setFecha(LocalDateTime v)   { this.fecha = v; }
    public void setEsValido(boolean v)      { this.esValido = v; }
}
