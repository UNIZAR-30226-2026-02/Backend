package com.secretpanda.codenames.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "personalizacion")
public class Personalizacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_personalizacion")
    private Integer idPersonalizacion;

    @Column(nullable = false, unique = true, length = 128)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "precio_bala", nullable = false)
    private int precioBala = 0;

    @Column(nullable = false, length = 64)
    private String tipo; 

    @Column(name = "valor_visual", columnDefinition = "TEXT")
    private String valorVisual; 

    @Column(nullable = false)
    private boolean activo = true;

    public Personalizacion() {}

    public Integer getIdPersonalizacion() { return idPersonalizacion; }
    public void setIdPersonalizacion(Integer idPersonalizacion) { this.idPersonalizacion = idPersonalizacion; }

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