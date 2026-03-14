package com.secretpanda.codenames.model;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

@Entity
@Table(name = "inventario_personalizacion")
public class InventarioPersonalizacion {

    @EmbeddedId
    private InventarioPersonalizacionId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idJugador")
    @JoinColumn(name = "id_jugador")
    private Jugador jugador;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idPersonalizacion")
    @JoinColumn(name = "id_personalizacion")
    private Personalizacion personalizacion;

    @Column(nullable = false)
    private boolean equipado = false;

    public InventarioPersonalizacion() {}

    public InventarioPersonalizacionId getId() { 
        return id; 
    }

    public void setId(InventarioPersonalizacionId id) { 
        this.id = id; 
    }

    public Jugador getJugador() { 
        return jugador; 
    }

    public void setJugador(Jugador jugador) { 
        this.jugador = jugador; 
    }

    public Personalizacion getPersonalizacion() { 
        return personalizacion; 
    }

    public void setPersonalizacion(Personalizacion personalizacion) { 
        this.personalizacion = personalizacion; 
    }

    public boolean isEquipado() { 
        return equipado; 
    }

    public void setEquipado(boolean equipado) { 
        this.equipado = equipado; 
    }
}