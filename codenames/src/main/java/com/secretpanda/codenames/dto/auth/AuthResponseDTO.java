package com.secretpanda.codenames.dto.auth;

import com.secretpanda.codenames.dto.jugador.JugadorDTO;

/**
 * Respuesta de /api/auth/login y /api/auth/registro.
 *
 * Login (esNuevo = true):  solo esNuevo=true, token=null, jugador=null
 * Login (esNuevo = false): esNuevo=false + token + jugador (con partidaActivaId)
 * Registro:                esNuevo=false + token + jugador
 */
public class AuthResponseDTO {

    private boolean esNuevo;

    // Null cuando esNuevo = true
    private String token;
    private JugadorDTO jugador;

    // Id de la partida EN CURSO a la que pertenece el jugador (null si no hay)
    private Integer partidaActivaId;

    public AuthResponseDTO() {}

    // Constructor para "jugador nuevo"
    public static AuthResponseDTO nuevo() {
        AuthResponseDTO r = new AuthResponseDTO();
        r.esNuevo = true;
        return r;
    }

    // Constructor para jugador existente o recién registrado
    public static AuthResponseDTO existente(String token, JugadorDTO jugador, Integer partidaActivaId) {
        AuthResponseDTO r = new AuthResponseDTO();
        r.esNuevo = false;
        r.token = token;
        r.jugador = jugador;
        r.partidaActivaId = partidaActivaId;
        return r;
    }

    // Getters
    public boolean isEsNuevo()        { return esNuevo; }
    public String getToken()          { return token; }
    public JugadorDTO getJugador()    { return jugador; }
    public Integer getPartidaActivaId() { return partidaActivaId; }

    // Setters (para Jackson)
    public void setEsNuevo(boolean esNuevo)              { this.esNuevo = esNuevo; }
    public void setToken(String token)                    { this.token = token; }
    public void setJugador(JugadorDTO jugador)            { this.jugador = jugador; }
    public void setPartidaActivaId(Integer partidaActivaId) { this.partidaActivaId = partidaActivaId; }
}
