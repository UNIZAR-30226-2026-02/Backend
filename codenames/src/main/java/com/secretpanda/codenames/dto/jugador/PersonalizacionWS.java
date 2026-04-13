package com.secretpanda.codenames.dto.jugador;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PersonalizacionWS {
    @JsonProperty("tipo")
    private String tipo;
    @JsonProperty("valor_visual")
    private String valorVisual;
    @JsonProperty("equipado")
    private boolean equipado;

    public PersonalizacionWS(String tipo, String valorVisual, boolean equipado) {
        this.tipo = tipo;
        this.valorVisual = valorVisual;
        this.equipado = equipado;
    }

    // Getters necesarios para Jackson
    public String getTipo() { 
        return tipo; 
    }

    public String getValorVisual() { 
        return valorVisual; 
    }
    
    public boolean isEquipado() { 
        return equipado; 
    }
}