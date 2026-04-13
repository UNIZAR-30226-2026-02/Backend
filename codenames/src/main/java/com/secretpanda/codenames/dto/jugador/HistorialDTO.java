package com.secretpanda.codenames.dto.jugador;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

import com.secretpanda.codenames.dto.partida.PartidaResumenDTO;

/**
 * DTO para devolver el historial de partidas del jugador de forma paginada.
 */
public class HistorialDTO {

    @JsonProperty("partidas")
    private List<PartidaResumenDTO> partidas;
    @JsonProperty("pagina_actual")
    private int paginaActual;
    @JsonProperty("total_paginas")
    private int totalPaginas;
    @JsonProperty("total_partidas")
    private long totalPartidas;

    public HistorialDTO() {
    }

    // Getters
    public List<PartidaResumenDTO> getPartidas() { return partidas; }
    public int getPaginaActual() { return paginaActual; }
    public int getTotalPaginas() { return totalPaginas; }
    public long getTotalPartidas() { return totalPartidas; }

    // Setters
    public void setPartidas(List<PartidaResumenDTO> partidas) { this.partidas = partidas; }
    public void setPaginaActual(int paginaActual) { this.paginaActual = paginaActual; }
    public void setTotalPaginas(int totalPaginas) { this.totalPaginas = totalPaginas; }
    public void setTotalPartidas(long totalPartidas) { this.totalPartidas = totalPartidas; }
}