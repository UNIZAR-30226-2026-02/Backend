package com.secretpanda.codenames.dto.juego;

/**
 * Emitido cada segundo a /topic/partidas/{id}/temporizador
 */
public class TemporizadorDTO {

    private Integer idPartida;
    private int segundosRestantes;

    public TemporizadorDTO() {}

    public TemporizadorDTO(Integer idPartida, int segundosRestantes) {
        this.idPartida = idPartida;
        this.segundosRestantes = segundosRestantes;
    }

    public Integer getIdPartida()       { return idPartida; }
    public int getSegundosRestantes()   { return segundosRestantes; }

    public void setIdPartida(Integer idPartida)             { this.idPartida = idPartida; }
    public void setSegundosRestantes(int segundosRestantes) { this.segundosRestantes = segundosRestantes; }
}
