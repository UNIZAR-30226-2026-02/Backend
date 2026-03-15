package com.secretpanda.codenames.mapper.juego;

import com.secretpanda.codenames.dto.juego.PistaDTO;
import com.secretpanda.codenames.model.Turno;

public class PistaMapper {

    private PistaMapper() {}

    public static PistaDTO toDTO(Turno turno) {
        if (turno == null || turno.getPalabraPista() == null) return null;

        PistaDTO dto = new PistaDTO();
        dto.setPalabraPista(turno.getPalabraPista());
        dto.setPistaNumero(turno.getPistaNumero());
        // Extraemos el equipo del jugador que creó el turno
        dto.setEquipoLider(turno.getJugadorPartida().getEquipo().name());

        return dto;
    }
}