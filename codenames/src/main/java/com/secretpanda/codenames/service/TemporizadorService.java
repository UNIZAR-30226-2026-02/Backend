package com.secretpanda.codenames.service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(TemporizadorService.class);

    private final SimpMessagingTemplate messagingTemplate;
    // Un scheduler dedicado con pool de 4 hilos (suficiente para 100 partidas concurrentes)
    private final ThreadPoolTaskScheduler scheduler;

    // Mapa: idPartida → ScheduledFuture del contador tick
    private final Map<Integer, ScheduledFuture<?>> timers = new ConcurrentHashMap<>();
    // Mapa: idPartida → segundos restantes (para el broadcast)
    private final Map<Integer, AtomicInteger>              segundos = new ConcurrentHashMap<>();

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
        // Cancelar si ya existía uno previo (ej: alguien votó antes de que acabe el tiempo)
        cancelarTemporizador(idPartida);

        AtomicInteger tiempoRestante = new AtomicInteger(duracionSegundos);
        segundos.put(idPartida, tiempoRestante);

        // Usamos la API de Instant y Duration para retrasar el primer tick 1 segundo
        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(() -> {
            
            // Decrementa de forma segura para hilos y obtiene el nuevo valor
            int actuales = tiempoRestante.decrementAndGet();

            TemporizadorDTO dto = new TemporizadorDTO(idPartida, actuales);
            messagingTemplate.convertAndSend(
                    "/topic/partidas/" + idPartida + "/temporizador", dto);

            if (actuales <= 0) {
                cancelarTemporizador(idPartida);
                
                // IMPORTANTE: Ejecutamos la lógica de expiración en otro hilo
                // para no bloquear los 4 hilos del scheduler si la BD va lenta.
                CompletableFuture.runAsync(() -> {
                    try {
                        alExpirar.run();
                    } catch (Exception e) {
                        log.error("Error al ejecutar el fin de turno automático para la partida {}", idPartida, e);
                    }
                });
            }
        }, Instant.now().plusSeconds(1), Duration.ofSeconds(1));

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
        AtomicInteger segs = segundos.get(idPartida);
        return segs != null ? segs.get() : 0;
    }

    @PreDestroy
    public void shutdown() {
        timers.values().forEach(f -> f.cancel(true));
        scheduler.shutdown();
    }
}
