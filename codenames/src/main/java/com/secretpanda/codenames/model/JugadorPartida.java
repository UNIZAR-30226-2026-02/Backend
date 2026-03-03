package com.secretpanda.codenames.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "jugador_partida", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"id_jugador", "id_partida"})
})
public class JugadorPartida {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_jugador_partida")
    private Integer idJugadorPartida;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_jugador", nullable = false)
    private Jugador jugador;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_partida", nullable = false)
    private Partida partida;

    @Column(nullable = false, length = 16)
    private String equipo; 

    @Column(nullable = false, length = 32)
    private String rol; 

    @Column(name = "num_fallos", nullable = false)
    private Integer numFallos = 0;

    @Column(name = "num_aciertos", nullable = false)
    private Integer numAciertos = 0;

    @Column(nullable = false)
    private Boolean abandono = false;

    public JugadorPartida() {}

    public Integer getIdJugadorPartida() { return idJugadorPartida; }
    public void setIdJugadorPartida(Integer idJugadorPartida) { this.idJugadorPartida = idJugadorPartida; }

    public Jugador getJugador() { return jugador; }
    public void setJugador(Jugador jugador) { this.jugador = jugador; }

    public Partida getPartida() { return partida; }
    public void setPartida(Partida partida) { this.partida = partida; }

    public String getEquipo() { return equipo; }
    public void setEquipo(String equipo) { this.equipo = equipo; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    public Integer getNumFallos() { return numFallos; }
    public void setNumFallos(Integer numFallos) { this.numFallos = numFallos; }

    public Integer getNumAciertos() { return numAciertos; }
    public void setNumAciertos(Integer numAciertos) { this.numAciertos = numAciertos; }

    public Boolean getAbandono() { return abandono; }
    public void setAbandono(Boolean abandono) { this.abandono = abandono; }
}