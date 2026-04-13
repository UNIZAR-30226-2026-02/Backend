package com.secretpanda.codenames.dto.partida;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO para transferir la información de un jugador dentro de una partida específica,
 * incluyendo su equipo, rol y estadísticas de esa partida.
 */
public class JugadorPartidaDTO {

    @JsonProperty("id_jugador_partida")
    private Integer idJugadorPartida;
    
    // Datos básicos del Jugador
    @JsonProperty("id_jugador")
    private String idJugador;
    @JsonProperty("tag")
    private String tag;
    @JsonProperty("foto_perfil")
    private String fotoPerfil;

    // Estado del jugador en la partida
    @JsonProperty("equipo")
    private String equipo; // "rojo" o "azul"
    @JsonProperty("rol")
    private String rol;    // "lider" o "agente"
    @JsonProperty("num_aciertos")
    private int numAciertos;
    @JsonProperty("num_fallos")
    private int numFallos;
    @JsonProperty("abandono")
    private boolean abandono;

    public JugadorPartidaDTO() {
    }

    public Integer getIdJugadorPartida() { 
        return idJugadorPartida; 
    }

    public void setIdJugadorPartida(Integer idJugadorPartida) { 
        this.idJugadorPartida = idJugadorPartida; 
    }

    public String getIdJugador() { 
        return idJugador; 
    }

    public void setIdJugador(String idJugador) { 
        this.idJugador = idJugador; 
    }
    
    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getFotoPerfil() {
        return fotoPerfil;
    }

    public void setFotoPerfil(String fotoPerfil) {
        this.fotoPerfil = fotoPerfil;
    }

    public String getEquipo() {
        return equipo;
    }

    public void setEquipo(String equipo) {
        this.equipo = equipo;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public int getNumAciertos() { 
        return numAciertos; 
    }

    public void setNumAciertos(int numAciertos) { 
        this.numAciertos = numAciertos; 
    }

    public int getNumFallos() { 
        return numFallos; 
    }

    public void setNumFallos(int numFallos) { 
        this.numFallos = numFallos; 
    }

    public boolean isAbandono() {
        return abandono;
    }

    public void setAbandono(boolean abandono) {
        this.abandono = abandono;
    }
}