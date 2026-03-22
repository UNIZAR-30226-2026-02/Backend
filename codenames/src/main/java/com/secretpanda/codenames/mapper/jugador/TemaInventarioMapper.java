package com.secretpanda.codenames.mapper.jugador;

import java.util.List;
import java.util.stream.Collectors;

import com.secretpanda.codenames.dto.jugador.TemaInventarioDTO;
import com.secretpanda.codenames.model.InventarioTema;

// Mapper estático: transforma InventarioTema → TemaInventarioDTO.
// Sin lógica de negocio, solo copia de datos.
public class TemaInventarioMapper {

    private TemaInventarioMapper() {}

    public static TemaInventarioDTO toDTO(InventarioTema inv) {
        if (inv == null) return null;

        TemaInventarioDTO dto = new TemaInventarioDTO();
        dto.setIdTema(inv.getTema().getIdTema());
        dto.setNombre(inv.getTema().getNombre());
        dto.setDescripcion(inv.getTema().getDescripcion());

        return dto;
    }

    public static List<TemaInventarioDTO> toDTOList(List<InventarioTema> inventario) {
        if (inventario == null) return null;
        return inventario.stream()
                .map(TemaInventarioMapper::toDTO)
                .collect(Collectors.toList());
    }
}