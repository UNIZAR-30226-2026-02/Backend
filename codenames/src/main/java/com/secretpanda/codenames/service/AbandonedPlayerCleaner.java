package com.secretpanda.codenames.service;

import java.time.LocalDateTime;
import java.util.List;

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

    @Autowired
    private JugadorPartidaRepository jugadorPartidaRepository;

    @Autowired
    private PartidaService partidaService;

    // Se ejecuta cada 5 segundos para asegurar baja latencia en la detección
    @Scheduled(fixedRate = 5000)
    @Transactional
    public void cleanupAbandonedPlayers() {
        LocalDateTime limit = LocalDateTime.now().minusSeconds(60);
        
        List<JugadorPartida> abandonos = jugadorPartidaRepository.findByAbandonoFalseAndUltimaDesconexionBefore(limit);
        
        for (JugadorPartida jp : abandonos) {
            logger.info("Detectado abandono definitivo tras timeout para jugador [{}], partida [{}]", 
                         jp.getJugador().getIdGoogle(), jp.getPartida().getIdPartida());
            
            try {
                partidaService.abandonar(jp.getPartida().getIdPartida(), jp.getJugador().getIdGoogle(), true);
            } catch (Exception e) {
                logger.error("Error al procesar abandono automático para jugador [{}]: {}", 
                              jp.getJugador().getIdGoogle(), e.getMessage());
            }
        }
    }
}