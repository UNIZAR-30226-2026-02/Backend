package com.secretpanda.codenames.Unitarios.service;

import com.secretpanda.codenames.service.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.secretpanda.codenames.model.Jugador;
import com.secretpanda.codenames.model.Tema;
import com.secretpanda.codenames.repository.InventarioTemaRepository;
import com.secretpanda.codenames.repository.JugadorRepository;
import com.secretpanda.codenames.repository.TemaRepository;

import com.secretpanda.codenames.Integracion.config.IntegrationTestBase;

/**
 * Prueba de integración para validar la concurrencia y los bloqueos pesimistas
 * en las transacciones de la tienda.
 */
public class TiendaServiceConcurrencyIT extends IntegrationTestBase {

    @Autowired
    private TiendaService tiendaService;

    @Autowired
    private JugadorRepository jugadorRepository;

    @Autowired
    private TemaRepository temaRepository;

    @Autowired
    private InventarioTemaRepository inventarioTemaRepository;

    /**
     * Prueba: shouldPreventDoubleSpendingOnConcurrentPurchases
     * Simula múltiples hilos intentando comprar el mismo artículo simultáneamente.
     * El bloqueo pesimista debe asegurar que solo una compra pase si el saldo no alcanza para ambas,
     * o que se lance la excepción de BadRequest (ya lo posees) en las ejecuciones concurrentes.
     */
    @Test
    public void shouldPreventDoubleSpendingOnConcurrentPurchases() throws InterruptedException {
        // 1. Arrange: Preparamos la DB
        String idGoogle = "concurrency_user";
        Jugador jugador = new Jugador();
        jugador.setIdGoogle(idGoogle);
        jugador.setTag("RichPanda");
        jugador.setBalas(50); // Exactamente lo que cuesta un tema
        jugadorRepository.saveAndFlush(jugador);

        Tema tema = new Tema();
        tema.setNombre("Tema Caro");
        tema.setPrecioBalas(50);
        tema = temaRepository.saveAndFlush(tema);
        final Integer temaId = tema.getIdTema();

        int numberOfThreads = 5;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger exceptionCount = new AtomicInteger(0);

        // 2. Act: Lanzamos las peticiones al mismo tiempo
        for (int i = 0; i < numberOfThreads; i++) {
            executor.submit(() -> {
                try {
                    tiendaService.comprarTema(idGoogle, temaId);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    exceptionCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }
        
        latch.await();
        executor.shutdown();

        // 3. Assert: Verificamos resultados
        // Solo 1 transacción debería haber tenido éxito debido al bloqueo de DB y reglas de negocio
        assertEquals(1, successCount.get(), "Solo una compra debe tener éxito.");
        // Los otros 4 hilos deben haber fallado (saldo insuficiente o artículo ya adquirido)
        assertEquals(4, exceptionCount.get(), "Las compras concurrentes deben fallar.");

        // Verificamos el saldo final en la base de datos
        Jugador jFinal = jugadorRepository.findById(idGoogle).orElseThrow();
        assertEquals(0, jFinal.getBalas(), "El saldo debe ser 0.");

        // Limpieza (Opcional si se usa @Transactional en el test general, pero en hilos múltiples es manual o se reinicia el contexto)
        inventarioTemaRepository.deleteAll();
        temaRepository.delete(tema);
        jugadorRepository.delete(jugador);
    }
}