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
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "tablero_carta", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"id_partida", "fila", "columna"})
})
public class TableroCarta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_carta_tablero")
    private Integer idCartaTablero;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_partida", nullable = false)
    private Partida partida;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_palabra", nullable = false)
    private PalabraTema palabra;

    @Column(nullable = false)
    private int fila;

    @Column(nullable = false)
    private int columna;

    @Column(nullable = false, length = 32)
    private String estado = "oculta";

    @Column(nullable = false, length = 20)
    private String tipo;

    public TableroCarta() {}

    // Getters y Setters
    public Integer getIdCartaTablero() { return idCartaTablero; }
    public void setIdCartaTablero(Integer idCartaTablero) { this.idCartaTablero = idCartaTablero; }

    public Partida getPartida() { return partida; }
    public void setPartida(Partida partida) { this.partida = partida; }

    public PalabraTema getPalabra() { return palabra; }
    public void setPalabra(PalabraTema palabra) { this.palabra = palabra; }

    public int getFila() { return fila; }
    public void setFila(int fila) { this.fila = fila; }

    public int getColumna() { return columna; }
    public void setColumna(int columna) { this.columna = columna; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
}