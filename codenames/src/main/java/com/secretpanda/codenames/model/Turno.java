package com.secretpanda.codenames.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
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
    private int numTurno;

    @Column(name = "palabra_pista", nullable = false, length = 256)
    private String palabraPista;

    @Column(name = "pista_numero", nullable = false)
    private int pistaNumero;

    // NUEVO: Relación para el RF-16 (Contabilizar votos en tiempo real)
    @OneToMany(mappedBy = "turno", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VotoCarta> votos = new ArrayList<>();

    public Turno() {}

    // Helper Bidireccional
    public void addVoto(VotoCarta voto) {
        votos.add(voto);
        voto.setTurno(this);
    }
    
    // Getters y Setters
    public Integer getIdTurno() { 
        return idTurno; 
    }

    public void setIdTurno(Integer idTurno) { 
        this.idTurno = idTurno; 
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

    public int getNumTurno() { 
        return numTurno; 
    }

    public void setNumTurno(int numTurno) { 
        this.numTurno = numTurno; 
    }

    public String getPalabraPista() { 
        return palabraPista; 
    }

    public void setPalabraPista(String palabraPista) { 
        this.palabraPista = palabraPista; 
    }

    public int getPistaNumero() { 
        return pistaNumero;
    }

    public void setPistaNumero(int pistaNumero) { 
        this.pistaNumero = pistaNumero; 
    }

    public List<VotoCarta> getVotos() { 
        return votos; 
    }

    public void setVotos(List<VotoCarta> votos) { 
        this.votos = votos; 
    }
}