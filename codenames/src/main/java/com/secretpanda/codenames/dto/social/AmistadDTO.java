package com.secretpanda.codenames.dto.social;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AmistadDTO {

    @JsonProperty("id_solicitante")
    private String idSolicitante; 
    @JsonProperty("tag_solicitante")
    private String tagSolicitante;
    @JsonProperty("foto_perfil_solicitante")
    private String fotoPerfilSolicitante;

    @JsonProperty("fecha_solicitud")
    private LocalDateTime fechaSolicitud; 

    @JsonProperty("estado")
    private String estado; // "pendiente" o "aceptada"

    public AmistadDTO() {}

    // Getters y Setters
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
    
    public String getFotoPerfilSolicitante() { 
        return fotoPerfilSolicitante; 
    }
    
    public void setFotoPerfilSolicitante(String foto) { 
        this.fotoPerfilSolicitante = foto; 
    }
    
    public LocalDateTime getFechaSolicitud() { 
        return fechaSolicitud; 
    }
    
    public void setFechaSolicitud(LocalDateTime fecha) { 
        this.fechaSolicitud = fecha; 
    }
    
    public String getEstado() { 
        return estado; 
    }
    
    public void setEstado(String estado) { 
        this.estado = estado; 
    }
}