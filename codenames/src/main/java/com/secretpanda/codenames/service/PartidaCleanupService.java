package com.secretpanda.codenames.service;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    public PartidaCleanupService(PartidaRepository partidaRepository) {
        this.partidaRepository = partidaRepository;
    }

    /**
     * Se ejecuta cada 30 minutos.
     * 1. Finaliza partidas en 'esperando' creadas hace más de 2 horas.
     * 2. Finaliza partidas 'en_curso' sin actividad (turno) en las últimas 3 horas.
     */
    @Scheduled(fixedRate = 1800000)
    @Transactional
    public void cleanupGhostGames() {
        logger.info("Iniciando recolector de partidas fantasma...");
        
        LocalDateTime dosHorasAtras = LocalDateTime.now().minusHours(2);
        LocalDateTime tresHorasAtras = LocalDateTime.now().minusHours(3);

        // 1. Partidas en 'esperando' creadas hace más de 2 horas
        List<Partida> esperandoAntiguas = partidaRepository
                .findByEstadoAndFechaCreacionBefore(EstadoPartida.esperando, dosHorasAtras);
        
        for (Partida p : esperandoAntiguas) {
            finalizarPartidaFantasma(p, "esperando (creada hace > 2h)");
        }

        // 2. Partidas 'en_curso' sin cambios de turno en las últimas 3 horas
        List<Partida> enCursoInactivas = partidaRepository
                .findByEstadoAndFechaInicioTurnoBefore(EstadoPartida.en_curso, tresHorasAtras);
        
        for (Partida p : enCursoInactivas) {
            finalizarPartidaFantasma(p, "en_curso (inactiva hace > 3h)");
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
