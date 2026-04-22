package com.secretpanda.codenames.event;

public class LogroEvent {
    private final String idJugador;
    private final String estadisticaClave;

    public LogroEvent(String idJugador, String estadisticaClave) {
        this.idJugador = idJugador;
        this.estadisticaClave = estadisticaClave;
    }

    public String getIdJugador() { return idJugador; }
    public String getEstadisticaClave() { return estadisticaClave; }
}
