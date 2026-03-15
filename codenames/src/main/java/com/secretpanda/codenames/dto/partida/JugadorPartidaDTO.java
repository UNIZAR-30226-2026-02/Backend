package com.secretpanda.codenames.dto.partida;

/**
 * DTO para transferir la información de un jugador dentro de una partida específica,
 * incluyendo su equipo, rol y estadísticas de esa partida.
 */
public class JugadorPartidaDTO {

    private Integer idJugadorPartida;
    
    // Datos básicos del Jugador
    private String idJugador;
    private String tag;
    private String fotoPerfil;

    // Estado del jugador en la partida
    private String equipo; // "rojo" o "azul"
    private String rol;    // "lider" o "agente"
    private int numAciertos;
    private int numFallos;
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