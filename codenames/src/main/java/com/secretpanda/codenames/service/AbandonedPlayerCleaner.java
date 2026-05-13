package com.secretpanda.codenames.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.secretpanda.codenames.model.JugadorPartida;
import com.secretpanda.codenames.repository.JugadorPartidaRepository;

@Service
public class AbandonedPlayerCleaner {

    private static final Logger logger = LoggerFactory.getLogger(AbandonedPlayerCleaner.class);
    private final Map<String, java.time.LocalDateTime> disconnectionTimes = new java.util.concurrent.ConcurrentHashMap<>();

    @Autowired
    private JugadorPartidaRepository jugadorPartidaRepository;

    @Autowired
    private PartidaService partidaService;

    public void registrarDesconexion(String idGoogle) {
        disconnectionTimes.put(idGoogle, java.time.LocalDateTime.now());
    }

    public void cancelarDesconexion(String idGoogle) {
        disconnectionTimes.remove(idGoogle);
    }

    @Scheduled(fixedRate = 5000)
    @Transactional
    public void cleanupAbandonedPlayers() {
        java.time.LocalDateTime limit = java.time.LocalDateTime.now().minusSeconds(60);
        
        disconnectionTimes.forEach((idGoogle, time) -> {
            if (time.isBefore(limit)) {
                logger.info("Detectado abandono definitivo tras timeout para jugador [{}]", idGoogle);
                
                // Obtenemos todos los registros activos del jugador para procesarlos uno a uno
                List<JugadorPartida> jpList = jugadorPartidaRepository.findAllByJugador_IdGoogleAndAbandonoFalse(idGoogle);
                
                for (JugadorPartida jp : jpList) {
                    try {
                        partidaService.abandonar(jp.getPartida().getIdPartida(), idGoogle, true);
                    } catch (Exception e) {
                        logger.error("Error al procesar abandono automático para jugador [{}] en partida [{}]: {}", 
                                      idGoogle, jp.getPartida().getIdPartida(), e.getMessage());
                    }
                }
                disconnectionTimes.remove(idGoogle);
            }
        });
    }
}