package com.secretpanda.codenames.dto.jugador;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ActualizarPerfilDTO {
    
    // Solo necesitamos los campos que se pueden modificar
    @JsonProperty("tag")
    private String tag;
    @JsonProperty("foto_perfil")
    private String fotoPerfil;

    // Constructor vacío
    public ActualizarPerfilDTO() {}

    // Getters y Setters
    public String getTag() { return tag; }
    public void setTag(String tag) { this.tag = tag; }

    public String getFotoPerfil() { return fotoPerfil; } 
    public void setFotoPerfil(String fotoPerfil) { this.fotoPerfil = fotoPerfil; }
}