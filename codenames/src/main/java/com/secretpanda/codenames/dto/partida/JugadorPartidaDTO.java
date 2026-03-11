package com.secretpanda.codenames.dto.partida;

/**
 * DTO para transferir la información de un jugador dentro de una partida específica,
 * incluyendo su equipo, rol y estadísticas de esa partida.
 */
public class JugadorPartidaDTO {

    private Integer idJugadorPartida;
    
    // Datos básicos del Jugador
    private String idJugador;
    private String tagJugador;
    private String fotoJugador;

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

    public String getTagJugador() {
        return tagJugador;
    }

    public void setTagJugador(String tagJugador) {
        this.tagJugador = tagJugador;
    }

    public String getFotoJugador() {
        return fotoJugador;
    }

    public void setFotoJugador(String fotoJugador) {
        this.fotoJugador = fotoJugador;
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