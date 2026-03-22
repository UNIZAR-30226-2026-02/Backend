package com.secretpanda.codenames.mapper.jugador;

import java.util.List;
import java.util.stream.Collectors;

import com.secretpanda.codenames.dto.jugador.PersonalizacionInventarioDTO;
import com.secretpanda.codenames.model.InventarioPersonalizacion;

// Mapper estático: transforma InventarioPersonalizacion → PersonalizacionInventarioDTO.
// Sin lógica de negocio, solo copia de datos.
public class PersonalizacionInventarioMapper {

    private PersonalizacionInventarioMapper() {}

    public static PersonalizacionInventarioDTO toDTO(InventarioPersonalizacion inv) {
        if (inv == null) return null;

        PersonalizacionInventarioDTO dto = new PersonalizacionInventarioDTO();
        dto.setIdPersonalizacion(inv.getPersonalizacion().getIdPersonalizacion());
        dto.setNombre(inv.getPersonalizacion().getNombre());
        dto.setTipo(inv.getPersonalizacion().getTipo().name());
        dto.setValorVisual(inv.getPersonalizacion().getValorVisual());
        dto.setEquipado(inv.isEquipado());

        return dto;
    }

    public static List<PersonalizacionInventarioDTO> toDTOList(List<InventarioPersonalizacion> inventario) {
        if (inventario == null) return null;
        return inventario.stream()
                .map(PersonalizacionInventarioMapper::toDTO)
                .collect(Collectors.toList());
    }
}