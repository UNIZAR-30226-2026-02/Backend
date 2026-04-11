package com.secretpanda.codenames.mapper.tienda;

import com.secretpanda.codenames.dto.tienda.PersonalizacionDTO;
import com.secretpanda.codenames.model.Personalizacion;

public class PersonalizacionMapper {

    private PersonalizacionMapper() {
    }

    /**
     * Convierte la entidad Personalización en un DTO inyectando el estado de posesión.
     */
    public static PersonalizacionDTO toDTO(Personalizacion personalizacion, boolean comprado) {
        if (personalizacion == null) {
            return null;
        }

        PersonalizacionDTO dto = new PersonalizacionDTO();
        dto.setIdPersonalizacion(personalizacion.getIdPersonalizacion());
        dto.setNombre(personalizacion.getNombre());
        dto.setDescripcion(personalizacion.getDescripcion());
        dto.setPrecioBala(personalizacion.getPrecioBala());
        dto.setTipo(personalizacion.getTipo().name());
        dto.setValorVisual(personalizacion.getValorVisual());
        dto.setComprado(comprado);

        return dto;
    }
}