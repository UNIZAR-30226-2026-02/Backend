package com.secretpanda.codenames.dto.jugador;

import com.secretpanda.codenames.model.Jugador;

public class JugadorDTO {
    
    private String idGoogle; // ID del jugador
    private String tag;
    private String fotoPerfil;

    // Constructor vacío
    public JugadorDTO() {}

    // Constructor que coge los datos del jugador
    public JugadorDTO(Jugador jugador) {
        this.idGoogle = jugador.getIdGoogle();
        this.tag = jugador.getTag();
        this.fotoPerfil = jugador.getFotoPerfil();
    }

    // Getters y Setters
    public String getIdGoogle() { return idGoogle; }
    public void setIdGoogle(String idGoogle) { this.idGoogle = idGoogle; }

    public String getTag() { return tag; }
    public void setTag(String tag) { this.tag = tag; }

    public String getFotoPerfil() { return fotoPerfil; }
    public void setFotoPerfil(String fotoPerfil) { this.fotoPerfil = fotoPerfil; }
}
