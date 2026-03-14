package com.secretpanda.codenames.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@Entity
@Table(name = "tablero_carta", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"id_partida", "fila", "columna"})
})
public class TableroCarta {

    public enum EstadoCarta {
        oculta, revelada
    }

    public enum TipoCarta {
        rojo, azul, civil, asesino
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_carta_tablero")
    private Integer idCartaTablero;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_partida", nullable = false)
    private Partida partida;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_palabra", nullable = false)
    private PalabraTema palabra;

    @Min(0)
    @Max(3)
    @Column(nullable = false)
    private int fila;

    @Min(0)
    @Max(4)
    @Column(nullable = false)
    private int columna;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private EstadoCarta estado;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoCarta tipo;

    public TableroCarta() {}

    @PrePersist
    protected void onPersist() {
        if (this.estado == null) {
            this.estado = EstadoCarta.oculta;
        }
    }

    public Integer getIdCartaTablero() { 
        return idCartaTablero; 
    }

    public void setIdCartaTablero(Integer idCartaTablero) { 
        this.idCartaTablero = idCartaTablero; 
    }

    public Partida getPartida() { 
        return partida; 
    }

    public void setPartida(Partida partida) { 
        this.partida = partida; 
    }

    public PalabraTema getPalabra() { 
        return palabra; 
    }

    public void setPalabra(PalabraTema palabra) { 
        this.palabra = palabra; 
    }

    public int getFila() { 
        return fila; 
    }

    public void setFila(int fila) { 
        this.fila = fila; 
    }

    public int getColumna() { 
        return columna; 
    }

    public void setColumna(int columna) { 
        this.columna = columna; 
    }

    public EstadoCarta getEstado() { 
        return estado; 
    }

    public void setEstado(EstadoCarta estado) { 
        this.estado = estado; 
    }

    public TipoCarta getTipo() { 
        return tipo; 
    }

    public void setTipo(TipoCarta tipo) { 
        this.tipo = tipo; 
    }
}