package com.secretpanda.codenames.mapper.jugador;

import java.util.List;
import java.util.stream.Collectors;

import com.secretpanda.codenames.dto.jugador.ActualizarPerfilDTO;
import com.secretpanda.codenames.dto.jugador.JugadorDTO;
import com.secretpanda.codenames.dto.social.RankingDTO;
import com.secretpanda.codenames.model.Jugador;
import com.secretpanda.codenames.model.Personalizacion;
import com.secretpanda.codenames.util.EstadisticasCalculator;

public class JugadorMapper {

    private JugadorMapper() {}

    public static JugadorDTO toDTO(Jugador jugador, EstadisticasCalculator calculator) {
        if (jugador == null) return null;

        JugadorDTO dto = new JugadorDTO();
        
        dto.setIdGoogle(jugador.getIdGoogle());
        dto.setTag(jugador.getTag());
        dto.setFotoPerfil(jugador.getFotoPerfil());
        dto.setBalas(jugador.getBalas());
        dto.setActivo(jugador.isActivo());

        dto.setPartidasJugadas(jugador.getPartidasJugadas());
        dto.setVictorias(jugador.getVictorias());
        dto.setNumAciertos(jugador.getNumAciertos());
        dto.setNumFallos(jugador.getNumFallos());

        if (calculator != null) {
            dto.setDerrotas(calculator.calcularDerrotas(jugador.getPartidasJugadas(), jugador.getVictorias()));
            dto.setPorcentajeVictorias(calculator.calcularWinrate(jugador.getVictorias(), jugador.getPartidasJugadas()));
        }

        // Lógica de Personalización: Extraer valores visuales de los items equipados
        
        // 1. Buscar Marco de Carta
        String marco = jugador.getInventario().stream()
                .filter(inv -> inv.isEquipado() && inv.getPersonalizacion().getTipo() == Personalizacion.TipoPersonalizacion.carta)
                .map(inv -> inv.getPersonalizacion().getValorVisual())
                .findFirst()
                .orElse(null); // O un valor por defecto si el frontend lo requiere
        dto.setMarcoCartaEquipado(marco);

        // 2. Buscar Fondo de Tablero
        String fondo = jugador.getInventario().stream()
                .filter(inv -> inv.isEquipado() && inv.getPersonalizacion().getTipo() == Personalizacion.TipoPersonalizacion.tablero)
                .map(inv -> inv.getPersonalizacion().getValorVisual())
                .findFirst()
                .orElse(null);
        dto.setFondoTableroEquipado(fondo);

        return dto;
    }

    public static RankingDTO toRankingDTO(Jugador jugador) {
        if (jugador == null) return null;

        RankingDTO dto = new RankingDTO();
        dto.setTag(jugador.getTag());
        dto.setFotoPerfil(jugador.getFotoPerfil());
        dto.setVictorias(jugador.getVictorias());
        
        return dto;
    }

    public static void applyUpdateDTO(ActualizarPerfilDTO dto, Jugador jugador) {
        if (dto == null || jugador == null) return;

        if (dto.getTag() != null) {
            jugador.setTag(dto.getTag());
        }
        if (dto.getFotoPerfil() != null) {
            jugador.setFotoPerfil(dto.getFotoPerfil());
        }
    }

    public static List<JugadorDTO> toDTOList(List<Jugador> jugadores, EstadisticasCalculator calculator) {
        if (jugadores == null) return null;
        
        return jugadores.stream()
                .map(j -> toDTO(j, calculator))
                .collect(Collectors.toList());
    }

    public static List<RankingDTO> toRankingDTOList(List<Jugador> jugadores) {
        if (jugadores == null) return null;
        
        return jugadores.stream()
                .map(JugadorMapper::toRankingDTO)
                .collect(Collectors.toList());
    }
}