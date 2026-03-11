package com.secretpanda.codenames.dto.tienda;

/**
 * DTO para transferir los datos de los objetos de personalización de la tienda.
 */
public class PersonalizacionDTO {

    private Integer idPersonalizacion;
    private String nombre;
    private String descripcion;
    private int precioBala;
    private String tipo; // Se pasa a String (ej: "carta" o "tablero")
    private String valorVisual;
    private boolean activo;

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

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}