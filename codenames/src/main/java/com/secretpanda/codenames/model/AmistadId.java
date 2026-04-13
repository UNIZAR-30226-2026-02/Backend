package com.secretpanda.codenames.model;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class AmistadId implements Serializable {

    @Column(name = "id_solicitante", length = 255)
    private String idSolicitante;

    @Column(name = "id_receptor", length = 255)
    private String idReceptor;

    public AmistadId() {}

    public String getIdSolicitante() { 
        return idSolicitante; 
    }

    public void setIdSolicitante(String idSolicitante) { 
        this.idSolicitante = idSolicitante; 
    }

    public String getIdReceptor() { 
        return idReceptor; 
    }

    public void setIdReceptor(String idReceptor) { 
        this.idReceptor = idReceptor; 
    }

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