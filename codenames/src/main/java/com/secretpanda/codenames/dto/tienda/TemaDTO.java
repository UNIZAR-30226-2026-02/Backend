package com.secretpanda.codenames.dto.tienda;

/**
 * DTO para transferir la información de los packs de palabras (Temas).
 */
public class TemaDTO {

    private Integer idTema; 
    private String nombre;
    private String descripcion;
    private int precioBalas; 
    private boolean activo;
    
    // Estado para la vista (Frontend)
    private boolean comprado;

    public TemaDTO() {
    }

    // Getters
    public Integer getIdTema() { return idTema; } 
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public int getPrecioBalas() { return precioBalas; } 
    public boolean isActivo() { return activo; }
    public boolean isComprado() { return comprado; }

    // Setters
    public void setIdTema(Integer idTema) { this.idTema = idTema; } 
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public void setPrecioBalas(int precioBalas) { this.precioBalas = precioBalas; } 
    public void setActivo(boolean activo) { this.activo = activo; }
    public void setComprado(boolean comprado) { this.comprado = comprado; }
}