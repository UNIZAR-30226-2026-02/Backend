package com.secretpanda.codenames.models;

import jakarta.persistence.*;

@Entity
@Table(name = "tablero_carta", uniqueConstraints = {
    // Evita que haya dos cartas en la misma coordenada de la misma partida
    @UniqueConstraint(columnNames = {"id_partida", "fila", "columna"})
})
public class TableroCarta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Se autogenera por ser SERIAL
    @Column(name = "id_carta_tablero")
    private Integer idCartaTablero;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_partida", nullable = false)
    private Partida partida;

    @ManyToOne(fetch = FetchType.EAGER) // Eager para traer el texto de la palabra siempre
    @JoinColumn(name = "id_palabra", nullable = false)
    private PalabraTema palabra;

    @Column(nullable = false)
    private Integer fila; // De 0 a 3

    @Column(nullable = false)
    private Integer columna; // De 0 a 4

    @Column(nullable = false, length = 32)
    private String estado = "oculta"; // Ej: 'oculta', 'revelada' (y sus variantes según tus triggers)

    @Column(nullable = false, length = 20)
    private String tipo; // 'rojo', 'azul', 'civil', 'asesino'

    public TableroCarta() {}

    public Integer getIdCartaTablero() { return idCartaTablero; }
    public void setIdCartaTablero(Integer idCartaTablero) { this.idCartaTablero = idCartaTablero; }

    public Partida getPartida() { return partida; }
    public void setPartida(Partida partida) { this.partida = partida; }

    public PalabraTema getPalabra() { return palabra; }
    public void setPalabra(PalabraTema palabra) { this.palabra = palabra; }

    public Integer getFila() { return fila; }
    public void setFila(Integer fila) { this.fila = fila; }

    public Integer getColumna() { return columna; }
    public void setColumna(Integer columna) { this.columna = columna; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
}