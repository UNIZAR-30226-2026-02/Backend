package com.secretpanda.codenames.dto.auth;

/**
 * Body para POST /api/auth/registro
 * { "id_google": "<sub de Google>", "tag": "NombreElegido" }
 */
public class RegistroRequestDTO {

    private String idGoogle;
    private String tag;

    public RegistroRequestDTO() {}

    public String getIdGoogle() { return idGoogle; }
    public void setIdGoogle(String idGoogle) { this.idGoogle = idGoogle; }

    public String getTag() { return tag; }
    public void setTag(String tag) { this.tag = tag; }
}
