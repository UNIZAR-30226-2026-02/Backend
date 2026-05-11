
package com.secretpanda.codenames.Integracion.config;

import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.secretpanda.codenames.security.JwtService;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
public abstract class IntegrationTestBase {

    @Container
    protected static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("codenames_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void postgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @LocalServerPort
    protected int port;

    @Autowired
    protected TestRestTemplate restTemplate;

    @Autowired
    protected JwtService jwtService;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    protected String generateValidToken(String idGoogle) {
        String token = jwtService.generarToken(idGoogle);
        jdbcTemplate.update("UPDATE jugador SET token_actual = ? WHERE id_google = ?", token, idGoogle);
        return token;
    }

    protected void setupTemaBasico() {
        jdbcTemplate.execute(
            "INSERT INTO tema (id_tema, nombre, precio_balas, activo) " +
            "VALUES (1, 'Básico', 0, true) ON CONFLICT (id_tema) DO NOTHING"
        );
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.execute("TRUNCATE TABLE voto_carta CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE turno CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE tablero_carta CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE jugador_partida CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE partida CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE amistad CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE jugador_logro CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE inventario_personalizacion CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE inventario_tema CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE palabra_tema CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE tema CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE personalizacion CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE logro CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE jugador CASCADE");
    }
}