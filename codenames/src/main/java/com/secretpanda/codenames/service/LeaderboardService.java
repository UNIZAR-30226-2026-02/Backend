package com.secretpanda.codenames.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.secretpanda.codenames.dto.social.RankingDTO;
import com.secretpanda.codenames.mapper.jugador.JugadorMapper;
import com.secretpanda.codenames.model.Amistad;
import com.secretpanda.codenames.model.Jugador;
import com.secretpanda.codenames.repository.AmistadRepository;
import com.secretpanda.codenames.repository.JugadorRepository;

/**
 * Servicio encargado de gestionar las tablas de clasificación (Leaderboard).
 * Ordenación basada en: 1º Victorias, 2º Número de Aciertos.
 */
@Service
public class LeaderboardService {

    private final int NUMERO_LISTAR_RANKING_GLOBAL = 10;
    private final int NUMERO_LISTAR_RANKING_AMIGOS = 100;

    private final JugadorRepository jugadorRepository;
    private final AmistadRepository amistadRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public LeaderboardService(JugadorRepository jugadorRepository, 
                              AmistadRepository amistadRepository, 
                              SimpMessagingTemplate messagingTemplate) {
        this.jugadorRepository = jugadorRepository;
        this.amistadRepository = amistadRepository;
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Obtiene el Top 10 mundial de jugadores activos.
     * Criterio: Más victorias, y en caso de empate, más aciertos totales.
     */
    @Transactional(readOnly = true)
    public List<RankingDTO> getGlobalRanking() {
        // Solicitamos los 10 primeros registros con el ordenamiento compuesto
        List<Jugador> topJugadores = jugadorRepository
                .findByActivoTrueOrderByVictoriasDescNumAciertosDesc(PageRequest.of(0, NUMERO_LISTAR_RANKING_GLOBAL));
        
        return JugadorMapper.toRankingDTOList(topJugadores);
    }

    /**
     * Obtiene la clasificación entre el círculo de amigos del usuario.
     * Incluye al propio usuario en la lista.
     */
    @Transactional(readOnly = true)
    public List<RankingDTO> getFriendsRanking(String idGoogle) {
        // Buscamos todas las relaciones de amistad que ya han sido aceptadas
        List<Amistad> amistades = amistadRepository
                .findAmistadesPorJugadorYEstado(idGoogle, Amistad.EstadoAmistad.aceptada);

        // Extraer los IDs de los amigos (identificando quién es el 'otro' en la relación)
        List<String> idsAmigos = amistades.stream()
                .map(a -> a.getSolicitante().getIdGoogle().equals(idGoogle) 
                          ? a.getReceptor().getIdGoogle() 
                          : a.getSolicitante().getIdGoogle())
                .collect(Collectors.toList());

        // Añadimr al usuario actual a la lista para que pueda compararse con sus amigos
        idsAmigos.add(idGoogle);

        // Recuperamos los perfiles ordenados por el criterio de competición
        List<Jugador> amigos = jugadorRepository
                .findByIdGoogleInAndActivoTrueOrderByVictoriasDescNumAciertosDesc(idsAmigos, PageRequest.of(0, NUMERO_LISTAR_RANKING_AMIGOS));
        
        return JugadorMapper.toRankingDTOList(amigos);
    }

    /**
     * Emite la actualización del ranking global por el canal de WebSockets.
     * Se llama automáticamente al finalizar una partida.
     */
    public void broadcastGlobalRanking() {
        List<RankingDTO> ranking = getGlobalRanking();
        messagingTemplate.convertAndSend("/topic/leaderboard/global", ranking);
    }
}