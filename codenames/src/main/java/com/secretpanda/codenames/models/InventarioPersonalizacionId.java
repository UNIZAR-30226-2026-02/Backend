package com.secretpanda.codenames.models;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class InventarioPersonalizacionId implements Serializable {

    @Column(name = "id_jugador", length = 2048)
    private String idJugador;

    @Column(name = "id_personalizacion")
    private Integer idPersonalizacion;

    public InventarioPersonalizacionId() {}

    public InventarioPersonalizacionId(String idJugador, Integer idPersonalizacion) {
        this.idJugador = idJugador;
        this.idPersonalizacion = idPersonalizacion;
    }

    public String getIdJugador() { return idJugador; }
    public void setIdJugador(String idJugador) { this.idJugador = idJugador; }

    public Integer getIdPersonalizacion() { return idPersonalizacion; }
    public void setIdPersonalizacion(Integer idPersonalizacion) { this.idPersonalizacion = idPersonalizacion; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InventarioPersonalizacionId that = (InventarioPersonalizacionId) o;
        return Objects.equals(idJugador, that.idJugador) &&
               Objects.equals(idPersonalizacion, that.idPersonalizacion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idJugador, idPersonalizacion);
    }
}