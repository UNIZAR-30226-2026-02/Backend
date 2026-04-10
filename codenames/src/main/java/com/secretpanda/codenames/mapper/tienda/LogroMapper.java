package com.secretpanda.codenames.mapper.tienda;

import com.secretpanda.codenames.dto.tienda.LogroDTO;
import com.secretpanda.codenames.model.Logro;

public class LogroMapper {

    private LogroMapper() {}

    /**
     * Convierte la entidad Logro en DTO con datos básicos.
     * Mapea 'tipo' a 'esLogro' y 'valorObjetivo' a 'progresoMax'.
     */
    public static LogroDTO toDTO(Logro logro) {
        if (logro == null) return null;

        LogroDTO dto = new LogroDTO();
        dto.setIdLogro(logro.getIdLogro());
        dto.setNombre(logro.getNombre());
        dto.setDescripcion(logro.getDescripcion());
        dto.setBalasRecompensa(logro.getBalasRecompensa());
        
        // Criterios de la API
        dto.setProgresoMax(logro.getValorObjetivo());
        dto.setEsLogro(Logro.TipoLogro.logro.equals(logro.getTipo()));

        // Los campos de progreso (actual y completado) se dejan por defecto (0/false)
        // ya que este mapper no conoce al jugador.
        return dto;
    }
}