package com.secretpanda.codenames.mapper.tienda;

import com.secretpanda.codenames.dto.tienda.LogroDTO;
import com.secretpanda.codenames.model.Logro;
import com.secretpanda.codenames.model.Logro.TipoLogro;

import java.util.List;
import java.util.stream.Collectors;

// Mapper estático para la entidad Logro
public class LogroMapper {

    private LogroMapper() {}

    // De Logro al DTO con la info que hace falta para poder mostrarlo
    public static LogroDTO toDTO(Logro logro) {
        if (logro == null) return null;

        LogroDTO dto = new LogroDTO();
        
        // Atributos en formato snake_case
        dto.setId_logro(logro.getIdLogro());
        dto.setNombre(logro.getNombre());
        dto.setDescripcion(logro.getDescripcion());
        dto.setTipo(logro.getTipo().name());
        dto.setEstadistica_clave(logro.getEstadisticaClave());
        dto.setValor_objetivo(logro.getValorObjetivo());
        dto.setBalas_recompensa(logro.getBalasRecompensa());
        dto.setActivo(logro.isActivo());
        
        return dto;
    }

    // Pasamos de LogroDTO a una nueva entidad Logro (para crear un nuevo logro desde un panel admin, por ejemplo)
    public static Logro toEntity(LogroDTO dto) {
        if (dto == null) return null;

        Logro logro = new Logro();
        logro.setNombre(dto.getNombre());
        logro.setDescripcion(dto.getDescripcion());
        logro.setTipo(TipoLogro.valueOf(dto.getTipo()));
        
        // Leemos usando getters en snake_case y guardamos en la entidad (camelCase)
        logro.setEstadisticaClave(dto.getEstadistica_clave());
        logro.setValorObjetivo(dto.getValor_objetivo());
        logro.setBalasRecompensa(dto.getBalas_recompensa());
        logro.setActivo(dto.isActivo());
        
        return logro;
    }

    // Actualizamos el logro con los datos del DTO
    public static void applyUpdateDTO(LogroDTO dto, Logro logro) {
        if (dto == null || logro == null) return;

        logro.setNombre(dto.getNombre());
        logro.setDescripcion(dto.getDescripcion());
        logro.setTipo(TipoLogro.valueOf(dto.getTipo()));
        
        // Leemos usando getters en snake_case
        logro.setEstadisticaClave(dto.getEstadistica_clave());
        logro.setValorObjetivo(dto.getValor_objetivo());
        logro.setBalasRecompensa(dto.getBalas_recompensa());
        logro.setActivo(dto.isActivo());
    }

    // Conversión de listas de logros (para mostrar todos los logros)
    public static List<LogroDTO> toDTOList(List<Logro> logros) {
        if (logros == null) return null; // Seguro anti-nulos
        
        return logros.stream()
                .map(LogroMapper::toDTO)
                .collect(Collectors.toList());
    }
}