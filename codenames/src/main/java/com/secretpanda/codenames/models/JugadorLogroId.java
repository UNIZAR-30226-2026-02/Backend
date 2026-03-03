package com.secretpanda.codenames.models;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class JugadorLogroId implements Serializable {

    @Column(name = "id_jugador", length = 2048)
    private String idJugador;

    @Column(name = "id_logro")
    private Integer idLogro;

    public JugadorLogroId() {}

    public JugadorLogroId(String idJugador, Integer idLogro) {
        this.idJugador = idJugador;
        this.idLogro = idLogro;
    }

    public String getIdJugador() { return idJugador; }
    public void setIdJugador(String idJugador) { this.idJugador = idJugador; }

    public Integer getIdLogro() { return idLogro; }
    public void setIdLogro(Integer idLogro) { this.idLogro = idLogro; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JugadorLogroId that = (JugadorLogroId) o;
        return Objects.equals(idJugador, that.idJugador) &&
               Objects.equals(idLogro, that.idLogro);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idJugador, idLogro);
    }
}