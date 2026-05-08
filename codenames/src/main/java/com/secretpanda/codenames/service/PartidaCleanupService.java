package com.secretpanda.codenames.service;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.secretpanda.codenames.model.Partida;
import com.secretpanda.codenames.model.Partida.EstadoPartida;
import com.secretpanda.codenames.repository.PartidaRepository;

/**
 * Servicio recolector de partidas fantasma (Ghost Games Collector).
 * Libera recursos finalizando partidas obsoletas o inactivas.
 */
@Service
public class PartidaCleanupService {

    private static final Logger logger = LoggerFactory.getLogger(PartidaCleanupService.class);

    private final PartidaRepository partidaRepository;

    @Value("${game.cleanup.horas-esperando:2}")
    private int horasEsperando;

    @Value("${game.cleanup.horas-en-curso:3}")
    private int horasEnCurso;

    public PartidaCleanupService(PartidaRepository partidaRepository) {
        this.partidaRepository = partidaRepository;
    }

    /**
     * Se ejecuta cada 30 minutos.
     * 1. Finaliza partidas en 'esperando' creadas hace más de N horas.
     * 2. Finaliza partidas 'en_curso' sin actividad (turno) en las últimas M horas.
     */
    @Scheduled(fixedRate = 1800000)
    @Transactional
    public void cleanupGhostGames() {
        logger.info("Iniciando recolector de partidas fantasma...");
        
        LocalDateTime tiempoEsperando = LocalDateTime.now().minusHours(horasEsperando);
        LocalDateTime tiempoEnCurso = LocalDateTime.now().minusHours(horasEnCurso);

        // 1. Partidas en 'esperando' antiguas
        List<Partida> esperandoAntiguas = partidaRepository
                .findByEstadoAndFechaCreacionBefore(EstadoPartida.esperando, tiempoEsperando);
        
        for (Partida p : esperandoAntiguas) {
            finalizarPartidaFantasma(p, "esperando (creada hace > " + horasEsperando + "h)");
        }

        // 2. Partidas 'en_curso' inactivas
        List<Partida> enCursoInactivas = partidaRepository
                .findByEstadoAndFechaInicioTurnoBefore(EstadoPartida.en_curso, tiempoEnCurso);
        
        for (Partida p : enCursoInactivas) {
            finalizarPartidaFantasma(p, "en_curso (inactiva hace > " + horasEnCurso + "h)");
        }

        partidaRepository.saveAll(esperandoAntiguas);
        partidaRepository.saveAll(enCursoInactivas);
        
        logger.info("Limpieza finalizada. Total: {} esperando, {} inactivas.", 
                esperandoAntiguas.size(), enCursoInactivas.size());
    }

    private void finalizarPartidaFantasma(Partida p, String motivo) {
        p.setEstado(EstadoPartida.finalizada);
        p.setFechaFin(LocalDateTime.now());
        logger.info("Partida fantasma finalizada: {} - Motivo: {}", p.getIdPartida(), motivo);
    }
}
