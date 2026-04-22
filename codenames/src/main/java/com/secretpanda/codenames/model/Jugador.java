package com.secretpanda.codenames.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "jugador")
public class Jugador {

    @Id
    @Column(name = "id_google", length = 255)
    private String idGoogle;

    @Column(name = "tag", nullable = false, length = 50)
    private String tag;

    @Column(name = "foto_perfil", columnDefinition = "TEXT")
    private String fotoPerfil;

    @Column(name = "balas", nullable = false)
    private int balas = 0;

    @CreationTimestamp
    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    @Column(name = "activo", nullable = false)
    private boolean activo = true;

    @Column(name = "partidas_jugadas", nullable = false)
    private int partidasJugadas = 0;

    @Column(name = "victorias", nullable = false)
    private int victorias = 0;

    @Column(name = "num_aciertos", nullable = false)
    private int numAciertos = 0;

    @Column(name = "num_fallos", nullable = false)
    private int numFallos = 0;

    @OneToMany(mappedBy = "solicitante", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Amistad> amistadesEnviadas = new ArrayList<>();

    @OneToMany(mappedBy = "receptor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Amistad> amistadesRecibidas = new ArrayList<>();

    @OneToMany(mappedBy = "jugador", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InventarioPersonalizacion> inventario = new ArrayList<>();

    @OneToMany(mappedBy = "jugador", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InventarioTema> inventarioTemas = new ArrayList<>();

    @OneToMany(mappedBy = "jugador", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<JugadorLogro> logros = new ArrayList<>();

    @OneToMany(mappedBy = "jugador", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<JugadorPartida> historialPartidas = new ArrayList<>();

    public Jugador() {}

    // Helpers Bidireccionales
    public void addAmistadEnviada(Amistad amistad) {
        amistadesEnviadas.add(amistad);
        amistad.setSolicitante(this);
    }

    public void addAmistadRecibida(Amistad amistad) {
        amistadesRecibidas.add(amistad);
        amistad.setReceptor(this);
    }

    public void addJugadorPartida(JugadorPartida jugadorPartida) {
        historialPartidas.add(jugadorPartida);
        jugadorPartida.setJugador(this);
    }

    public void addInventarioTema(InventarioTema inventarioTema) {
        inventarioTemas.add(inventarioTema);
        inventarioTema.setJugador(this);
    }

    // Getters y Setters
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

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
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

    public List<Amistad> getAmistadesEnviadas() { 
        return amistadesEnviadas; 
    }

    public void setAmistadesEnviadas(List<Amistad> amistadesEnviadas) { 
        this.amistadesEnviadas = amistadesEnviadas; 
    }

    public List<Amistad> getAmistadesRecibidas() { 
        return amistadesRecibidas; 
    }

    public void setAmistadesRecibidas(List<Amistad> amistadesRecibidas) { 
        this.amistadesRecibidas = amistadesRecibidas; 
    }

    public List<InventarioPersonalizacion> getInventario() { 
        return inventario; 
    }

    public void setInventario(List<InventarioPersonalizacion> inventario) { 
        this.inventario = inventario; 
    }

    public List<InventarioTema> getInventarioTemas() {
        return inventarioTemas;
    }

    public void setInventarioTemas(List<InventarioTema> inventarioTemas) {
        this.inventarioTemas = inventarioTemas;
    }

    public List<JugadorLogro> getLogros() { 
        return logros; 
    }

    public void setLogros(List<JugadorLogro> logros) { 
        this.logros = logros; 
    }

    public List<JugadorPartida> getHistorialPartidas() { 
        return Collections.unmodifiableList(historialPartidas); 
    }

    public void setHistorialPartidas(List<JugadorPartida> historialPartidas) { 
        this.historialPartidas = historialPartidas; 
    }
}
