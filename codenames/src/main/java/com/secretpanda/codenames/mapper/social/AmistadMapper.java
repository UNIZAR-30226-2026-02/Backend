package com.secretpanda.codenames.mapper.social;

import com.secretpanda.codenames.dto.social.AmistadDTO;
import com.secretpanda.codenames.model.Amistad;

import java.util.List;
import java.util.stream.Collectors;

// Mapper estático para la entidad Amistad (necesita info de ambos jugadores)
public class AmistadMapper {

    private AmistadMapper() {}

    // Convierte Amistad en un DTO 
    public static AmistadDTO toDTO(Amistad amistad) {
        if (amistad == null) return null;

        AmistadDTO dto = new AmistadDTO();

        // Datos del que envía la petición (formato snake_case)
        dto.setId_solicitante(amistad.getSolicitante().getIdGoogle());
        dto.setTag_solicitante(amistad.getSolicitante().getTag());
        dto.setFoto_solicitante(amistad.getSolicitante().getFotoPerfil());

        // Datos del que recibe la petición (formato snake_case)
        dto.setId_receptor(amistad.getReceptor().getIdGoogle());
        dto.setTag_receptor(amistad.getReceptor().getTag());
        dto.setFoto_receptor(amistad.getReceptor().getFotoPerfil());

        // Estado y fecha de la relación entre ambos
        dto.setEstado(amistad.getEstado().name());
        dto.setFecha_solicitud(amistad.getFechaSolicitud());

        return dto;
    }

    // Conversión de lista para todas las solicitudes pendientes o amigos de un jugador
    public static List<AmistadDTO> toDTOList(List<Amistad> amistades) {
        if (amistades == null) return null; // Seguro anti-nulos
        
        return amistades.stream()
                .map(AmistadMapper::toDTO)
                .collect(Collectors.toList());
    }
}