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
import com.secretpanda.codenames.model.Jugador;
import com.secretpanda.codenames.model.Partida;
import com.secretpanda.codenames.model.Tema;
import com.secretpanda.codenames.repository.JugadorRepository;
import com.secretpanda.codenames.repository.PartidaRepository;
import com.secretpanda.codenames.repository.TemaRepository;

public class LobbyIntegrationIT extends IntegrationTestBase {

    @Autowired
    private JugadorRepository jugadorRepository;

    @Autowired
    private PartidaRepository partidaRepository;

    @Autowired
    private TemaRepository temaRepository;

    @Test
    void testUnionConcurrente_NoDebeExcederMaxJugadores() throws Exception {
        // GIVEN: Un tema, un creador y una partida con max 2 jugadores
        setupTemaBasico();

        Jugador creador = new Jugador();
        creador.setIdGoogle("creador");
        creador.setTag("Creador");
        creador.setActivo(true);
        jugadorRepository.save(creador);

        Partida partida = new Partida();
        partida.setCreador(creador);
        partida.setTema(temaRepository.findById(1).get());
        partida.setMaxJugadores(4); // Mínimo 4 según la entidad
        partida.setEsPublica(true);
        partida.setEstado(Partida.EstadoPartida.esperando);
        partida.setCodigoPartida("LOBBY1");
        partida = partidaRepository.save(partida);

        // Ya hay 1 jugador (el creador)
        jdbcTemplate.execute("INSERT INTO jugador_partida (id_partida, id_jugador, equipo, rol, abandono, num_aciertos, num_fallos) VALUES (" + 
            partida.getIdPartida() + ", 'creador', 'rojo', 'agente', false, 0, 0)");

        // 4 Jugadores intentando unirse a los 3 huecos libres
        int numIntentos = 4;
        ExecutorService executor = Executors.newFixedThreadPool(numIntentos);
        List<CompletableFuture<ResponseEntity<Void>>> futures = new ArrayList<>();
        
        for (int i = 0; i < numIntentos; i++) {
            String idGoogle = "user_" + i;
            Jugador j = new Jugador();
            j.setIdGoogle(idGoogle);
            j.setTag("User" + i);
            j.setActivo(true);
            jugadorRepository.save(j);
            
            String token = generateValidToken(idGoogle);
            final int idPartidaFinal = partida.getIdPartida();
            
            futures.add(CompletableFuture.supplyAsync(() -> {
                HttpHeaders headers = new HttpHeaders();
                headers.setBearerAuth(token);
                HttpEntity<Void> entity = new HttpEntity<>(headers);
                return restTemplate.postForEntity("/api/partidas/" + idPartidaFinal + "/unirse/publica", entity, Void.class);
            }, executor));
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger errorCount = new AtomicInteger();

        for (CompletableFuture<ResponseEntity<Void>> f : futures) {
            ResponseEntity<Void> res = f.get();
            if (res.getStatusCode() == HttpStatus.OK) {
                successCount.incrementAndGet();
            } else {
                errorCount.incrementAndGet();
            }
        }

        // VALIDACIÓN
        long finalCount = jdbcTemplate.queryForObject(
            "SELECT count(*) FROM jugador_partida WHERE id_partida = " + partida.getIdPartida(), Long.class);

        assertThat(finalCount).isEqualTo(4); // Creador + 3
        assertThat(successCount.get()).isEqualTo(3);
        assertThat(errorCount.get()).isEqualTo(1);

        executor.shutdown();
    }
}
