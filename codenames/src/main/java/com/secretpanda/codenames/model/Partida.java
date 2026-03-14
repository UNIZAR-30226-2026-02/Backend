package com.secretpanda.codenames.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "partida")
public class Partida {

    public enum EstadoPartida {
        esperando, en_curso, finalizada
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_partida")
    private Integer idPartida;

    @Column(name = "codigo_partida", nullable = false, unique = true, length = 32)
    private String codigoPartida;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tema", nullable = false)
    private Tema tema;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_creador", nullable = false)
    private Jugador creador;

    @Column(name = "tiempo_espera", nullable = false)
    private int tiempoEspera = 60;

    @Column(name = "max_jugadores", nullable = false)
    private int maxJugadores = 8;

    @Column(name = "es_publica", nullable = false)
    private boolean esPublica = true;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_fin")
    private LocalDateTime fechaFin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private EstadoPartida estado;

    @Column(name = "rojo_gana")
    private Boolean rojoGana;

    public Partida() {}

    @PrePersist
    protected void onCreate() {
        if (this.fechaCreacion == null) {
            this.fechaCreacion = LocalDateTime.now();
        }
        if (this.estado == null) {
            this.estado = EstadoPartida.esperando;
        }

        if (this.maxJugadores < 4 || this.maxJugadores > 16) {
            throw new IllegalStateException("El número de jugadores debe estar entre 4 y 16");
        }
        if (this.tiempoEspera != 30 && this.tiempoEspera != 60 && this.tiempoEspera != 90 && this.tiempoEspera != 120) {
            throw new IllegalStateException("El tiempo de espera debe ser 30, 60, 90 o 120 segundos");
        }
    }

    public Integer getIdPartida() { 
        return idPartida; 
    }

    public void setIdPartida(Integer idPartida) { 
        this.idPartida = idPartida; 
    }

    public String getCodigoPartida() { 
        return codigoPartida; 
    }

    public void setCodigoPartida(String codigoPartida) { 
        this.codigoPartida = codigoPartida; 
    }

    public Tema getTema() { 
        return tema; 
    }

    public void setTema(Tema tema) { 
        this.tema = tema; 
    }

    public Jugador getCreador() { 
        return creador; 
    }

    public void setCreador(Jugador creador) { 
        this.creador = creador; 
    }

    public int getTiempoEspera() { 
        return tiempoEspera; 
    }

    public void setTiempoEspera(int tiempoEspera) { 
        this.tiempoEspera = tiempoEspera; 
    }

    public int getMaxJugadores() { 
        return maxJugadores; 
    }

    public void setMaxJugadores(int maxJugadores) { 
        this.maxJugadores = maxJugadores; 
    }

    public boolean isEsPublica() { 
        return esPublica; 
    }

    public void setEsPublica(boolean esPublica) { 
        this.esPublica = esPublica; 
    }

    public LocalDateTime getFechaCreacion() { 
        return fechaCreacion; 
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) { 
        this.fechaCreacion = fechaCreacion; 
    }

    public LocalDateTime getFechaFin() { 
        return fechaFin; 
    }

    public void setFechaFin(LocalDateTime fechaFin) { 
        this.fechaFin = fechaFin; 
    }

    public EstadoPartida getEstado() { 
        return estado; 
    }

    public void setEstado(EstadoPartida estado) { 
        this.estado = estado; 
    }

    public Boolean getRojoGana() { 
        return rojoGana; 
    }

    public void setRojoGana(Boolean rojoGana) { 
        this.rojoGana = rojoGana; 
    }
}