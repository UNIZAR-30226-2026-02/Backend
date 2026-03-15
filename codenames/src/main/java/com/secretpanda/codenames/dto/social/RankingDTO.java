package com.secretpanda.codenames.dto.social;

/**
 * DTO para mostrar a un jugador en la tabla de clasificación.
 */
public class RankingDTO {

    private String tag;
    private String fotoPerfil; 
    private int victorias;

    public RankingDTO() {
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getFotoPerfil() { 
        return fotoPerfil; 
    }

    public void setFotoPerfil(String fotoPerfil) { 
        this.fotoPerfil = fotoPerfil; 
    }

    public int getVictorias() {
        return victorias;
    }

    public void setVictorias(int victorias) {
        this.victorias = victorias;
    }
}