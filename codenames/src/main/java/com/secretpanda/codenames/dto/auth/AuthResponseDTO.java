package com.secretpanda.codenames.dto.auth;

import com.secretpanda.codenames.dto.jugador.JugadorDTO;

/**
 * Respuesta del endpoint POST /api/auth/login.
 * Devuelve el token JWT y el objeto Maestro del Jugador con todas sus estadísticas.
 */
public class AuthResponseDTO {

    // JWT interno — para Android (React usa la cookie HttpOnly)
    private String token;

    // Objeto Maestro con toda la info del usuario
    private JugadorDTO jugador;

    public AuthResponseDTO() {}

    public AuthResponseDTO(String token, JugadorDTO jugador) {
        this.token = token;
        this.jugador = jugador;
    }

    // Getters
    public String getToken() { return token; }
    public JugadorDTO getJugador() { return jugador; }

    // Setters
    public void setToken(String token) { this.token = token; }
    public void setJugador(JugadorDTO jugador) { this.jugador = jugador; }
}