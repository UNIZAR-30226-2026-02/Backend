package com.secretpanda.codenames.dto.tienda;

import java.time.LocalDateTime;

/**
 * DTO para transferir la información de un logro, incluyendo
 * el progreso actual del jugador que lo solicita.
 */
public class LogroDTO {

    // Datos base del Logro (BD: logro)
    private Integer idLogro; 
    private String nombre;
    private String descripcion;
    private String tipo; 
    private String estadisticaClave; 
    private int valorObjetivo; 
    private int balasRecompensa; 
    private boolean activo;

    // Estado del progreso del Jugador (BD: jugador_logro)
    private int progresoActual; 
    private boolean completado;
    private LocalDateTime fechaDesbloqueo; 

    public LogroDTO() {
    }

    // Getters
    public Integer getIdLogro() { return idLogro; } 
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public String getTipo() { return tipo; }
    public String getEstadisticaClave() { return estadisticaClave; } 
    public int getValorObjetivo() { return valorObjetivo; } 
    public int getBalasRecompensa() { return balasRecompensa; } 
    public boolean isActivo() { return activo; }
    public int getProgresoActual() { return progresoActual; } 
    public boolean isCompletado() { return completado; }
    public LocalDateTime getFechaDesbloqueo() { return fechaDesbloqueo; } 

    // Setters
    public void setIdLogro(Integer idLogro) { this.idLogro = idLogro; } 
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public void setEstadisticaClave(String estadisticaClave) { this.estadisticaClave = estadisticaClave; } 
    public void setValorObjetivo(int valorObjetivo) { this.valorObjetivo = valorObjetivo; } 
    public void setBalasRecompensa(int balasRecompensa) { this.balasRecompensa = balasRecompensa; } 
    public void setActivo(boolean activo) { this.activo = activo; }
    public void setProgresoActual(int progresoActual) { this.progresoActual = progresoActual; } 
    public void setCompletado(boolean completado) { this.completado = completado; }
    public void setFechaDesbloqueo(LocalDateTime fechaDesbloqueo) { this.fechaDesbloqueo = fechaDesbloqueo; } 
}