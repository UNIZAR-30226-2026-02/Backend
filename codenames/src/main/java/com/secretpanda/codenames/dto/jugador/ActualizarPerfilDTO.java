package com.secretpanda.codenames.dto.jugador;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ActualizarPerfilDTO {
    
    // Solo necesitamos los campos que se pueden modificar
    @NotBlank(message = "El tag no puede estar vacío")
    @Size(min = 3, max = 20, message = "El tag debe tener entre 3 y 20 caracteres")
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