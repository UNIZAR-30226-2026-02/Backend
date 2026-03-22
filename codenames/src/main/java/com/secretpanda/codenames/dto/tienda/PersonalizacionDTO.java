package com.secretpanda.codenames.dto.tienda;

/**
 * DTO de catálogo para Personalización.
 * Contiene exclusivamente los datos estáticos del ítem (tienda).
 * El estado de posesión y equipamiento se gestiona en PersonalizacionInventarioDTO.
 */
public class PersonalizacionDTO {

    private Integer idPersonalizacion; 
    private String nombre;
    private String descripcion;
    private int precioBala; 
    private String tipo; // "carta" o "tablero"
    private String valorVisual; 
    private boolean activo;

    public PersonalizacionDTO() {}

    // Getters
    public Integer getIdPersonalizacion() { return idPersonalizacion; } 
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public int getPrecioBala() { return precioBala; } 
    public String getTipo() { return tipo; }
    public String getValorVisual() { return valorVisual; } 
    public boolean isActivo() { return activo; }

    // Setters
    public void setIdPersonalizacion(Integer idPersonalizacion) { this.idPersonalizacion = idPersonalizacion; } 
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public void setPrecioBala(int precioBala) { this.precioBala = precioBala; } 
    public void setTipo(String tipo) { this.tipo = tipo; }
    public void setValorVisual(String valorVisual) { this.valorVisual = valorVisual; } 
    public void setActivo(boolean activo) { this.activo = activo; }
}