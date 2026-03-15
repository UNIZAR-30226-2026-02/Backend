package com.secretpanda.codenames.mapper.tienda;

import com.secretpanda.codenames.dto.tienda.TemaDTO;
import com.secretpanda.codenames.model.Tema;
import java.util.List;
import java.util.stream.Collectors;

public class TemaMapper {

    private TemaMapper() {}

    public static TemaDTO toDTO(Tema tema) {
        if (tema == null) return null;

        TemaDTO dto = new TemaDTO();
        dto.setIdTema(tema.getIdTema());
        dto.setNombre(tema.getNombre());
        dto.setDescripcion(tema.getDescripcion());
        dto.setPrecioBalas(tema.getPrecioBalas());
        dto.setActivo(tema.isActivo());
        
        // El campo 'comprado' se gestionará en el Service cruzando con el Inventario
        return dto;
    }

    public static List<TemaDTO> toDTOList(List<Tema> temas) {
        if (temas == null) return null;
        return temas.stream().map(TemaMapper::toDTO).collect(Collectors.toList());
    }
}