package com.secretpanda.codenames.dto.tienda;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PersonalizacionDTO {

    @JsonProperty("id_personalizacion")
    private Integer idPersonalizacion;
    @JsonProperty("nombre")
    private String nombre;
    @JsonProperty("descripcion")
    private String descripcion;
    @JsonProperty("precio_bala")
    private int precioBala;
    @JsonProperty("tipo")
    private String tipo;
    @JsonProperty("valor_visual")
    private String valorVisual;
    @JsonProperty("comprado")
    private boolean comprado;

    public PersonalizacionDTO() {
    }

    public Integer getIdPersonalizacion() {
        return idPersonalizacion;
    }

    public void setIdPersonalizacion(Integer idPersonalizacion) {
        this.idPersonalizacion = idPersonalizacion;
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

    public int getPrecioBala() {
        return precioBala;
    }

    public void setPrecioBala(int precioBala) {
        this.precioBala = precioBala;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getValorVisual() {
        return valorVisual;
    }

    public void setValorVisual(String valorVisual) {
        this.valorVisual = valorVisual;
    }

    public boolean isComprado() {
        return comprado;
    }

    public void setComprado(boolean comprado) {
        this.comprado = comprado;
    }
}