package com.secretpanda.codenames.dto.jugador;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO de inventario para Tema.
 * Representa un pack de palabras que el jugador ya ha adquirido.
 * Se usa en GET /api/jugadores/{id_google}/temas y para filtrar el selector de partidas.
 */
public class TemaInventarioDTO {

    @JsonProperty("id_tema")
    private Integer idTema;
    @JsonProperty("nombre")
    private String nombre;
    @JsonProperty("descripcion")
    private String descripcion;

    public TemaInventarioDTO() {}

    // Getters
    public Integer getIdTema() { return idTema; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }

    // Setters
    public void setIdTema(Integer idTema) { this.idTema = idTema; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
}