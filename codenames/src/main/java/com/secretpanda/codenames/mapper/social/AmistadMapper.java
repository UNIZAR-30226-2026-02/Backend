package com.secretpanda.codenames.mapper.social;

import java.util.List;
import java.util.stream.Collectors;

import com.secretpanda.codenames.dto.social.AmistadDTO;
import com.secretpanda.codenames.model.Amistad;

// Mapper estático para la entidad Amistad (necesita info de ambos jugadores)
public class AmistadMapper {

    private AmistadMapper() {}

    // Convierte Amistad en un DTO 
    public static AmistadDTO toDTO(Amistad amistad) {
        if (amistad == null) return null;

        AmistadDTO dto = new AmistadDTO();
        dto.setIdSolicitante(amistad.getSolicitante().getIdGoogle());
        dto.setTagSolicitante(amistad.getSolicitante().getTag());
        dto.setFotoPerfilSolicitante(amistad.getSolicitante().getFotoPerfil());
        dto.setFechaSolicitud(amistad.getFechaSolicitud());
        dto.setEstado(amistad.getEstado().name());

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