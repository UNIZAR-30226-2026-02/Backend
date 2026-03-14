package com.secretpanda.codenames.mapper.juego;

import com.secretpanda.codenames.dto.juego.VotoDTO;
import com.secretpanda.codenames.model.VotoCarta;
import java.util.List;
import java.util.stream.Collectors;

public class VotoMapper {

    private VotoMapper() {}

    public static VotoDTO toDTO(VotoCarta voto) {
        if (voto == null) return null;

        VotoDTO dto = new VotoDTO();
        dto.setId_carta(voto.getCartaTablero().getIdCartaTablero());
        dto.setTag_jugador(voto.getJugadorPartida().getJugador().getTag());
        dto.setEquipo(voto.getJugadorPartida().getEquipo().name());

        return dto;
    }

    public static List<VotoDTO> toDTOList(List<VotoCarta> votos) {
        if (votos == null) return null;
        return votos.stream().map(VotoMapper::toDTO).collect(Collectors.toList());
    }
}