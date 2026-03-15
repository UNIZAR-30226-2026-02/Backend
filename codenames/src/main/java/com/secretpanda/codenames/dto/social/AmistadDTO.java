package com.secretpanda.codenames.dto.social;

import java.time.LocalDateTime;

/**
 * DTO para transferir el estado de una amistad junto con 
 * los datos visuales de los jugadores involucrados.
 */
public class AmistadDTO {

    // Datos de quien envía la solicitud
    private String idSolicitante; 
    private String tagSolicitante;
    private String fotoSolicitante;

    // Datos de quien recibe la solicitud
    private String idReceptor; 
    private String tagReceptor;
    private String fotoReceptor;

    // Estado y metadatos de la amistad
    private String estado; // "pendiente" o "aceptada"
    private LocalDateTime fechaSolicitud; 

    public AmistadDTO() {
    }

    public String getIdSolicitante() { 
        return idSolicitante; 
    }

    public void setIdSolicitante(String idSolicitante) { 
        this.idSolicitante = idSolicitante; 
    }

    public String getTagSolicitante() {
        return tagSolicitante;
    }

    public void setTagSolicitante(String tagSolicitante) {
        this.tagSolicitante = tagSolicitante;
    }

    public String getFotoSolicitante() {
        return fotoSolicitante;
    }

    public void setFotoSolicitante(String fotoSolicitante) {
        this.fotoSolicitante = fotoSolicitante;
    }

    public String getIdReceptor() { 
        return idReceptor; 
    }

    public void setIdReceptor(String idReceptor) { 
        this.idReceptor = idReceptor; 
    }

    public String getTagReceptor() {
        return tagReceptor;
    }

    public void setTagReceptor(String tagReceptor) {
        this.tagReceptor = tagReceptor;
    }

    public String getFotoReceptor() {
        return fotoReceptor;
    }

    public void setFotoReceptor(String fotoReceptor) {
        this.fotoReceptor = fotoReceptor;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaSolicitud() { 
        return fechaSolicitud; 
    }

    public void setFechaSolicitud(LocalDateTime fechaSolicitud) { 
        this.fechaSolicitud = fechaSolicitud; 
    }
}