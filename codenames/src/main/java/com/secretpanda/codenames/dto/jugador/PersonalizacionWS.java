package com.secretpanda.codenames.dto.jugador;

public class PersonalizacionWS {
    private String tipo;
    private String valorVisual;
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