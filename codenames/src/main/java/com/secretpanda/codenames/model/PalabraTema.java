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
    @GeneratedValue(strategy = GenerationType.IDENTITY) // SERIAL en la base de datos
    @Column(name = "id_palabra")
    private Integer idPalabra;

    // Relación Muchos a Uno con la tabla Tema
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tema", nullable = false)
    private Tema tema;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String valor;

    @Column(nullable = false)
    private Boolean activo = true;

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

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
}