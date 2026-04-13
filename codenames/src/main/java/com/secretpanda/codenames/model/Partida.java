package com.secretpanda.codenames.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@Entity
@Table(name = "partida")
public class Partida {

    public enum EstadoPartida {
        esperando, en_curso, finalizada
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_partida")
    private Integer idPartida;

    @Column(name = "codigo_partida", nullable = false, unique = true, length = 32)
    private String codigoPartida;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tema", nullable = false)
    private Tema tema;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_creador", nullable = false)
    private Jugador creador;

    @Column(name = "tiempo_espera", nullable = false)
    private int tiempoEspera = 60;

    @Min(4)
    @Max(16)
    @Column(name = "max_jugadores", nullable = false)
    private int maxJugadores = 8;

    @Column(name = "es_publica", nullable = false)
    private boolean esPublica = true;

    @CreationTimestamp
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_fin")
    private LocalDateTime fechaFin;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 32)
    private EstadoPartida estado;

    @Column(name = "rojo_gana")
    private Boolean rojoGana;

    @OneToMany(mappedBy = "partida", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<JugadorPartida> jugadores = new ArrayList<>();

    @OneToMany(mappedBy = "partida", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TableroCarta> cartasTablero = new ArrayList<>();

    @OneToMany(mappedBy = "partida", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Turno> turnos = new ArrayList<>();

    @OneToMany(mappedBy = "partida", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Chat> mensajesChat = new ArrayList<>();

    public Partida() {}

    @PrePersist
    protected void onCreate() {
        if (this.estado == null) {
            this.estado = EstadoPartida.esperando;
        }
    }

    // Helpers Bidireccionales
    public void addJugador(JugadorPartida jugadorPartida) {
        jugadores.add(jugadorPartida);
        jugadorPartida.setPartida(this);
    }

    public void addCartaTablero(TableroCarta carta) {
        cartasTablero.add(carta);
        carta.setPartida(this);
    }

    public void addTurno(Turno turno) {
        turnos.add(turno);
        turno.setPartida(this);
    }

    public void addMensajeChat(Chat chat) {
        mensajesChat.add(chat);
        chat.setPartida(this);
    }

    // Getters y Setters
    public Integer getIdPartida() { 
        return idPartida; 
    }

    public void setIdPartida(Integer idPartida) { 
        this.idPartida = idPartida; 
    }

    public String getCodigoPartida() { 
        return codigoPartida; 
    }

    public void setCodigoPartida(String codigoPartida) { 
        this.codigoPartida = codigoPartida; 
    }

    public Tema getTema() { 
        return tema; 
    }

    public void setTema(Tema tema) { 
        this.tema = tema; 
    }

    public Jugador getCreador() { 
        return creador; 
    }

    public void setCreador(Jugador creador) { 
        this.creador = creador; 
    }

    public int getTiempoEspera() { 
        return tiempoEspera; 
    }

    public void setTiempoEspera(int tiempoEspera) { 
        this.tiempoEspera = tiempoEspera; 
    }

    public int getMaxJugadores() { 
        return maxJugadores; 
    }

    public void setMaxJugadores(int maxJugadores) { 
        this.maxJugadores = maxJugadores; 
    }

    public boolean isEsPublica() { 
        return esPublica; 
    }

    public void setEsPublica(boolean esPublica) { 
        this.esPublica = esPublica; 
    }

    public LocalDateTime getFechaCreacion() { 
        return fechaCreacion; 
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) { 
        this.fechaCreacion = fechaCreacion; 
    }

    public LocalDateTime getFechaFin() { 
        return fechaFin; 
    }

    public void setFechaFin(LocalDateTime fechaFin) { 
        this.fechaFin = fechaFin; 
    }

    public EstadoPartida getEstado() { 
        return estado; 
    }

    public void setEstado(EstadoPartida estado) { 
        this.estado = estado; 
    }

    public Boolean getRojoGana() { 
        return rojoGana; 
    }

    public void setRojoGana(Boolean rojoGana) { 
        this.rojoGana = rojoGana; 
    }

    public List<JugadorPartida> getJugadores() { 
        return jugadores; 
    }

    public void setJugadores(List<JugadorPartida> jugadores) { 
        this.jugadores = jugadores; 
    }

    public List<TableroCarta> getCartasTablero() { 
        return cartasTablero; 
    }

    public void setCartasTablero(List<TableroCarta> cartasTablero) { 
        this.cartasTablero = cartasTablero; 
    }

    public List<Turno> getTurnos() { 
        return turnos; 
    }

    public void setTurnos(List<Turno> turnos) { 
        this.turnos = turnos; 
    }

    public List<Chat> getMensajesChat() { 
        return mensajesChat; 
    }

    public void setMensajesChat(List<Chat> mensajesChat) { 
        this.mensajesChat = mensajesChat; 
    }
}