package com.secretpanda.codenames.dto.partida;

/**
 * DTO para recibir la solicitud de creación de una nueva partida desde la app.
 */
public class CrearPartidaDTO {

    private String idCreador; 
    private Integer idTema; 
    private int tiempoEspera; 
    private int maxJugadores; 
    private boolean esPublica; 

    public CrearPartidaDTO() {
    }

    public String getIdCreador() { 
        return idCreador; 
    }

    public void setIdCreador(String idCreador) { 
        this.idCreador = idCreador; 
    }

    public Integer getIdTema() { 
        return idTema; 
    }

    public void setIdTema(Integer idTema) { 
        this.idTema = idTema; 
    }

    public int getTiempoEspera() { 
        return tiempoEspera; 
    }

    public void setTiempoEspera(int tiempoEspera) { 
        this.tiempoEspera = tiempoEspera; 
    }

    public int getMaxJugadores() { 
        return maxJugadores; 
    }

    public void setMaxJugadores(int maxJugadores) { 
        this.maxJugadores = maxJugadores; 
    }

    public boolean isEsPublica() { 
        return esPublica; 
    }

    public void setEsPublica(boolean esPublica) { 
        this.esPublica = esPublica; 
    }
}