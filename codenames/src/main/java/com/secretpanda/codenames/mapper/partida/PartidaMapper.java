package com.secretpanda.codenames.mapper.partida;

import com.secretpanda.codenames.dto.partida.JugadorPartidaDTO;
import com.secretpanda.codenames.dto.partida.LobbyStatusDTO;
import com.secretpanda.codenames.dto.partida.PartidaResumenDTO;
import com.secretpanda.codenames.model.JugadorPartida;
import com.secretpanda.codenames.model.Partida;

import java.util.List;
import java.util.stream.Collectors;

// Mapper estático para la entidad Partida. Da DTOs para el lobby y para el resumen de la partida
public class PartidaMapper {

    private PartidaMapper() {}

    // Coge la partida y la lista de participantes y hace el DTO para el estado del lobby
    public static LobbyStatusDTO toLobbyStatusDTO(Partida partida, List<JugadorPartida> participantes) {
        if (partida == null) return null;

        LobbyStatusDTO dto = new LobbyStatusDTO();
        dto.setIdPartida(partida.getIdPartida());
        dto.setCodigoPartida(partida.getCodigoPartida());
        dto.setEstado(partida.getEstado().name());
        dto.setMaxJugadores(partida.getMaxJugadores());

        dto.setNombreTema(partida.getTema().getNombre());

        // Hacemos la conversión de cada jugador con su mapper
        List<JugadorPartidaDTO> jugadoresDTO = JugadorPartidaMapper.toDTOList(participantes);
        dto.setJugadores(jugadoresDTO);

        return dto;
    }

    // Convertimos la partida y el jugador en un DTO para mostrarlo en el historial de partidas del jugador
    public static PartidaResumenDTO toResumenDTO(Partida partida, JugadorPartida jpDelJugador) {
        if (partida == null || jpDelJugador == null) return null;

        PartidaResumenDTO dto = new PartidaResumenDTO();
        dto.setIdPartida(partida.getIdPartida());
        dto.setFechaFin(partida.getFechaFin());

        dto.setNombreTema(partida.getTema().getNombre());

        // Datos del jugador
        dto.setEquipoJugador(jpDelJugador.getEquipo().name());
        dto.setRolJugador(jpDelJugador.getRol().name());
        dto.setNumAciertos(jpDelJugador.getNumAciertos());
        dto.setNumFallos(jpDelJugador.getNumFallos());

        // Calculamos si el jugador ha ganado o perdido
        if (partida.getRojoGana() != null) {
            boolean jugadorEsRojo = JugadorPartida.Equipo.rojo.equals(jpDelJugador.getEquipo());
            boolean victoria = (partida.getRojoGana() && jugadorEsRojo)
                             || (!partida.getRojoGana() && !jugadorEsRojo);
            dto.setVictoria(victoria);
        } else {
            dto.setVictoria(false); // Si la partida aún no ha terminado
        }

        return dto;
    }

    // Conversión de lista de partidas a lista de DTOs de las partidas para el historial del jugador
    public static List<PartidaResumenDTO> toResumenDTOList(List<JugadorPartida> participaciones) {
        return participaciones.stream()
                .map(jp -> toResumenDTO(jp.getPartida(), jp))
                .collect(Collectors.toList());
    }
}