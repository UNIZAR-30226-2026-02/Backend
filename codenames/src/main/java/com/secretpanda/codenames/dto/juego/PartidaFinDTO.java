package com.secretpanda.codenames.dto.juego;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PartidaFinDTO {
    
    @JsonProperty("equipo_ganador")
    private String equipoGanador;
    
    @JsonProperty("aciertos_rojo")
    private int aciertosRojo;
    
    @JsonProperty("aciertos_azul")
    private int aciertosAzul;

    // Getters y Setters
    public String getEquipoGanador() { return equipoGanador; }
    public void setEquipoGanador(String equipoGanador) { this.equipoGanador = equipoGanador; }
    public int getAciertosRojo() { return aciertosRojo; }
    public void setAciertosRojo(int aciertosRojo) { this.aciertosRojo = aciertosRojo; }
    public int getAciertosAzul() { return aciertosAzul; }
    public void setAciertosAzul(int aciertosAzul) { this.aciertosAzul = aciertosAzul; }
}