package com.secretpanda.codenames.model;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

@Entity
@Table(name = "inventario_tema")
public class InventarioTema {

    @EmbeddedId
    private InventarioTemaId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idJugador")
    @JoinColumn(name = "id_jugador")
    private Jugador jugador;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idTema")
    @JoinColumn(name = "id_tema")
    private Tema tema;

    public InventarioTema() {}

    public InventarioTema(Jugador jugador, Tema tema) {
        this.jugador = jugador;
        this.tema = tema;
        this.id = new InventarioTemaId(jugador.getIdGoogle(), tema.getIdTema());
    }

    public InventarioTemaId getId() {
        return id;
    }

    public void setId(InventarioTemaId id) {
        this.id = id;
    }

    public Jugador getJugador() {
        return jugador;
    }

    public void setJugador(Jugador jugador) {
        this.jugador = jugador;
    }

    public Tema getTema() {
        return tema;
    }

    public void setTema(Tema tema) {
        this.tema = tema;
    }
}