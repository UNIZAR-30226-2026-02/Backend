package com.secretpanda.codenames.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "logro")
public class Logro {

    public enum TipoLogro {
        medalla, logro
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_logro")
    private Integer idLogro;

    @Column(name = "nombre", nullable = false, unique = true, length = 128)
    private String nombre;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 64)
    private TipoLogro tipo;

    @Column(name = "estadistica_clave", nullable = false, length = 64)
    private String estadisticaClave; 

    @Column(name = "valor_objetivo", nullable = false)
    private int valorObjetivo;

    @Column(name = "balas_recompensa", nullable = false)
    private int balasRecompensa;

    @Column(name = "activo", nullable = false)
    private boolean activo = true;

    public Logro() {
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

    public TipoLogro getTipo() { 
        return tipo; 
    }

    public void setTipo(TipoLogro tipo) { 
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