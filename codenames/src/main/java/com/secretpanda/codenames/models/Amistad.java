package com.secretpanda.codenames.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "amistad")
public class Amistad {

    public enum EstadoAmistad {
        PENDIENTE, ACEPTADA, RECHAZADA
    }

    @EmbeddedId // Indica que la clave primaria es un objeto incrustado
    private AmistadId id;

    // Relaciones con la tabla Jugador para obtener todos sus datos si es necesario
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
    private EstadoAmistad estado = EstadoAmistad.PENDIENTE;

    @Column(name = "fecha_solicitud", nullable = false, updatable = false)
    private LocalDateTime fechaSolicitud = LocalDateTime.now();

    public Amistad() {}

    // Getters y Setters
    public AmistadId getId() { return id; }
    public void setId(AmistadId id) { this.id = id; }

    public Jugador getSolicitante() { return solicitante; }
    public void setSolicitante(Jugador solicitante) { this.solicitante = solicitante; }

    public Jugador getReceptor() { return receptor; }
    public void setReceptor(Jugador receptor) { this.receptor = receptor; }

    public EstadoAmistad getEstado() { return estado; }
    public void setEstado(EstadoAmistad estado) { this.estado = estado; }

    public LocalDateTime getFechaSolicitud() { return fechaSolicitud; }
    public void setFechaSolicitud(LocalDateTime fechaSolicitud) { this.fechaSolicitud = fechaSolicitud; }
}