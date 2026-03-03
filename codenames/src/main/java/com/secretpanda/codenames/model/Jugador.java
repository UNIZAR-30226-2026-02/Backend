package com.secretpanda.codenames.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity 
@Table(name = "jugador") 
public class Jugador {

    @Id 
    @Column(name = "id_google", length = 2048)
    private String idGoogle;

    @Column(nullable = false, unique = true, length = 100)
    private String tag;

    @Column(name = "foto_perfil", columnDefinition = "TEXT")
    private String fotoPerfil;

    @Column(nullable = false)
    private Integer balas = 0;

    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro = LocalDateTime.now();

    @Column(name = "partidas_jugadas", nullable = false)
    private Integer partidasJugadas = 0;

    @Column(nullable = false)
    private Integer victorias = 0;

    @Column(name = "num_aciertos", nullable = false)
    private Integer numAciertos = 0;

    @Column(name = "num_fallos", nullable = false)
    private Integer numFallos = 0;

    // CORRECCIÓN: Añadido el contador histórico de abandonos
    @Column(nullable = false)
    private Integer abandonos = 0;

    public Jugador() {}

    // Getters y Setters
    public String getIdGoogle() { return idGoogle; }
    public void setIdGoogle(String idGoogle) { this.idGoogle = idGoogle; }
    
    public String getTag() { return tag; }
    public void setTag(String tag) { this.tag = tag; }

    public String getFotoPerfil() { return fotoPerfil; }
    public void setFotoPerfil(String fotoPerfil) { this.fotoPerfil = fotoPerfil; }

    public Integer getBalas() { return balas; }
    public void setBalas(Integer balas) { this.balas = balas; }

    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    public Integer getPartidasJugadas() { return partidasJugadas; }
    public void setPartidasJugadas(Integer partidasJugadas) { this.partidasJugadas = partidasJugadas; }

    public Integer getVictorias() { return victorias; }
    public void setVictorias(Integer victorias) { this.victorias = victorias; }

    public Integer getNumAciertos() { return numAciertos; }
    public void setNumAciertos(Integer numAciertos) { this.numAciertos = numAciertos; }

    public Integer getNumFallos() { return numFallos; }
    public void setNumFallos(Integer numFallos) { this.numFallos = numFallos; }

    public Integer getAbandonos() { return abandonos; }
    public void setAbandonos(Integer abandonos) { this.abandonos = abandonos; }
}