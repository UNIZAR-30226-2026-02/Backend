package com.secretpanda.codenames.util;

import org.springframework.stereotype.Component;

@Component
public class EstadisticasCalculator {

    /**
     * Calcula la tasa de victorias (winrate) de un jugador como un porcentaje.
     * Redondeado a 2 decimales para evitar problemas de renderizado en el Frontend.
     *
     * @param partidasGanadas Cantidad de partidas ganadas.
     * @param partidasJugadas Cantidad total de partidas jugadas.
     * @return El porcentaje de winrate (ej: 33.33). Devuelve 0.0 si no hay partidas.
     */
    public double calcularWinrate(int partidasGanadas, int partidasJugadas) {
        if (partidasJugadas <= 0) {
            return 0.0;
        }
        double ratio = ((double) partidasGanadas / partidasJugadas) * 100.0;
        return Math.round(ratio * 100.0) / 100.0; 
    }

    /**
     * Calcula la precisión de aciertos de un jugador como un porcentaje.
     * Redondeado a 2 decimales.
     *
     * @param aciertos Cantidad de palabras que el jugador adivinó correctamente.
     * @param fallos Cantidad de palabras que el jugador falló.
     * @return El porcentaje de precisión (ej: 75.50). Devuelve 0.0 si no hay intentos.
     */
    public double calcularPrecision(int aciertos, int fallos) {
        int totalIntentos = aciertos + fallos;
        if (totalIntentos <= 0) {
            return 0.0;
        }
        double ratio = ((double) aciertos / totalIntentos) * 100.0;
        return Math.round(ratio * 100.0) / 100.0;
    }

    /**
     * Calcula las derrotas restando las victorias a las partidas jugadas.
     * Incluye un seguro para evitar números negativos en caso de error de BD.
     */
    public int calcularDerrotas(int partidasJugadas, int victorias) {
        return Math.max(0, partidasJugadas - victorias);
    }
}