package com.secretpanda.codenames.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity 
@Table(name = "jugador") 
public class Jugador {

    @Id 
    @Column(name = "id_google", length = 255)
    private String idGoogle;

    @Column(nullable = false, unique = true, length = 100)
    private String tag;

    @Column(name = "foto_perfil", columnDefinition = "TEXT")
    private String fotoPerfil;

    @Column(nullable = false)
    private int balas = 0;

    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    @Column(name = "partidas_jugadas", nullable = false)
    private int partidasJugadas = 0;

    @Column(nullable = false)
    private int victorias = 0;

    @Column(name = "num_aciertos", nullable = false)
    private int numAciertos = 0;

    @Column(name = "num_fallos", nullable = false)
    private int numFallos = 0;

    public Jugador() {}

    @PrePersist
    protected void onCreate() {
        if (this.fechaRegistro == null) {
            this.fechaRegistro = LocalDateTime.now();
        }
    }

    public String getIdGoogle() { 
        return idGoogle;
    }

    public void setIdGoogle(String idGoogle) { 
        this.idGoogle = idGoogle;
    }
    
    public String getTag() { 
        return tag;
    }

    public void setTag(String tag) { 
        this.tag = tag;
    }

    public String getFotoPerfil() { 
        return fotoPerfil;
    }

    public void setFotoPerfil(String fotoPerfil) { 
        this.fotoPerfil = fotoPerfil;
    }

    public int getBalas() { 
        return balas;
    }

    public void setBalas(int balas) { 
        this.balas = balas;
    }

    public LocalDateTime getFechaRegistro() { 
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) { 
        this.fechaRegistro = fechaRegistro;
    }

    public int getPartidasJugadas() { 
        return partidasJugadas;
    }

    public void setPartidasJugadas(int partidasJugadas) { 
        this.partidasJugadas = partidasJugadas;
    }

    public int getVictorias() { 
        return victorias;
    }

    public void setVictorias(int victorias) { 
        this.victorias = victorias;
    }

    public int getNumAciertos() { 
        return numAciertos;
    }

    public void setNumAciertos(int numAciertos) { 
        this.numAciertos = numAciertos;
    }

    public int getNumFallos() { 
        return numFallos;
    }

    public void setNumFallos(int numFallos) { 
        this.numFallos = numFallos;
    }
}