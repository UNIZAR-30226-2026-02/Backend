package com.secretpanda.codenames.dto.tienda;

/**
 * DTO para la Pantalla de Logros y Medallas.
 * Basado estrictamente en el contrato de API (Página 30).
 */
public class LogroDTO {

    private Integer idLogro;
    private boolean esLogro; // true = Logro, false = Medalla
    private String nombre;
    private String descripcion;
    private int progresoActual;
    private int progresoMax;
    private boolean completado;
    private int balasRecompensa;

    public LogroDTO() {
    }

    // GETTERS Y SETTERS

    public Integer getIdLogro() {
        return idLogro;
    }

    public void setIdLogro(Integer idLogro) {
        this.idLogro = idLogro;
    }

    public boolean isEsLogro() {
        return esLogro;
    }

    public void setEsLogro(boolean esLogro) {
        this.esLogro = esLogro;
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

    public int getProgresoActual() {
        return progresoActual;
    }

    public void setProgresoActual(int progresoActual) {
        this.progresoActual = progresoActual;
    }

    public int getProgresoMax() {
        return progresoMax;
    }

    public void setProgresoMax(int progresoMax) {
        this.progresoMax = progresoMax;
    }

    public boolean isCompletado() {
        return completado;
    }

    public void setCompletado(boolean completado) {
        this.completado = completado;
    }

    public int getBalasRecompensa() {
        return balasRecompensa;
    }

    public void setBalasRecompensa(int balasRecompensa) {
        this.balasRecompensa = balasRecompensa;
    }
}