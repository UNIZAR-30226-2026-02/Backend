package com.secretpanda.codenames.mapper.partida;

import java.util.List;
import java.util.stream.Collectors;

import com.secretpanda.codenames.dto.partida.JugadorLobbyDTO;
import com.secretpanda.codenames.dto.partida.LobbyStatusDTO;
import com.secretpanda.codenames.dto.partida.PartidaResumenDTO;
import com.secretpanda.codenames.model.JugadorPartida;
import com.secretpanda.codenames.model.Partida;

// Mapper estático para la entidad Partida. Da DTOs para el lobby y para el resumen de la partida
public class PartidaMapper {

    private PartidaMapper() {}

    // Coge la partida y la lista de participantes y hace el DTO para el estado del lobby
    public static LobbyStatusDTO toLobbyStatusDTO(Partida partida, List<JugadorPartida> participantes) {
        if (partida == null) return null;

        LobbyStatusDTO dto = new LobbyStatusDTO();
        
        // Adaptado a snake_case
        dto.setIdPartida(partida.getIdPartida());
        dto.setCodigoPartida(partida.getCodigoPartida());
        dto.setEstado(partida.getEstado().name());
        dto.setMaxJugadores(partida.getMaxJugadores());
        dto.setEsPublica(partida.isEsPublica()); 
        dto.setIdTema(partida.getTema().getIdTema()); 
        
        dto.setNombreTema(partida.getTema().getNombre());

        // Hacemos la conversión de cada jugador con su mapper
        List<JugadorLobbyDTO> jugadoresLobby = JugadorPartidaMapper.toLobbyDTOList(participantes);
        dto.setJugadores(jugadoresLobby);

        return dto;
    }

    // Convertimos la partida y el jugador en un DTO para mostrarlo en el historial de partidas del jugador
    public static PartidaResumenDTO toResumenDTO(Partida partida, JugadorPartida jpDelJugador) {
        if (partida == null || jpDelJugador == null) return null;

        PartidaResumenDTO dto = new PartidaResumenDTO();
        
        // Datos de la Partida
        dto.setIdPartida(partida.getIdPartida());
        dto.setCodigoPartida(partida.getCodigoPartida());
        dto.setFechaFin(partida.getFechaFin());
        dto.setEstado(partida.getEstado().name());
        dto.setRojoGana(partida.getRojoGana());

        // Datos del Jugador en esa partida
        dto.setEquipo(jpDelJugador.getEquipo().name());
        dto.setRol(jpDelJugador.getRol().name());
        dto.setAbandono(jpDelJugador.isAbandono());
        dto.setNumAciertos(jpDelJugador.getNumAciertos());
        dto.setNumFallos(jpDelJugador.getNumFallos());
        dto.setTagCreador(partida.getCreador().getTag());

        return dto;
    }

    // Conversión de lista de partidas a lista de DTOs de las partidas para el historial del jugador
    public static List<PartidaResumenDTO> toResumenDTOList(List<JugadorPartida> participaciones) {
        if (participaciones == null) return null;
        return participaciones.stream()
                .map(jp -> toResumenDTO(jp.getPartida(), jp))
                .collect(Collectors.toList());
    }
}