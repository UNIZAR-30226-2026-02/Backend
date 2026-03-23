package com.secretpanda.codenames.service;

import java.util.Map;
import java.util.concurrent.*;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

import com.secretpanda.codenames.dto.juego.TemporizadorDTO;
import jakarta.annotation.PreDestroy;

/**
 * Gestiona un temporizador por partida.
 * Cada segundo emite por WebSocket los segundos restantes.
 * Cuando llega a 0, invoca el callback de expiración (fin de turno).
 */
@Service
public class TemporizadorService {

    private final SimpMessagingTemplate messagingTemplate;
    // Un scheduler dedicado con pool de 4 hilos (suficiente para 100 partidas concurrentes)
    private final ThreadPoolTaskScheduler scheduler;

    // Mapa: idPartida → ScheduledFuture del contador tick
    private final Map<Integer, ScheduledFuture<?>> timers = new ConcurrentHashMap<>();
    // Mapa: idPartida → segundos restantes (para el broadcast)
    private final Map<Integer, int[]>              segundos = new ConcurrentHashMap<>();

    public TemporizadorService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
        this.scheduler = new ThreadPoolTaskScheduler();
        this.scheduler.setPoolSize(4);
        this.scheduler.setThreadNamePrefix("timer-");
        this.scheduler.initialize();
    }

    /**
     * Inicia (o reinicia) el temporizador de una partida.
     *
     * @param idPartida      id de la partida
     * @param duracionSegundos duración del turno en segundos (30, 60, 90, 120)
     * @param alExpirar      callback que se ejecuta al llegar a 0
     */
    public void iniciarTemporizador(Integer idPartida, int duracionSegundos, Runnable alExpirar) {
        cancelarTemporizador(idPartida);

        int[] segs = { duracionSegundos };
        segundos.put(idPartida, segs);

        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(() -> {
            segs[0]--;

            TemporizadorDTO dto = new TemporizadorDTO(idPartida, segs[0]);
            messagingTemplate.convertAndSend(
                    "/topic/partidas/" + idPartida + "/temporizador", dto);

            if (segs[0] <= 0) {
                cancelarTemporizador(idPartida);
                try {
                    alExpirar.run();
                } catch (Exception e) {
                    // Log y continuar — nunca dejar morir el scheduler
                }
            }
        }, 1000); // cada 1 segundo

        timers.put(idPartida, future);
    }

    /** Detiene el temporizador de una partida sin disparar el callback. */
    public void cancelarTemporizador(Integer idPartida) {
        ScheduledFuture<?> existing = timers.remove(idPartida);
        if (existing != null) {
            existing.cancel(false);
        }
        segundos.remove(idPartida);
    }

    /** Devuelve los segundos restantes de una partida (0 si no hay temporizador activo). */
    public int getSegundosRestantes(Integer idPartida) {
        int[] segs = segundos.get(idPartida);
        return segs != null ? segs[0] : 0;
    }

    @PreDestroy
    public void shutdown() {
        timers.values().forEach(f -> f.cancel(true));
        scheduler.shutdown();
    }
}
