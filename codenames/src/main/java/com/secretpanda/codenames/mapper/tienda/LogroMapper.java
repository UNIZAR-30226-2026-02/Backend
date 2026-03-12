package com.secretpanda.codenames.mapper.tienda;

import com.secretpanda.codenames.dto.tienda.LogroDTO;
import com.secretpanda.codenames.model.Logro;
import com.secretpanda.codenames.model.Logro.TipoLogro;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 
 *
 * Gestiona la conversión bidireccional entre la entidad y el DTO
 * usado en el catálogo de logros (pantalla de logros del jugador)
 * y en los endpoints de administración (crear/actualizar logros).
 *
 * El enum TipoLogro se serializa como String ("medalla" o "logro")
 * y se deserializa con valueOf() para mantener el contrato de la API
 * independiente de la implementación interna del enum.
 */
// Mapper estático para la entidad Logro
public class LogroMapper {

    private LogroMapper() {}

    // De Logro al DTO con la info que hace falta para poder mostrarlo
    public static LogroDTO toDTO(Logro logro) {
        if (logro == null) return null;

        LogroDTO dto = new LogroDTO();
        dto.setIdLogro(logro.getIdLogro());
        dto.setNombre(logro.getNombre());
        dto.setDescripcion(logro.getDescripcion());
        dto.setTipo(logro.getTipo().name());
        dto.setEstadisticaClave(logro.getEstadisticaClave());
        dto.setValorObjetivo(logro.getValorObjetivo());
        dto.setBalasRecompensa(logro.getBalasRecompensa());
        dto.setActivo(logro.isActivo());
        return dto;
    }

    // Pasamos de LogroDTO a una nueva entidad Logro (para crear un nuevo logro)
    public static Logro toEntity(LogroDTO dto) {
        if (dto == null) return null;

        Logro logro = new Logro();
        logro.setNombre(dto.getNombre());
        logro.setDescripcion(dto.getDescripcion());
        logro.setTipo(TipoLogro.valueOf(dto.getTipo()));
        logro.setEstadisticaClave(dto.getEstadisticaClave());
        logro.setValorObjetivo(dto.getValorObjetivo());
        logro.setBalasRecompensa(dto.getBalasRecompensa());
        logro.setActivo(dto.isActivo());
        return logro;
    }

    // Cambiamos el logro con los datos del 
    public static void applyUpdateDTO(LogroDTO dto, Logro logro) {
        if (dto == null || logro == null) return;

        logro.setNombre(dto.getNombre());
        logro.setDescripcion(dto.getDescripcion());
        logro.setTipo(TipoLogro.valueOf(dto.getTipo()));
        logro.setEstadisticaClave(dto.getEstadisticaClave());
        logro.setValorObjetivo(dto.getValorObjetivo());
        logro.setBalasRecompensa(dto.getBalasRecompensa());
        logro.setActivo(dto.isActivo());
    }

    // Conversión de listas de logros (para mostrar todos los logros)
    public static List<LogroDTO> toDTOList(List<Logro> logros) {
        return logros.stream()
                .map(LogroMapper::toDTO)
                .collect(Collectors.toList());
    }
}