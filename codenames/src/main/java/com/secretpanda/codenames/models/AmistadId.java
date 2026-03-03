package com.secretpanda.codenames.models;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class AmistadId implements Serializable {

    @Column(name = "id_solicitante", length = 2048)
    private String idSolicitante;

    @Column(name = "id_receptor", length = 2048)
    private String idReceptor;

    public AmistadId() {}

    public AmistadId(String idSolicitante, String idReceptor) {
        this.idSolicitante = idSolicitante;
        this.idReceptor = idReceptor;
    }

    // Getters y Setters
    public String getIdSolicitante() { return idSolicitante; }
    public void setIdSolicitante(String idSolicitante) { this.idSolicitante = idSolicitante; }

    public String getIdReceptor() { return idReceptor; }
    public void setIdReceptor(String idReceptor) { this.idReceptor = idReceptor; }

    // equals() y hashCode() son obligatorios en JPA para claves compuestas
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AmistadId amistadId = (AmistadId) o;
        return Objects.equals(idSolicitante, amistadId.idSolicitante) &&
               Objects.equals(idReceptor, amistadId.idReceptor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idSolicitante, idReceptor);
    }
}