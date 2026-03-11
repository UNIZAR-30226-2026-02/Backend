package com.secretpanda.codenames.dto.tienda;

/**
 * DTO para transferir la información de un logro o medalla.
 */
public class LogroDTO {

    private Integer idLogro;
    private String nombre;
    private String descripcion;
    private String tipo; // Se pasa a String (ej: "medalla" o "logro")
    private String estadisticaClave;
    private int valorObjetivo;
    private int balasRecompensa;
    private boolean activo;

    public LogroDTO() {
    }

    public Integer getIdLogro() {
        return idLogro;
    }

    public void setIdLogro(Integer idLogro) {
        this.idLogro = idLogro;
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

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getEstadisticaClave() {
        return estadisticaClave;
    }

    public void setEstadisticaClave(String estadisticaClave) {
        this.estadisticaClave = estadisticaClave;
    }

    public int getValorObjetivo() {
        return valorObjetivo;
    }

    public void setValorObjetivo(int valorObjetivo) {
        this.valorObjetivo = valorObjetivo;
    }

    public int getBalasRecompensa() {
        return balasRecompensa;
    }

    public void setBalasRecompensa(int balasRecompensa) {
        this.balasRecompensa = balasRecompensa;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}