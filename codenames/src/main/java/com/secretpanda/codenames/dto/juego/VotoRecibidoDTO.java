package com.secretpanda.codenames.dto.juego;

import java.util.List;
import java.util.Map;

/**
 * Respuesta inmediata al votar: mapa de idCartaTablero → lista de tags que votaron esa carta.
 * El frontend actualiza los contadores de votos en tiempo real.
 */
public class VotoRecibidoDTO {

    private Integer idTurno;
    // key: idCartaTablero, value: lista de tags de jugadores que votaron esa carta
    private Map<Integer, List<String>> votosPorCarta;

    public VotoRecibidoDTO() {}

    public Integer getIdTurno()                           { return idTurno; }
    public Map<Integer, List<String>> getVotosPorCarta()  { return votosPorCarta; }

    public void setIdTurno(Integer idTurno)                              { this.idTurno = idTurno; }
    public void setVotosPorCarta(Map<Integer, List<String>> votosPorCarta) {
        this.votosPorCarta = votosPorCarta;
    }
}
