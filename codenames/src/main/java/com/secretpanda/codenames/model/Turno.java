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
@Table(name = "turno", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"id_partida", "num_turno"})
})
public class Turno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_turno")
    private Integer idTurno;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_partida", nullable = false)
    private Partida partida;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_jugador_partida", nullable = false)
    private JugadorPartida jugadorPartida;

    @Column(name = "num_turno", nullable = false)
    private int numTurno; // Primitivo para evitar nulos en la secuencia cronológica

    @Column(name = "palabra_pista", length = 256)
    private String palabraPista;

    @Column(name = "pista_numero")
    private Integer pistaNumero; // Se mantiene Integer porque es nulo hasta que el jefe de espías decide

    public Turno() {}
    
    // Getters y Setters
    public Integer getIdTurno() { return idTurno; }
    public void setIdTurno(Integer idTurno) { this.idTurno = idTurno; }

    public Partida getPartida() { return partida; }
    public void setPartida(Partida partida) { this.partida = partida; }

    public JugadorPartida getJugadorPartida() { return jugadorPartida; }
    public void setJugadorPartida(JugadorPartida jugadorPartida) { this.jugadorPartida = jugadorPartida; }

    public int getNumTurno() { return numTurno; }
    public void setNumTurno(int numTurno) { this.numTurno = numTurno; }

    public String getPalabraPista() { return palabraPista; }
    public void setPalabraPista(String palabraPista) { this.palabraPista = palabraPista; }

    public Integer getPistaNumero() { return pistaNumero; }
    public void setPistaNumero(Integer pistaNumero) { this.pistaNumero = pistaNumero; }
}