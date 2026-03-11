package com.secretpanda.codenames.dto.juego;

import com.secretpanda.codenames.model.TableroCarta;

public class CartaDTO {
    
    private Integer idCarta;
    private String palabra;
    private int fila;
    private int columna;
    private String estado; // "oculta" o "revelada"
    
    // IMPORTANTE SEGURIDAD: 
    // Si la carta está oculta para el agente, no revelamos su tipo (lo ponemos null).
    // Si la carta está revelada o el agente es el líder, sí mostramos su tipo real.
    private String tipo; 

    // Constructor vacío
    public CartaDTO() {}

    // Constructor con parámetros para coger los datos directamente
    public CartaDTO(TableroCarta carta, boolean esLider) {
        this.idCarta = carta.getIdCartaTablero();
        this.palabra = carta.getPalabra().getValor();
        this.fila = carta.getFila();
        this.columna = carta.getColumna();
        this.estado = carta.getEstado().name();
        
        // Comprobamos si debemos mostrar el tipo de la carta (el jugador es líder o la carta ya está revelada) o
        // si no podemos mostrarla (el jugador es agente y la carta está oculta)
        if (esLider || "revelada".equalsIgnoreCase(this.estado)) {
            this.tipo = carta.getTipo().name();
        } else {
            this.tipo = null;
        }
    }

    // Getters y Setters
    public Integer getIdCarta() { return idCarta; }
    public void setIdCarta(Integer idCarta) { this.idCarta = idCarta; }

    public String getPalabra() { return palabra; }
    public void setPalabra(String palabra) { this.palabra = palabra; }

    public int getFila() { return fila; }
    public void setFila(int fila) { this.fila = fila; }

    public int getColumna() { return columna; }
    public void setColumna(int columna) { this.columna = columna; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
}