package com.secretpanda.codenames.dto.jugador;

/**
 * DTO de inventario para Personalización.
 * Representa un cosmético que el jugador ya posee, incluyendo su estado de equipamiento.
 * Se usa en GET /api/personalizaciones/{id_google} y en el UserContext.
 */
public class PersonalizacionInventarioDTO {

    // Datos del ítem necesarios para renderizarlo en la UI
    private Integer idPersonalizacion;
    private String nombre;
    private String tipo;        // "carta" o "tablero"
    private String valorVisual; // URL del asset

    // Estado de posesión (exclusivo de este DTO)
    private boolean equipado;

    public PersonalizacionInventarioDTO() {}

    // Getters
    public Integer getIdPersonalizacion() { return idPersonalizacion; }
    public String getNombre() { return nombre; }
    public String getTipo() { return tipo; }
    public String getValorVisual() { return valorVisual; }
    public boolean isEquipado() { return equipado; }

    // Setters
    public void setIdPersonalizacion(Integer idPersonalizacion) { this.idPersonalizacion = idPersonalizacion; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public void setValorVisual(String valorVisual) { this.valorVisual = valorVisual; }
    public void setEquipado(boolean equipado) { this.equipado = equipado; }
}