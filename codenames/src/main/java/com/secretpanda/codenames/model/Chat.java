package com.secretpanda.codenames.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "chat")
public class Chat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_mensaje")
    private Integer idMensaje;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_partida", nullable = false)
    private Partida partida;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_jugador_partida", nullable = false)
    private JugadorPartida jugadorPartida;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String mensaje;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime fecha;

    public Chat() {}

    public Integer getIdMensaje() { 
        return idMensaje; 
    }

    public void setIdMensaje(Integer idMensaje) { 
        this.idMensaje = idMensaje; 
    }

    public Partida getPartida() { 
        return partida; 
    }

    public void setPartida(Partida partida) { 
        this.partida = partida; 
    }

    public JugadorPartida getJugadorPartida() { 
        return jugadorPartida; 
    }

    public void setJugadorPartida(JugadorPartida jugadorPartida) { 
        this.jugadorPartida = jugadorPartida; 
    }

    public String getMensaje() { 
        return mensaje; 
    }

    public void setMensaje(String mensaje) { 
        this.mensaje = mensaje; 
    }

    public LocalDateTime getFecha() { 
        return fecha; 
    }

    public void setFecha(LocalDateTime fecha) { 
        this.fecha = fecha; 
    }
}