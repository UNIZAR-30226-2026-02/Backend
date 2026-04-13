package com.secretpanda.codenames.model;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class InventarioTemaId implements Serializable {

    @Column(name = "id_jugador", length = 255, nullable = false)
    private String idJugador;

    @Column(name = "id_tema", nullable = false)
    private Integer idTema;

    public InventarioTemaId() {}

    public String getIdJugador() {
        return idJugador;
    }

    public void setIdJugador(String idJugador) {
        this.idJugador = idJugador;
    }

    public Integer getIdTema() {
        return idTema;
    }

    public void setIdTema(Integer idTema) {
        this.idTema = idTema;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InventarioTemaId that = (InventarioTemaId) o;
        return Objects.equals(idJugador, that.idJugador) &&
               Objects.equals(idTema, that.idTema);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idJugador, idTema);
    }
}