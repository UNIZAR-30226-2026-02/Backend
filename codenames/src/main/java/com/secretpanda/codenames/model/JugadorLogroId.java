package com.secretpanda.codenames.model;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JugadorLogroId implements Serializable {

    @Column(name = "id_jugador")
    private String idJugador;

    @Column(name = "id_logro")
    private Integer idLogro;

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