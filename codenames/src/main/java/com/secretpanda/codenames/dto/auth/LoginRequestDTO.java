package com.secretpanda.codenames.dto.auth;

/**
 * Body que envía el cliente al endpoint POST /api/auth/login.
 *
 * Contrato API: { "id_google": "token_proporcionado_por_google" }
 *
 * El campo id_google contiene el idToken que devuelve el SDK de Google.
 * El backend lo verifica con GoogleAuthService.
 * No se incluye "tag": el registro es automático usando el nombre de Google.
 */
public class LoginRequestDTO {

    private String id_google;

    public LoginRequestDTO() {}

    public String getId_google() { return id_google; }

    public void setId_google(String id_google) { this.id_google = id_google; }
}