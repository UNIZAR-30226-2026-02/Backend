package com.secretpanda.codenames.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "palabra_tema")
public class PalabraTema {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_palabra")
    private Integer idPalabra;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tema", nullable = false)
    private Tema tema;

    @Column(name = "valor", nullable = false, columnDefinition = "TEXT")
    private String valor;

    @Column(name = "activo", nullable = false)
    private boolean activo = true;

    public PalabraTema() {
    }

    public Integer getIdPalabra() { 
        return idPalabra; 
    }

    public void setIdPalabra(Integer idPalabra) { 
        this.idPalabra = idPalabra; 
    }

    public Tema getTema() { 
        return tema; 
    }

    public void setTema(Tema tema) { 
        this.tema = tema; 
    }

    public String getValor() { 
        return valor; 
    }

    public void setValor(String valor) { 
        this.valor = valor; 
    }

    public boolean isActivo() { 
        return activo; 
    }

    public void setActivo(boolean activo) { 
        this.activo = activo; 
    }
}