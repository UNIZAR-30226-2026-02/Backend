package com.secretpanda.codenames.dto.partida;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Vista simplificada de un jugador en el lobby.
 * Los roles NO se muestran aquí (se asignan al iniciar la partida).
 */
public class JugadorLobbyDTO {

    @JsonProperty("tag")
    private String tag;
    @JsonProperty("foto_perfil")
    private String fotoPerfil;
    @JsonProperty("equipo")
    private String equipo;   // "rojo" o "azul"

    public JugadorLobbyDTO() {}

    public JugadorLobbyDTO(String tag, String fotoPerfil, String equipo) {
        this.tag = tag;
        this.fotoPerfil = fotoPerfil;
        this.equipo = equipo;
    }

    public String getTag()           { return tag; }
    public String getFotoPerfil()    { return fotoPerfil; }
    public String getEquipo()        { return equipo; }

    public void setTag(String tag)                  { this.tag = tag; }
    public void setFotoPerfil(String fotoPerfil)    { this.fotoPerfil = fotoPerfil; }
    public void setEquipo(String equipo)            { this.equipo = equipo; }
}
