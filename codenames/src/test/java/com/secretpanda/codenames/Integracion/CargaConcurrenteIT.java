
package com.secretpanda.codenames.Integracion;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import com.secretpanda.codenames.Integracion.config.IntegrationTestBase;
import com.secretpanda.codenames.dto.partida.CrearPartidaDTO;
import com.secretpanda.codenames.dto.partida.LobbyStatusDTO;

public class CargaConcurrenteIT extends IntegrationTestBase {

    private static final int NUM_PARTIDAS          = 100;
    private static final int JUGADORES_POR_PARTIDA = 4;
    private static final int TOTAL_JUGADORES       = NUM_PARTIDAS * JUGADORES_POR_PARTIDA;
    private static final int THREAD_POOL_SIZE      = 100;

    private static final long TIMEOUT_CREACION_S = 120;
    private static final long TIMEOUT_UNION_S    = 120;
    private static final long TIMEOUT_INICIO_S   = 240;

    @Test
    void testCien_PartidasConcurrentes_DebenCrearseYEmpezarCorrectamente() throws Exception {

        // 1. GIVEN ─────────────────────────────────────────────────────────────
        setupTemaBasico();

        StringBuilder sbJugadores = new StringBuilder(
            "INSERT INTO jugador (id_google, tag, balas, activo, fecha_registro, " +
            "partidas_jugadas, victorias, num_aciertos, num_fallos, token_actual) VALUES ");
        StringBuilder sbInventario = new StringBuilder(
            "INSERT INTO inventario_tema (id_jugador, id_tema) VALUES ");

        List<String> tokens = new ArrayList<>(TOTAL_JUGADORES);

        for (int i = 0; i < TOTAL_JUGADORES; i++) {
            String idGoogle = "carga_user_" + i;
            String token = jwtService.generarToken(idGoogle);
            tokens.add(token);

            if (i > 0) sbJugadores.append(", ");
            sbJugadores.append(String.format(
                "('%s', 'Panda_%d', 100, true, NOW(), 0, 0, 0, 0, '%s')",
                idGoogle, i, token));

            if (i < NUM_PARTIDAS) {
                if (i > 0) sbInventario.append(", ");
                sbInventario.append(String.format("('%s', 1)", idGoogle));
            }
        }

        jdbcTemplate.execute(sbJugadores.toString());
        jdbcTemplate.execute(sbInventario.toString());

        ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        AtomicInteger errores5xx = new AtomicInteger(0);

        try {
            // 2. FASE 1: crear 100 partidas concurrentemente ───────────────────
            List<CompletableFuture<ResponseEntity<LobbyStatusDTO>>> creationFutures = new ArrayList<>();

            CrearPartidaDTO crearDto = new CrearPartidaDTO();
            crearDto.setIdTema(1);
            crearDto.setEsPublica(true);
            crearDto.setMaxJugadores(JUGADORES_POR_PARTIDA);
            crearDto.setTiempoEspera(60);

            for (int i = 0; i < NUM_PARTIDAS; i++) {
                final int index = i;
                creationFutures.add(CompletableFuture.supplyAsync(() -> {
                    HttpHeaders headers = new HttpHeaders();
                    headers.setBearerAuth(tokens.get(index));
                    ResponseEntity<LobbyStatusDTO> res = restTemplate.postForEntity(
                        "/api/partidas/",
                        new HttpEntity<>(crearDto, headers),
                        LobbyStatusDTO.class);
                    if (res.getStatusCode().is5xxServerError()) errores5xx.incrementAndGet();
                    return res;
                }, executor));
            }

            CompletableFuture.allOf(creationFutures.toArray(new CompletableFuture[0]))
                .get(TIMEOUT_CREACION_S, TimeUnit.SECONDS);

            List<Integer> idPartidas = new ArrayList<>();
            for (var future : creationFutures) {
                ResponseEntity<LobbyStatusDTO> res = future.get();
                assertThat(res.getStatusCode().is2xxSuccessful())
                    .as("Crear partida debe devolver 2xx, recibido: %s", res.getStatusCode())
                    .isTrue();
                idPartidas.add(res.getBody().getIdPartida());
            }

            // 3. FASE 2: unirse a las partidas (300 uniones) ───────────────────
            List<CompletableFuture<ResponseEntity<Void>>> joinFutures = new ArrayList<>();

            for (int p = 0; p < NUM_PARTIDAS; p++) {
                final int idPartida = idPartidas.get(p);
                for (int j = 1; j < JUGADORES_POR_PARTIDA; j++) {
                    final int playerIndex = p + (j * NUM_PARTIDAS);
                    joinFutures.add(CompletableFuture.supplyAsync(() -> {
                        HttpHeaders headers = new HttpHeaders();
                        headers.setBearerAuth(tokens.get(playerIndex));
                        ResponseEntity<Void> res = restTemplate.postForEntity(
                            "/api/partidas/" + idPartida + "/unirse/publica",
                            new HttpEntity<>(headers),
                            Void.class);
                        if (res.getStatusCode().is5xxServerError()) errores5xx.incrementAndGet();
                        return res;
                    }, executor));
                }
            }

            // Esperar a que TODOS los joins estén en BD antes de asignar equipos
            CompletableFuture.allOf(joinFutures.toArray(new CompletableFuture[0]))
                .get(TIMEOUT_UNION_S, TimeUnit.SECONDS);

            // FASE 2.5: asignar equipos directamente en BD
            // Primeros 2 por id_jugador_partida → rojo, siguientes 2 → azul
            for (int p = 0; p < NUM_PARTIDAS; p++) {
                final int idPartida = idPartidas.get(p);
                jdbcTemplate.update(
                    "UPDATE jugador_partida SET equipo = 'rojo' " +
                    "WHERE id_partida = ? AND id_jugador_partida IN (" +
                    "  SELECT id_jugador_partida FROM jugador_partida " +
                    "  WHERE id_partida = ? AND abandono = false ORDER BY id_jugador_partida LIMIT 2" +
                    ")", idPartida, idPartida);
                jdbcTemplate.update(
                    "UPDATE jugador_partida SET equipo = 'azul' " +
                    "WHERE id_partida = ? AND id_jugador_partida IN (" +
                    "  SELECT id_jugador_partida FROM jugador_partida " +
                    "  WHERE id_partida = ? AND abandono = false ORDER BY id_jugador_partida OFFSET 2 LIMIT 2" +
                    ")", idPartida, idPartida);
            }

            // 4. FASE 3: iniciar las 100 partidas ──────────────────────────────
            // TEMPORAL: String.class para ver el body exacto del error 400
            List<CompletableFuture<ResponseEntity<String>>> startFutures = new ArrayList<>();

            for (int i = 0; i < NUM_PARTIDAS; i++) {
                final int index     = i;
                final int idPartida = idPartidas.get(index);
                startFutures.add(CompletableFuture.supplyAsync(() -> {
                    HttpHeaders headers = new HttpHeaders();
                    headers.setBearerAuth(tokens.get(index));
                    ResponseEntity<String> res = restTemplate.exchange(
                        "/api/partida/" + idPartida + "/iniciar",
                        HttpMethod.PUT,
                        new HttpEntity<>(headers),
                        String.class);
                    if (res.getStatusCode().is5xxServerError()) errores5xx.incrementAndGet();
                    return res;
                }, executor));
            }

            CompletableFuture.allOf(startFutures.toArray(new CompletableFuture[0]))
                .get(TIMEOUT_INICIO_S, TimeUnit.SECONDS);

            for (var future : startFutures) {
                ResponseEntity<String> res = future.get();
                assertThat(res.getStatusCode().is2xxSuccessful())
                    .as("Iniciar partida debe devolver 2xx, recibido: %s — body: %s",
                        res.getStatusCode(), res.getBody())
                    .isTrue();
            }

            // 5. THEN ──────────────────────────────────────────────────────────
            assertThat(errores5xx.get())
                .as("No debe haber ningún error 5xx en ninguna de las fases")
                .isEqualTo(0);

            Long partidasEnCurso = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM partida WHERE estado = 'en_curso'", Long.class);
            assertThat(partidasEnCurso)
                .as("Deben existir exactamente 100 partidas en curso en BD")
                .isEqualTo((long) NUM_PARTIDAS);

            Long totalParticipantes = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM jugador_partida", Long.class);
            assertThat(totalParticipantes)
                .as("Deben existir exactamente 400 participantes en BD")
                .isEqualTo((long) TOTAL_JUGADORES);

        } finally {
            executor.shutdown();
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        }
    }
}