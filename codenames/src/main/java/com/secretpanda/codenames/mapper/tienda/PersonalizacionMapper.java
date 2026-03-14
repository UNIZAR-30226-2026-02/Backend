package com.secretpanda.codenames.mapper.tienda;

import com.secretpanda.codenames.dto.tienda.PersonalizacionDTO;
import com.secretpanda.codenames.model.Personalizacion;
import com.secretpanda.codenames.model.Personalizacion.TipoPersonalizacion;

import java.util.List;
import java.util.stream.Collectors;

// Mapper estático para la entidad Personalización para las cartas y el tablero (tienda y mostrarlas en las partidas)
public class PersonalizacionMapper {

    private PersonalizacionMapper() {}

    // Convertimos la personalización en un DTO con la info para mostrarla en la tienda y en la partida
    public static PersonalizacionDTO toDTO(Personalizacion personalizacion) {
        if (personalizacion == null) return null;

        PersonalizacionDTO dto = new PersonalizacionDTO();
        
        // Atributos en formato snake_case para el DTO
        dto.setId_personalizacion(personalizacion.getIdPersonalizacion());
        dto.setNombre(personalizacion.getNombre());
        dto.setDescripcion(personalizacion.getDescripcion());
        dto.setPrecio_bala(personalizacion.getPrecioBala());
        dto.setTipo(personalizacion.getTipo().name());
        dto.setValor_visual(personalizacion.getValorVisual());
        dto.setActivo(personalizacion.isActivo());
        
        // Nota: Los campos 'comprado' y 'equipado' del DTO se rellenarán en la capa Service.
        
        return dto;
    }

    // Convertimos el DTO en una nueva entidad Personalizacion (para crear una nueva personalización)
    public static Personalizacion toEntity(PersonalizacionDTO dto) {
        if (dto == null) return null;

        Personalizacion personalizacion = new Personalizacion();
        personalizacion.setNombre(dto.getNombre());
        personalizacion.setDescripcion(dto.getDescripcion());
        
        // Leemos usando getters en snake_case y guardamos en la entidad (camelCase)
        personalizacion.setPrecioBala(dto.getPrecio_bala());
        personalizacion.setTipo(TipoPersonalizacion.valueOf(dto.getTipo())); // String → Enum
        personalizacion.setValorVisual(dto.getValor_visual());
        personalizacion.setActivo(dto.isActivo());
        
        return personalizacion;
    }

    // Actualiza los cambios del DTO sobre la entidad existente (para actualizar una personalización)
    public static void applyUpdateDTO(PersonalizacionDTO dto, Personalizacion personalizacion) {
        if (dto == null || personalizacion == null) return;

        personalizacion.setNombre(dto.getNombre());
        personalizacion.setDescripcion(dto.getDescripcion());
        
        // Leemos usando getters en snake_case
        personalizacion.setPrecioBala(dto.getPrecio_bala());
        personalizacion.setTipo(TipoPersonalizacion.valueOf(dto.getTipo()));
        personalizacion.setValorVisual(dto.getValor_visual());
        personalizacion.setActivo(dto.isActivo());
    }

    // Conversión de listas de personalizaciones (para la tienda)
    public static List<PersonalizacionDTO> toDTOList(List<Personalizacion> personalizaciones) {
        if (personalizaciones == null) return null; // Seguro anti-nulos
        
        return personalizaciones.stream()
                .map(PersonalizacionMapper::toDTO)
                .collect(Collectors.toList());
    }
}