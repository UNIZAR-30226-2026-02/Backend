package com.secretpanda.codenames.models;

import jakarta.persistence.*;

@Entity
@Table(name = "inventario_personalizacion")
public class InventarioPersonalizacion {

    @EmbeddedId
    private InventarioPersonalizacionId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idJugador")
    @JoinColumn(name = "id_jugador")
    private Jugador jugador;

    @ManyToOne(fetch = FetchType.EAGER) // Eager para traer los datos del cosmético automáticamente
    @MapsId("idPersonalizacion")
    @JoinColumn(name = "id_personalizacion")
    private Personalizacion personalizacion;

    @Column(nullable = false)
    private Boolean equipado = false;

    public InventarioPersonalizacion() {}

    public InventarioPersonalizacionId getId() { return id; }
    public void setId(InventarioPersonalizacionId id) { this.id = id; }

    public Jugador getJugador() { return jugador; }
    public void setJugador(Jugador jugador) { this.jugador = jugador; }

    public Personalizacion getPersonalizacion() { return personalizacion; }
    public void setPersonalizacion(Personalizacion personalizacion) { this.personalizacion = personalizacion; }

    public Boolean getEquipado() { return equipado; }
    public void setEquipado(Boolean equipado) { this.equipado = equipado; }
}