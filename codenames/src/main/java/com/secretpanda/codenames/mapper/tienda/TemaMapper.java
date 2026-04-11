package com.secretpanda.codenames.mapper.tienda;

import com.secretpanda.codenames.dto.tienda.TemaDTO;
import com.secretpanda.codenames.model.Tema;

public class TemaMapper {

    private TemaMapper() {
    }

    /**
     * Convierte la entidad Tema en un DTO inyectando el estado de posesión.
     */
    public static TemaDTO toDTO(Tema tema, boolean comprado) {
        if (tema == null) {
            return null;
        }

        TemaDTO dto = new TemaDTO();
        dto.setIdTema(tema.getIdTema());
        dto.setNombre(tema.getNombre());
        dto.setDescripcion(tema.getDescripcion());
        dto.setPrecioBalas(tema.getPrecioBalas());
        dto.setComprado(comprado);

        return dto;
    }
}