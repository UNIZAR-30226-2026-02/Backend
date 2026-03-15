package com.secretpanda.codenames.dto.tienda;

/**
 * DTO para transferir los datos de los objetos de personalización de la tienda.
 * Incluye el estado de posesión para la vista del usuario.
 */
public class PersonalizacionDTO {

    private Integer idPersonalizacion; 
    private String nombre;
    private String descripcion;
    private int precioBala; 
    private String tipo; // "carta" o "tablero"
    private String valorVisual; 
    private boolean activo;

    // ESTADOS PARA LA VISTA (Para que el frontend sepa qué botón pintar)
    private boolean comprado;
    private boolean equipado;

    public PersonalizacionDTO() {
    }

    // Getters
    public Integer getIdPersonalizacion() { return idPersonalizacion; } 
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public int getPrecioBala() { return precioBala; } 
    public String getTipo() { return tipo; }
    public String getValorVisual() { return valorVisual; } 
    public boolean isActivo() { return activo; }
    public boolean isComprado() { return comprado; }
    public boolean isEquipado() { return equipado; }

    // Setters
    public void setIdPersonalizacion(Integer idPersonalizacion) { this.idPersonalizacion = idPersonalizacion; } 
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public void setPrecioBala(int precioBala) { this.precioBala = precioBala; } 
    public void setTipo(String tipo) { this.tipo = tipo; }
    public void setValorVisual(String valorVisual) { this.valorVisual = valorVisual; } 
    public void setActivo(boolean activo) { this.activo = activo; }
    public void setComprado(boolean comprado) { this.comprado = comprado; }
    public void setEquipado(boolean equipado) { this.equipado = equipado; }
}