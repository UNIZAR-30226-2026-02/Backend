package com.secretpanda.codenames.mapper.social;

import com.secretpanda.codenames.dto.social.AmistadDTO;
import com.secretpanda.codenames.model.Amistad;

import java.util.List;
import java.util.stream.Collectors;

// Mapper estático para la entidad Amistad (neceesita info de ambos jugadores)
public class AmistadMapper {

    private AmistadMapper() {}

    // Convierte Amistad en un DTO 
    public static AmistadDTO toDTO(Amistad amistad) {
        if (amistad == null) return null;

        AmistadDTO dto = new AmistadDTO();

        // Datos del que envía la petición
        dto.setIdSolicitante(amistad.getSolicitante().getIdGoogle());
        dto.setTagSolicitante(amistad.getSolicitante().getTag());
        dto.setFotoSolicitante(amistad.getSolicitante().getFotoPerfil());

        // Datos del que recibe la petición
        dto.setIdReceptor(amistad.getReceptor().getIdGoogle());
        dto.setTagReceptor(amistad.getReceptor().getTag());
        dto.setFotoReceptor(amistad.getReceptor().getFotoPerfil());

        // Estado y fecha de la relación entre ambos
        dto.setEstado(amistad.getEstado().name());
        dto.setFechaSolicitud(amistad.getFechaSolicitud());

        return dto;
    }

    // Conversión de lista para todas las solicitudes pendientes o amigos de un jugador
    public static List<AmistadDTO> toDTOList(List<Amistad> amistades) {
        return amistades.stream()
                .map(AmistadMapper::toDTO)
                .collect(Collectors.toList());
    }
}
