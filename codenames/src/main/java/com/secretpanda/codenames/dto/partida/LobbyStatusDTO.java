package com.secretpanda.codenames.dto.partida;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * DTO del estado del lobby. Usado tanto en REST (GET /lobby) como en WS (/topic/.../lobby).
 *
 * Campos añadidos respecto a la versión anterior:
 *   - tagCreador    : para que el frontend sepa si el usuario actual es el creador
 *   - hayMinimo     : true si los 2 equipos tienen >= 2 jugadores (para habilitar "Iniciar")
 *   - tiempoEspera  : tiempo de turno configurado
 */
public class LobbyStatusDTO {

    @JsonProperty("id_partida")
    private Integer idPartida;
    @JsonProperty("codigo_partida")
    private String codigoPartida;
    @JsonProperty("estado")
    private String estado;           // "esperando", "en_curso", "finalizada"
    @JsonProperty("max_jugadores")
    private int maxJugadores;
    @JsonProperty("es_publica")
    private boolean esPublica;
    @JsonProperty("id_tema")
    private Integer idTema;
    @JsonProperty("nombre_tema")
    private String nombreTema;
    @JsonProperty("tiempo_espera")
    private int tiempoEspera;
    @JsonProperty("tag_creador")
    private String tagCreador;       // NUEVO
    @JsonProperty("hay_minimo")
    private boolean hayMinimo;       // NUEVO: ambos equipos tienen >= 2 jugadores

    @JsonProperty("jugadores")
    private List<JugadorLobbyDTO> jugadores;  // Simplificado para el lobby

    public LobbyStatusDTO() {}

    // ─── Getters ──────────────────────────────────────────────────────────────

    public Integer getIdPartida()       { return idPartida; }
    public String getCodigoPartida()    { return codigoPartida; }
    public String getEstado()           { return estado; }
    public int getMaxJugadores()        { return maxJugadores; }
    public boolean isEsPublica()        { return esPublica; }
    public Integer getIdTema()          { return idTema; }
    public String getNombreTema()       { return nombreTema; }
    public int getTiempoEspera()        { return tiempoEspera; }
    public String getTagCreador()       { return tagCreador; }
    public boolean isHayMinimo()        { return hayMinimo; }
    public List<JugadorLobbyDTO> getJugadores() { return jugadores; }

    // ─── Setters ──────────────────────────────────────────────────────────────

    public void setIdPartida(Integer idPartida)           { this.idPartida = idPartida; }
    public void setCodigoPartida(String codigoPartida)    { this.codigoPartida = codigoPartida; }
    public void setEstado(String estado)                  { this.estado = estado; }
    public void setMaxJugadores(int maxJugadores)         { this.maxJugadores = maxJugadores; }
    public void setEsPublica(boolean esPublica)           { this.esPublica = esPublica; }
    public void setIdTema(Integer idTema)                 { this.idTema = idTema; }
    public void setNombreTema(String nombreTema)          { this.nombreTema = nombreTema; }
    public void setTiempoEspera(int tiempoEspera)         { this.tiempoEspera = tiempoEspera; }
    public void setTagCreador(String tagCreador)          { this.tagCreador = tagCreador; }
    public void setHayMinimo(boolean hayMinimo)           { this.hayMinimo = hayMinimo; }
    public void setJugadores(List<JugadorLobbyDTO> jugadores) { this.jugadores = jugadores; }
}
