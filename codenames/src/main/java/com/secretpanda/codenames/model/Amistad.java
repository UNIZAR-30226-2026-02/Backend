package com.secretpanda.codenames.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "amistad")
public class Amistad {
    
    public enum EstadoAmistad {
        pendiente, aceptada
    }

    @EmbeddedId 
    private AmistadId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idSolicitante")
    @JoinColumn(name = "id_solicitante")
    private Jugador solicitante;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idReceptor")
    @JoinColumn(name = "id_receptor")
    private Jugador receptor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private EstadoAmistad estado;

    @CreationTimestamp
    @Column(name = "fecha_solicitud", nullable = false, updatable = false)
    private LocalDateTime fechaSolicitud;

    public Amistad() {}

    @PrePersist
    protected void onCreate() {
        if (this.estado == null) {
            this.estado = EstadoAmistad.pendiente;
        }
        if (solicitante != null && receptor != null && solicitante.getIdGoogle().equals(receptor.getIdGoogle())) {
            throw new IllegalStateException("Un jugador no puede enviarse una solicitud de amistad a sí mismo");
        }
    }

    public AmistadId getId() { 
        return id; 
    }

    public void setId(AmistadId id) { 
        this.id = id; 
    }

    public Jugador getSolicitante() { 
        return solicitante; 
    }

    public void setSolicitante(Jugador solicitante) { 
        this.solicitante = solicitante; 
    }

    public Jugador getReceptor() { 
        return receptor; 
    }

    public void setReceptor(Jugador receptor) { 
        this.receptor = receptor; 
    }

    public EstadoAmistad getEstado() { 
        return estado; 
    }

    public void setEstado(EstadoAmistad estado) { 
        this.estado = estado; 
    }

    public LocalDateTime getFechaSolicitud() { 
        return fechaSolicitud; 
    }

    public void setFechaSolicitud(LocalDateTime fechaSolicitud) { 
        this.fechaSolicitud = fechaSolicitud; 
    }
}