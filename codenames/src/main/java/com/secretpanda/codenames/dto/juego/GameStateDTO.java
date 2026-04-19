package com.secretpanda.codenames.dto.juego;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class GameStateDTO {

    @JsonProperty("id_partida")
    private Integer idPartida;

    @JsonProperty("estado")
    private String estado; // "esperando", "en_curso" o "finalizada"
    
    // Estado del Turno Actual
    @JsonProperty("equipo_turno_actual")
    private String equipoTurnoActual; // "rojo" ó "azul"

    @JsonProperty("fase_turno")
    private String faseTurno; // "esperando_pista" (le toca al lider) o "votando" (le toca a los agentes)
    
    @JsonProperty("cartas_rojas_restantes")
    private int cartasRojasRestantes;

    @JsonProperty("cartas_azules_restantes")
    private int cartasAzulesRestantes;
    
    @JsonProperty("rojo_gana")
    private Boolean rojoGana;

    @JsonProperty("segundos_restantes")
    private Integer segundosRestantes;

    // Componentes del juego
    @JsonProperty("pista_actual")
    private PistaDTO pistaActual; // Puede ser null si el líder aún no la ha introducido

    @JsonProperty("tablero")
    private TableroDTO tablero;

    @JsonProperty("votos_turno_actual")
    private List<VotoDTO> votosTurnoActual; // Votos emitidos en el turno actual

    // Constructor vacío
    public GameStateDTO() {}

    // Getters y Setters
    public Integer getIdPartida() { return idPartida; } 
    public void setIdPartida(Integer idPartida) { this.idPartida = idPartida; } 

    public String getEstado() { return estado; } 
    public void setEstado(String estado) { this.estado = estado; } 

    public String getEquipoTurnoActual() { return equipoTurnoActual; }
    public void setEquipoTurnoActual(String equipoTurnoActual) { this.equipoTurnoActual = equipoTurnoActual; }

    public String getFaseTurno() { return faseTurno; }
    public void setFaseTurno(String faseTurno) { this.faseTurno = faseTurno; }

    public int getCartasRojasRestantes() { return cartasRojasRestantes; }
    public void setCartasRojasRestantes(int cartasRojasRestantes) { this.cartasRojasRestantes = cartasRojasRestantes; }

    public int getCartasAzulesRestantes() { return cartasAzulesRestantes; }
    public void setCartasAzulesRestantes(int cartasAzulesRestantes) { this.cartasAzulesRestantes = cartasAzulesRestantes; }

    public Boolean getRojoGana() { return rojoGana; } 
    public void setRojoGana(Boolean rojoGana) { this.rojoGana = rojoGana; } 

    public Integer getSegundosRestantes() { return segundosRestantes; }
    public void setSegundosRestantes(Integer segundosRestantes) { this.segundosRestantes = segundosRestantes; }

    public PistaDTO getPistaActual() { return pistaActual; }
    public void setPistaActual(PistaDTO pistaActual) { this.pistaActual = pistaActual; }

    public TableroDTO getTablero() { return tablero; }
    public void setTablero(TableroDTO tablero) { this.tablero = tablero; }

    public List<VotoDTO> getVotosTurnoActual() { return votosTurnoActual; }
    public void setVotosTurnoActual(List<VotoDTO> votosTurnoActual) { this.votosTurnoActual = votosTurnoActual; }
}