
package com.secretpanda.codenames.Integracion.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

import com.secretpanda.codenames.security.JwtService;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public abstract class IntegrationTestBase {

    protected static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("codenames_test")
            .withUsername("test")
            .withPassword("test");

    static {
        postgres.start();
    }

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

    /*protected void setupTemaBasico() {
        jdbcTemplate.execute(
            "INSERT INTO tema (id_tema, nombre, precio_balas, activo) " +
            "VALUES (1, 'Básico', 0, true) ON CONFLICT (id_tema) DO NOTHING"
        );
    }*/

    protected void setupTemaBasico() {
        jdbcTemplate.execute(
            "INSERT INTO tema (id_tema, nombre, precio_balas, activo) " +
            "VALUES (1, 'Básico', 0, true) ON CONFLICT (id_tema) DO NOTHING"
        );
        jdbcTemplate.execute(
            "INSERT INTO palabra_tema (id_tema, valor, activo) VALUES " +
            "(1, 'AGENTE', true), (1, 'BANCO', true), (1, 'BARCO', true), " +
            "(1, 'CAMPO', true), (1, 'CARTA', true), (1, 'CIUDAD', true), " +
            "(1, 'CLASE', true), (1, 'COBRA', true), (1, 'DIANA', true), " +
            "(1, 'DISCO', true), (1, 'ESPÍA', true), (1, 'FARO', true),  " +
            "(1, 'FUEGO', true), (1, 'GANCHO', true), (1, 'GLOBO', true), " +
            "(1, 'HIELO', true), (1, 'LLAVE', true), (1, 'LUNA', true),  " +
            "(1, 'MANGO', true), (1, 'MARCA', true), (1, 'NOCHE', true), " +
            "(1, 'OPERA', true), (1, 'PARIS', true), (1, 'PISTA', true), " +
            "(1, 'PLUMA', true) ON CONFLICT DO NOTHING"
        );
    }

    @BeforeEach
    void setUp() {
        limpiarBD();
    }

    @AfterEach
    void tearDown() {
        limpiarBD();
    }

    private void limpiarBD() {
        jdbcTemplate.execute("""
            TRUNCATE TABLE
                voto_carta, turno, tablero_carta,
                jugador_partida, partida,
                amistad, jugador_logro,
                inventario_personalizacion, inventario_tema,
                palabra_tema, tema,
                personalizacion, logro, jugador
            RESTART IDENTITY CASCADE
        """);
    }
}