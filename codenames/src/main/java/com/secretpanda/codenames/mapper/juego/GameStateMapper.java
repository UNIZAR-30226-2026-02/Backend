package com.secretpanda.codenames.mapper.juego;

import com.secretpanda.codenames.dto.juego.GameStateDTO;
import com.secretpanda.codenames.model.Partida;
import com.secretpanda.codenames.model.TableroCarta;
import com.secretpanda.codenames.model.Turno;
import com.secretpanda.codenames.model.VotoCarta;

import java.util.List;

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
            String equipoLider = turnoActual.getJugadorPartida().getEquipo().name();
            dto.setEquipoTurnoActual(equipoLider);
            dto.setPistaActual(PistaMapper.toDTO(turnoActual));

            // Fase: si hay pista → "votando", si no → "esperando_pista"
            dto.setFaseTurno(turnoActual.getPalabraPista() != null
                    ? "votando" : "esperando_pista");
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
