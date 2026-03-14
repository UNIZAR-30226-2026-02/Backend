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
                                    Turno turnoActual, List<VotoCarta> votosTurno, boolean esLider) {
        if (partida == null) return null;

        GameStateDTO dto = new GameStateDTO();
        dto.setId_partida(partida.getIdPartida());
        dto.setEstado_partida(partida.getEstado().name());
        dto.setVictoria_roja(partida.getRojoGana());

        // Mapeamos el tablero usando su propio mapper (con niebla de guerra)
        dto.setTablero(TableroMapper.toDTO(cartas, esLider));

        // Mapeamos la pista si existe un turno activo
        if (turnoActual != null) {
            dto.setEquipo_turno_actual(turnoActual.getJugadorPartida().getEquipo().name());
            dto.setPista_actual(PistaMapper.toDTO(turnoActual));
            // La fase depende de si hay pista: si hay pista, los agentes están "votando"
            dto.setFase_turno(turnoActual.getPalabraPista() != null ? "votando" : "esperando_pista");
        }

        // Mapeamos los votos
        dto.setVotos_turno_actual(VotoMapper.toDTOList(votosTurno));

        // Cálculos de cartas restantes (esto lo sacaremos de la lista de cartas)
        long rojas = cartas.stream().filter(c -> "rojo".equals(c.getTipo().name()) && "oculta".equals(c.getEstado().name())).count();
        long azules = cartas.stream().filter(c -> "azul".equals(c.getTipo().name()) && "oculta".equals(c.getEstado().name())).count();
        
        dto.setCartas_rojas_restantes((int) rojas);
        dto.setCartas_azules_restantes((int) azules);

        return dto;
    }
}