package com.secretpanda.codenames.dto.tienda;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TemaDTO {

    @JsonProperty("id_tema")
    private Integer idTema;
    @JsonProperty("nombre")
    private String nombre;
    @JsonProperty("descripcion")
    private String descripcion;
    @JsonProperty("precio_balas")
    private int precioBalas;
    @JsonProperty("comprado")
    private boolean comprado;

    public TemaDTO() {
    }

    public Integer getIdTema() {
        return idTema;
    }

    public void setIdTema(Integer idTema) {
        this.idTema = idTema;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public int getPrecioBalas() {
        return precioBalas;
    }

    public void setPrecioBalas(int precioBalas) {
        this.precioBalas = precioBalas;
    }

    public boolean isComprado() {
        return comprado;
    }

    public void setComprado(boolean comprado) {
        this.comprado = comprado;
    }
}