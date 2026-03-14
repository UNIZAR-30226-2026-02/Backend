package com.secretpanda.codenames.dto.auth;

import com.secretpanda.codenames.model.Jugador;

/**
 * Respuesta del endpoint POST /api/auth/login.
 *
 * Contrato API: "Respuesta (200 OK): Objeto JUGADOR completo."
 *
 * Devuelve todos los campos del JUGADOR más el token JWT interno
 * que el cliente Android usa en el header Authorization: Bearer <token>.
 * React usa la cookie HttpOnly emitida por AuthController.
 */
public class AuthResponseDTO {

    // JWT interno — para Android (React usa la cookie HttpOnly)
    private String token;

    // Todos los campos del JUGADOR 
    private String idGoogle;
    private String tag;
    private String fotoPerfil;
    private int balas;
    private int partidasJugadas;
    private int victorias;
    private int numAciertos;
    private int numFallos;

    public AuthResponseDTO() {}

    public AuthResponseDTO(String token, Jugador jugador) {
        this.token           = token;
        this.idGoogle        = jugador.getIdGoogle();
        this.tag             = jugador.getTag();
        this.fotoPerfil      = jugador.getFotoPerfil();
        this.balas           = jugador.getBalas();
        this.partidasJugadas = jugador.getPartidasJugadas();
        this.victorias       = jugador.getVictorias();
        this.numAciertos     = jugador.getNumAciertos();
        this.numFallos       = jugador.getNumFallos();
    }

    // Getters
    public String getToken()           { return token; }
    public String getIdGoogle()        { return idGoogle; }
    public String getTag()             { return tag; }
    public String getFotoPerfil()      { return fotoPerfil; }
    public int getBalas()              { return balas; }
    public int getPartidasJugadas()    { return partidasJugadas; }
    public int getVictorias()          { return victorias; }
    public int getNumAciertos()        { return numAciertos; }
    public int getNumFallos()          { return numFallos; }

    // Setters
    public void setToken(String token)               { this.token = token; }
    public void setIdGoogle(String idGoogle)         { this.idGoogle = idGoogle; }
    public void setTag(String tag)                   { this.tag = tag; }
    public void setFotoPerfil(String fotoPerfil)     { this.fotoPerfil = fotoPerfil; }
    public void setBalas(int balas)                  { this.balas = balas; }
    public void setPartidasJugadas(int p)            { this.partidasJugadas = p; }
    public void setVictorias(int victorias)          { this.victorias = victorias; }
    public void setNumAciertos(int n)                { this.numAciertos = n; }
    public void setNumFallos(int n)                  { this.numFallos = n; }
}