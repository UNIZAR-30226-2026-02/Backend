package com.secretpanda.codenames.dto.partida;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Respuesta de GET /api/partidas/{id}/participantes/rol
 * Informa al frontend del rol del jugador logueado en esa partida.
 */
public class RolPartidaDTO {

    @JsonProperty("rol")
    private String rol;            // "lider" o "agente"
    @JsonProperty("equipo")
    private String equipo;         // "rojo" o "azul"
    @JsonProperty("equipo_inicial")
    private String equipoInicial;  // equipo que inicia la partida (el que tiene más cartas)

    public RolPartidaDTO() {}

    public RolPartidaDTO(String rol, String equipo, String equipoInicial) {
        this.rol = rol;
        this.equipo = equipo;
        this.equipoInicial = equipoInicial;
    }

    public String getRol()             { return rol; }
    public String getEquipo()          { return equipo; }
    public String getEquipoInicial()   { return equipoInicial; }

    public void setRol(String rol)                   { this.rol = rol; }
    public void setEquipo(String equipo)             { this.equipo = equipo; }
    public void setEquipoInicial(String ei)          { this.equipoInicial = ei; }
}
