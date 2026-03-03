package com.secretpanda.codenames.models;

import jakarta.persistence.*;

@Entity
@Table(name = "voto_carta", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"id_turno", "id_jugador_partida"})
})
public class VotoCarta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_voto")
    private Integer idVoto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_turno", nullable = false)
    private Turno turno;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_jugador_partida", nullable = false)
    private JugadorPartida jugadorPartida;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_carta_tablero", nullable = false)
    private TableroCarta cartaTablero;

    public VotoCarta() {}

    public Integer getIdVoto() { return idVoto; }
    public void setIdVoto(Integer idVoto) { this.idVoto = idVoto; }

    public Turno getTurno() { return turno; }
    public void setTurno(Turno turno) { this.turno = turno; }

    public JugadorPartida getJugadorPartida() { return jugadorPartida; }
    public void setJugadorPartida(JugadorPartida jugadorPartida) { this.jugadorPartida = jugadorPartida; }

    public TableroCarta getCartaTablero() { return cartaTablero; }
    public void setCartaTablero(TableroCarta cartaTablero) { this.cartaTablero = cartaTablero; }
}