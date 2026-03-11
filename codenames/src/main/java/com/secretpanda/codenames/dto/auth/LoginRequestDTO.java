package com.secretpanda.codenames.dto.auth;

public class LoginRequestDTO {
    
    // Es el "token" que devuelve el SDK de Google al hacer el login 
    // Lo usaremos para verificar la identidad del usuario
    private String idToken;
    
    // Es el "nickname" que el usuario va a usar en la aplicación
    private String tag;

    // Contructor vacío
    public LoginRequestDTO() {}

    // Getters y Setters
    public String getIdToken() { return idToken; }

    public void setIdToken(String idToken) { this.idToken = idToken; }

    public String getTag() { return tag; }

    public void setTag(String tag) { this.tag = tag; }
}
