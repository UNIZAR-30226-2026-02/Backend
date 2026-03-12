package com.secretpanda.codenames.util;

import org.springframework.stereotype.Component;

@Component
public class EstadisticasCalculator {

    /**
     * Calcula la tasa de victorias (winrate) de un jugador como un porcentaje.
     *
     * @param partidasGanadas Cantidad de partidas ganadas.
     * @param partidasJugadas Cantidad total de partidas jugadas.
     * @return El porcentaje de winrate (de 0.0 a 100.0). Devuelve 0.0 si no hay partidas jugadas.
     */
    public double calcularWinrate(int partidasGanadas, int partidasJugadas) {
        if (partidasJugadas <= 0) {
            return 0.0;
        }
        return ((double) partidasGanadas / partidasJugadas) * 100.0;
    }

    /**
     * Calcula la precisión de aciertos de un jugador como un porcentaje.
     *
     * @param palabrasAcertadas Cantidad de palabras que el jugador adivinó correctamente.
     * @param totalIntentos Cantidad total de palabras que el jugador intentó adivinar.
     * @return El porcentaje de precisión (de 0.0 a 100.0). Devuelve 0.0 si no hay intentos.
     */
    public double calcularPrecision(int palabrasAcertadas, int totalIntentos) {
        if (totalIntentos <= 0) {
            return 0.0;
        }
        return ((double) palabrasAcertadas / totalIntentos) * 100.0;
    }
}