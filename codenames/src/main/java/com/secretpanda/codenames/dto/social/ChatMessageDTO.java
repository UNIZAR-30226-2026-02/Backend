package com.secretpanda.codenames.dto.social;

import java.time.LocalDateTime;

/**
 * DTO para enviar la información de un mensaje de chat dentro de una partida.
 */
public class ChatMessageDTO {

    private Integer idMensaje;
    private Integer idPartida; 

    private String idJugador;
    private String tagJugador;
    private String equipo; 
    
    private String mensaje;
    private LocalDateTime fecha;

    public ChatMessageDTO() {
    }

    public Integer getIdMensaje() {
        return idMensaje;
    }

    public void setIdMensaje(Integer idMensaje) {
        this.idMensaje = idMensaje;
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

    public String getTagJugador() {
        return tagJugador;
    }

    public void setTagJugador(String tagJugador) {
        this.tagJugador = tagJugador;
    }

    public String getEquipo() {
        return equipo;
    }

    public void setEquipo(String equipo) {
        this.equipo = equipo;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }
}