package com.secretpanda.codenames.Integracion;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.secretpanda.codenames.Integracion.config.IntegrationTestBase;
import com.secretpanda.codenames.dto.tienda.ComprarItemRequest;
import com.secretpanda.codenames.model.Jugador;
import com.secretpanda.codenames.model.Tema;
import com.secretpanda.codenames.repository.JugadorRepository;
import com.secretpanda.codenames.repository.TemaRepository;

public class TiendaIntegrationIT extends IntegrationTestBase {

    @Autowired private JugadorRepository jugadorRepository;
    @Autowired private TemaRepository temaRepository;

    @Test
    void testComprarTema_ConSaldoInsuficiente_DebeRechazar() {
        // GIVEN: Jugador con 49 balas y tema de 50
        Jugador j = new Jugador(); j.setIdGoogle("j1"); j.setTag("J1"); j.setBalas(49); j.setActivo(true);
        jugadorRepository.save(j);

        Tema t = new Tema(); t.setNombre("Premium"); t.setPrecioBalas(50); t.setActivo(true);
        t = temaRepository.save(t);

        String token = generateValidToken("j1");
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        
        ComprarItemRequest req = new ComprarItemRequest();
        req.setIdTema(t.getIdTema());
        HttpEntity<ComprarItemRequest> entity = new HttpEntity<>(req, headers);

        // WHEN
        ResponseEntity<String> res = restTemplate.postForEntity("/api/tienda/comprar/tema", entity, String.class);

        // THEN
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        Jugador jFinal = jugadorRepository.findById("j1").get();
        assertThat(jFinal.getBalas()).isEqualTo(49);
    }

    @Test
    void testComprarTemaConcurrente_NoDebeGastarDeMas() throws Exception {
        // GIVEN: Jugador con 60 balas y tema de 50. Intenta comprar 2 veces a la vez.
        Jugador j = new Jugador(); j.setIdGoogle("j_concurrente"); j.setTag("JC"); j.setBalas(60); j.setActivo(true);
        jugadorRepository.save(j);

        Tema t = new Tema(); t.setNombre("Premium2"); t.setPrecioBalas(50); t.setActivo(true);
        t = temaRepository.save(t);

        String token = generateValidToken("j_concurrente");
        int numIntentos = 2;
        ExecutorService executor = Executors.newFixedThreadPool(numIntentos);
        List<CompletableFuture<ResponseEntity<String>>> futures = new ArrayList<>();

        for (int i = 0; i < numIntentos; i++) {
            final int idTema = t.getIdTema();
            futures.add(CompletableFuture.supplyAsync(() -> {
                HttpHeaders headers = new HttpHeaders();
                headers.setBearerAuth(token);
                ComprarItemRequest req = new ComprarItemRequest();
                req.setIdTema(idTema);
                HttpEntity<ComprarItemRequest> entity = new HttpEntity<>(req, headers);
                return restTemplate.postForEntity("/api/tienda/comprar/tema", entity, String.class);
            }, executor));
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        AtomicInteger successCount = new AtomicInteger();
        for (CompletableFuture<ResponseEntity<String>> f : futures) {
            if (f.get().getStatusCode() == HttpStatus.OK) successCount.incrementAndGet();
        }

        // THEN: Solo una compra debe tener éxito
        assertThat(successCount.get()).isEqualTo(1);
        Jugador jFinal = jugadorRepository.findById("j_concurrente").get();
        assertThat(jFinal.getBalas()).isEqualTo(10); // 60 - 50

        executor.shutdown();
    }
}
