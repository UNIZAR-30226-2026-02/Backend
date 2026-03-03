package com.secretpanda.codenames.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "jugador_logro")
public class JugadorLogro {

    @EmbeddedId
    private JugadorLogroId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idJugador")
    @JoinColumn(name = "id_jugador")
    private Jugador jugador;

    @ManyToOne(fetch = FetchType.EAGER) // Eager porque casi siempre querremos saber qué logro es
    @MapsId("idLogro")
    @JoinColumn(name = "id_logro")
    private Logro logro;

    @Column(name = "progreso_actual", nullable = false)
    private Integer progresoActual = 0;

    @Column(nullable = false)
    private Boolean completado = false;

    @Column(name = "fecha_desbloqueo")
    private LocalDateTime fechaDesbloqueo;

    public JugadorLogro() {}

    public JugadorLogroId getId() { return id; }
    public void setId(JugadorLogroId id) { this.id = id; }

    public Jugador getJugador() { return jugador; }
    public void setJugador(Jugador jugador) { this.jugador = jugador; }

    public Logro getLogro() { return logro; }
    public void setLogro(Logro logro) { this.logro = logro; }

    public Integer getProgresoActual() { return progresoActual; }
    public void setProgresoActual(Integer progresoActual) { this.progresoActual = progresoActual; }

    public Boolean getCompletado() { return completado; }
    public void setCompletado(Boolean completado) { this.completado = completado; }

    public LocalDateTime getFechaDesbloqueo() { return fechaDesbloqueo; }
    public void setFechaDesbloqueo(LocalDateTime fechaDesbloqueo) { this.fechaDesbloqueo = fechaDesbloqueo; }
}