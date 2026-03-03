package com.secretpanda.codenames.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "partida")
public class Partida {

    public enum EstadoPartida {
        ESPERANDO, EN_CURSO, FINALIZADA
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
    private Integer tiempoEspera = 60;

    @Column(name = "max_jugadores", nullable = false)
    private Integer maxJugadores = 8;

    @Column(name = "es_publica", nullable = false)
    private Boolean esPublica = true;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @Column(name = "fecha_fin")
    private LocalDateTime fechaFin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private EstadoPartida estado = EstadoPartida.ESPERANDO;

    @Column(name = "rojo_gana")
    private Boolean rojoGana;

    public Partida() {}

    public Integer getIdPartida() { return idPartida; }
    public void setIdPartida(Integer idPartida) { this.idPartida = idPartida; }

    public String getCodigoPartida() { return codigoPartida; }
    public void setCodigoPartida(String codigoPartida) { this.codigoPartida = codigoPartida; }

    public Tema getTema() { return tema; }
    public void setTema(Tema tema) { this.tema = tema; }

    public Jugador getCreador() { return creador; }
    public void setCreador(Jugador creador) { this.creador = creador; }

    public Integer getTiempoEspera() { return tiempoEspera; }
    public void setTiempoEspera(Integer tiempoEspera) { this.tiempoEspera = tiempoEspera; }

    public Integer getMaxJugadores() { return maxJugadores; }
    public void setMaxJugadores(Integer maxJugadores) { this.maxJugadores = maxJugadores; }

    public Boolean getEsPublica() { return esPublica; }
    public void setEsPublica(Boolean esPublica) { this.esPublica = esPublica; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public LocalDateTime getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDateTime fechaFin) { this.fechaFin = fechaFin; }

    public EstadoPartida getEstado() { return estado; }
    public void setEstado(EstadoPartida estado) { this.estado = estado; }

    public Boolean getRojoGana() { return rojoGana; }
    public void setRojoGana(Boolean rojoGana) { this.rojoGana = rojoGana; }
}