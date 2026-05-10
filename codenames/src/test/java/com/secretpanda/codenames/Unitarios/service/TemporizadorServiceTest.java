package com.secretpanda.codenames.Unitarios.service;

import com.secretpanda.codenames.service.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import static org.mockito.Mockito.mock;

/**
 * Suite de pruebas unitarias para TemporizadorService.
 * Valida la precisión de los hilos, su correcta ejecución y su cancelación segura.
 */
public class TemporizadorServiceTest {

    private TemporizadorService temporizadorService;
    private SimpMessagingTemplate messagingTemplate;

    @BeforeEach
    public void setUp() {
        messagingTemplate = mock(SimpMessagingTemplate.class);
        temporizadorService = new TemporizadorService(messagingTemplate, 4);
    }

    /**
     * Prueba: shouldExecuteCallbackAfterDelay
     * Verifica que el callback se ejecuta tras el tiempo estipulado si no se cancela.
     */
    @Test
    public void shouldExecuteCallbackAfterDelay() throws InterruptedException {
        Integer idPartida = 1;
        AtomicBoolean executed = new AtomicBoolean(false);
        CountDownLatch latch = new CountDownLatch(1);

        // Simulamos un temporizador muy corto (1 segundo)
        temporizadorService.iniciarTemporizador(idPartida, 1, () -> {
            executed.set(true);
            latch.countDown();
        });

        // Esperamos máximo 2 segundos a que el hilo termine
        boolean awaitResult = latch.await(2, TimeUnit.SECONDS);

        assertTrue(awaitResult, "El latch debería haber llegado a 0");
        assertTrue(executed.get(), "El callback debería haberse ejecutado");
    }

    /**
     * Prueba: shouldNotExecuteCallbackIfCanceled
     * Verifica que si se llama a cancelarTemporizador antes de que expire el tiempo,
     * el hilo se interrumpe y el callback NUNCA se ejecuta.
     */
    @Test
    public void shouldNotExecuteCallbackIfCanceled() throws InterruptedException {
        Integer idPartida = 2;
        AtomicBoolean executed = new AtomicBoolean(false);

        // Iniciamos un temporizador de 2 segundos
        temporizadorService.iniciarTemporizador(idPartida, 2, () -> {
            executed.set(true);
        });

        // Lo cancelamos inmediatamente (a los 100ms)
        Thread.sleep(100);
        temporizadorService.cancelarTemporizador(idPartida);

        // Esperamos a que pasen los 2 segundos originales
        Thread.sleep(2100);

        assertFalse(executed.get(), "El callback NO debería haberse ejecutado tras la cancelación");
    }
}