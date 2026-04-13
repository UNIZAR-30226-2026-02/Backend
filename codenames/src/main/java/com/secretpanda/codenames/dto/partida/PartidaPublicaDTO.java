package com.secretpanda.codenames.dto.partida;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PartidaPublicaDTO {
    
    @JsonProperty("id_partida")
    private Integer idPartida;
    @JsonProperty("tag")
    private String tag;            // tag del creador
    @JsonProperty("nombre")
    private String nombre;         // nombre del tema
    @JsonProperty("tiempo_espera")
    private int tiempoEspera;
    @JsonProperty("max_jugadores")
    private int maxJugadores;
    @JsonProperty("jugadores_actuales")
    private int jugadoresActuales;

    public PartidaPublicaDTO(Integer idPartida, String tag, String nombre,
                             int tiempoEspera, int maxJugadores, int jugadoresActuales) {
        this.idPartida = idPartida;
        this.tag = tag;
        this.nombre = nombre;
        this.tiempoEspera = tiempoEspera;
        this.maxJugadores = maxJugadores;
        this.jugadoresActuales = jugadoresActuales;
    }

    // Getters
    public Integer getIdPartida()       { return idPartida; }
    public String getTag()              { return tag; }
    public String getNombre()           { return nombre; }
    public int getTiempoEspera()        { return tiempoEspera; }
    public int getMaxJugadores()        { return maxJugadores; }
    public int getJugadoresActuales()   { return jugadoresActuales; }

    // Setters (Opcionales, agrégalos si tu framework o lógica los llega a requerir para deserialización)
    public void setIdPartida(Integer idPartida) { this.idPartida = idPartida; }
    public void setTag(String tag) { this.tag = tag; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setTiempoEspera(int tiempoEspera) { this.tiempoEspera = tiempoEspera; }
    public void setMaxJugadores(int maxJugadores) { this.maxJugadores = maxJugadores; }
    public void setJugadoresActuales(int jugadoresActuales) { this.jugadoresActuales = jugadoresActuales; }
}