package com.secretpanda.codenames.model;

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
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "jugador_partida", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"id_jugador", "id_partida"})
})
public class JugadorPartida {

    public enum Equipo {
        rojo, azul
    }

    public enum Rol {
        lider, agente
    }

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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private Equipo equipo; 

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private Rol rol; 

    @Column(name = "num_fallos", nullable = false)
    private int numFallos = 0;

    @Column(name = "num_aciertos", nullable = false)
    private int numAciertos = 0;

    @Column(nullable = false)
    private boolean abandono = false;

    public JugadorPartida() {}

    public Integer getIdJugadorPartida() { 
        return idJugadorPartida; 
    }

    public void setIdJugadorPartida(Integer idJugadorPartida) { 
        this.idJugadorPartida = idJugadorPartida; 
    }

    public Jugador getJugador() { 
        return jugador; 
    }

    public void setJugador(Jugador jugador) { 
        this.jugador = jugador; 
    }

    public Partida getPartida() { 
        return partida; 
    }

    public void setPartida(Partida partida) { 
        this.partida = partida; 
    }

    public Equipo getEquipo() { 
        return equipo; 
    }

    public void setEquipo(Equipo equipo) { 
        this.equipo = equipo; 
    }

    public Rol getRol() { 
        return rol; 
    }

    public void setRol(Rol rol) { 
        this.rol = rol; 
    }

    public int getNumFallos() { 
        return numFallos; 
    }

    public void setNumFallos(int numFallos) { 
        this.numFallos = numFallos; 
    }

    public int getNumAciertos() { 
        return numAciertos; 
    }

    public void setNumAciertos(int numAciertos) { 
        this.numAciertos = numAciertos; 
    }

    public boolean isAbandono() { 
        return abandono; 
    }

    public void setAbandono(boolean abandono) { 
        this.abandono = abandono; 
    }
}