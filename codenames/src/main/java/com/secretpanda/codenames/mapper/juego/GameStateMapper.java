package com.secretpanda.codenames.mapper.juego;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import com.secretpanda.codenames.dto.juego.GameStateDTO;
import com.secretpanda.codenames.model.JugadorPartida;
import com.secretpanda.codenames.model.JugadorPartida.Equipo;
import com.secretpanda.codenames.model.JugadorPartida.Rol;
import com.secretpanda.codenames.model.Partida;
import com.secretpanda.codenames.model.TableroCarta;
import com.secretpanda.codenames.model.Turno;
import com.secretpanda.codenames.model.VotoCarta;

public class GameStateMapper {

    private GameStateMapper() {}

    public static GameStateDTO toDTO(Partida partida, List<TableroCarta> cartas,
                                     Turno turnoActual, List<VotoCarta> votosTurno,
                                     boolean esLider) {
        if (partida == null) return null;

        GameStateDTO dto = new GameStateDTO();
        dto.setIdPartida(partida.getIdPartida());
        dto.setEstado(partida.getEstado().name());
        dto.setRojoGana(partida.getRojoGana());

        // Cálculo de agentes y abandono
        List<JugadorPartida> jps = partida.getJugadores();
        int agentesRojosActivos = (int) jps.stream().filter(jp -> jp.getEquipo() == Equipo.rojo && jp.getRol() == Rol.agente && !jp.isAbandono()).count();
        int agentesAzulesActivos = (int) jps.stream().filter(jp -> jp.getEquipo() == Equipo.azul && jp.getRol() == Rol.agente && !jp.isAbandono()).count();
        
        dto.setTotalAgentesRojos(agentesRojosActivos);
        dto.setTotalAgentesAzules(agentesAzulesActivos);
        dto.setAbandonoRojo(jps.stream().anyMatch(jp -> jp.getEquipo() == Equipo.rojo && jp.isAbandono()));
        dto.setAbandonoAzul(jps.stream().anyMatch(jp -> jp.getEquipo() == Equipo.azul && jp.isAbandono()));

        // Cálculo de segundos restantes
        if (partida.getFechaInicioTurno() != null && partida.getEstado() == Partida.EstadoPartida.en_curso) {
            long transcurridos = ChronoUnit.SECONDS.between(partida.getFechaInicioTurno(), LocalDateTime.now());
            int restantes = (int) (partida.getTiempoEspera() - transcurridos);
            dto.setSegundosRestantes(Math.max(0, restantes));
        } else {
            dto.setSegundosRestantes(0);
        }

        // Tablero con niebla de guerra según rol
        dto.setTablero(TableroMapper.toDTO(cartas, esLider));

        // Cartas restantes (siempre visibles para todos)
        long rojas = cartas.stream()
                .filter(c -> TableroCarta.TipoCarta.rojo.equals(c.getTipo())
                          && TableroCarta.EstadoCarta.oculta.equals(c.getEstado()))
                .count();
        long azules = cartas.stream()
                .filter(c -> TableroCarta.TipoCarta.azul.equals(c.getTipo())
                          && TableroCarta.EstadoCarta.oculta.equals(c.getEstado()))
                .count();
        dto.setCartasRojasRestantes((int) rojas);
        dto.setCartasAzulesRestantes((int) azules);

        // Turno actual
        if (turnoActual != null) {
            dto.setEquipoTurnoActual(turnoActual.getJugadorPartida().getEquipo().name());

            if (turnoActual.getPalabraPista() == null) {
                dto.setFaseTurno("esperando_pista");
                dto.setPistaActual(null); // Forzamos pista nula en el DTO
            } else {
                dto.setFaseTurno("votando");
                dto.setPistaActual(PistaMapper.toDTO(turnoActual));
            }
        } else {
            // Sin ningún turno aún: el equipo que empieza es el que tiene más cartas
            dto.setEquipoTurnoActual(rojas >= azules ? "rojo" : "azul");
            dto.setFaseTurno("esperando_pista");
        }

        // Votos del turno actual
        dto.setVotosTurnoActual(VotoMapper.toDTOList(votosTurno));

        return dto;
    }
}
