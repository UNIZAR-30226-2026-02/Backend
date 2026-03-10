package com.secretpanda.codenames.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

@Entity
@Table(name = "jugador_logro")
public class JugadorLogro {

    @EmbeddedId
    private JugadorLogroId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idJugador")
    @JoinColumn(name = "id_jugador")
    private Jugador jugador;

    @ManyToOne(fetch = FetchType.EAGER)
    @MapsId("idLogro")
    @JoinColumn(name = "id_logro")
    private Logro logro;

    @Column(name = "progreso_actual", nullable = false)
    private int progresoActual = 0;

    @Column(nullable = false)
    private boolean completado = false;

    @Column(name = "fecha_desbloqueo")
    private LocalDateTime fechaDesbloqueo;

    public JugadorLogro() {}

    // Getters y Setters
    public JugadorLogroId getId() { return id; }
    public void setId(JugadorLogroId id) { this.id = id; }

    public Jugador getJugador() { return jugador; }
    public void setJugador(Jugador jugador) { this.jugador = jugador; }

    public Logro getLogro() { return logro; }
    public void setLogro(Logro logro) { this.logro = logro; }

    public int getProgresoActual() { return progresoActual; }
    public void setProgresoActual(int progresoActual) { this.progresoActual = progresoActual; }

    public boolean isCompletado() { return completado; }
    public void setCompletado(boolean completado) { this.completado = completado; }

    public LocalDateTime getFechaDesbloqueo() { return fechaDesbloqueo; }
    public void setFechaDesbloqueo(LocalDateTime fechaDesbloqueo) { this.fechaDesbloqueo = fechaDesbloqueo; }
}