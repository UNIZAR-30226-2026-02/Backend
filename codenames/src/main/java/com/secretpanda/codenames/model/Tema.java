package com.secretpanda.codenames.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "tema")
public class Tema {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tema")
    private Integer idTema;

    @Column(nullable = false, unique = true, length = 128)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "precio_balas", nullable = false)
    private int precioBalas = 0;

    @Column(nullable = false)
    private boolean activo = true;

    @OneToMany(mappedBy = "tema", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PalabraTema> palabras = new ArrayList<>();

    public Tema() {}

    // Helper Bidireccional
    public void addPalabra(PalabraTema palabra) {
        palabras.add(palabra);
        palabra.setTema(this);
    }

    public Integer getIdTema() { 
        return idTema; 
    }

    public void setIdTema(Integer idTema) { 
        this.idTema = idTema; 
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

    public int getPrecioBalas() { 
        return precioBalas; 
    }

    public void setPrecioBalas(int precioBalas) { 
        this.precioBalas = precioBalas; 
    }

    public boolean isActivo() { 
        return activo; 
    }

    public void setActivo(boolean activo) { 
        this.activo = activo; 
    }

    public List<PalabraTema> getPalabras() { 
        return palabras; 
    }

    public void setPalabras(List<PalabraTema> palabras) { 
        this.palabras = palabras; 
    }
}