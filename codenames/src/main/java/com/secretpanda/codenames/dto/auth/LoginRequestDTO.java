package com.secretpanda.codenames.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Body que envía el cliente al endpoint POST /api/auth/login.
 *
 * Contrato API: { "id_google": "token_proporcionado_por_google" }
 *
 * El campo idGoogle contiene el idToken que devuelve el SDK de Google.
 * El backend lo verifica con GoogleAuthService.
 * No se incluye "tag": el registro es automático usando el nombre de Google.
 */
public class LoginRequestDTO {

    @JsonProperty("id_google")
    private String idGoogle;

    public LoginRequestDTO() {}

    public String getIdGoogle() { return idGoogle; }

    public void setIdGoogle(String idGoogle) { this.idGoogle = idGoogle; }
}