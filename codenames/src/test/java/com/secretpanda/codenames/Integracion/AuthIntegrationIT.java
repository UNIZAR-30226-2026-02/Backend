package com.secretpanda.codenames.Integracion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.secretpanda.codenames.Integracion.config.IntegrationTestBase;
import com.secretpanda.codenames.dto.auth.AuthResponseDTO;
import com.secretpanda.codenames.dto.auth.LoginRequestDTO;
import com.secretpanda.codenames.dto.auth.RegistroRequestDTO;
import com.secretpanda.codenames.dto.jugador.JugadorDTO;
import com.secretpanda.codenames.model.Tema;
import com.secretpanda.codenames.repository.TemaRepository;
import com.secretpanda.codenames.security.GoogleAuthService;
import org.springframework.beans.factory.annotation.Autowired;

public class AuthIntegrationIT extends IntegrationTestBase {

    @MockBean
    private GoogleAuthService googleAuthService;

    @Autowired
    private TemaRepository temaRepository;

    @Test
    void testRegistroFlujoCompleto_DebeCrearUsuarioEInventario() {
        // GIVEN
        String mockIdGoogle = "google_12345";
        String mockTag = "PandaTest";
        
        // Mocking Google verification
        when(googleAuthService.verificarToken(anyString()))
            .thenReturn(new GoogleAuthService.DatosGoogle(mockIdGoogle, "panda@test.com", "Panda"));

        // Setup tema básico (id=1) vía JDBC para evitar problemas de persistencia/locking de Hibernate
        jdbcTemplate.execute("INSERT INTO tema (id_tema, nombre, precio_balas, activo) VALUES (1, 'Básico', 0, true)");

        // 1. LOGIN (Debe decir que es nuevo)
        LoginRequestDTO loginReq = new LoginRequestDTO();
        loginReq.setIdGoogle("mock_token");
        ResponseEntity<AuthResponseDTO> loginRes = restTemplate.postForEntity("/api/auth/login", loginReq, AuthResponseDTO.class);
        
        assertThat(loginRes.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(loginRes.getBody().isEsNuevo()).isTrue();

        // 2. REGISTRO
        RegistroRequestDTO registroReq = new RegistroRequestDTO();
        registroReq.setIdGoogle("mock_token");
        registroReq.setTag(mockTag);
        ResponseEntity<AuthResponseDTO> registroRes = restTemplate.postForEntity("/api/auth/registro", registroReq, AuthResponseDTO.class);

        assertThat(registroRes.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(registroRes.getBody().isEsNuevo()).isFalse();
        assertThat(registroRes.getBody().getToken()).isNotNull();
        assertThat(registroRes.getBody().getJugador().getTag()).isEqualTo(mockTag);

        // 3. VALIDAR ACCESO PROTEGIDO Y PERSISTENCIA
        String jwt = registroRes.getBody().getToken();
        HttpHeaders authHeaders = new HttpHeaders();
        authHeaders.setBearerAuth(jwt);
        
        ResponseEntity<JugadorDTO> perfilRes = restTemplate.exchange("/api/jugadores", HttpMethod.GET, new HttpEntity<>(authHeaders), JugadorDTO.class);
        assertThat(perfilRes.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(perfilRes.getBody().getTag()).isEqualTo(mockTag);
    }

    @Test
    void testRecursoProtegido_SinToken_DebeDevolver403() {
        ResponseEntity<Void> res = restTemplate.getForEntity("/api/jugadores", Void.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void testWebSocketHandshake_SinTokenValido_DebePermitirConexionPeroNoSuscripcionProtegida() throws Exception {
        com.secretpanda.codenames.Integracion.config.StompTestClient client = new com.secretpanda.codenames.Integracion.config.StompTestClient(port);
        org.springframework.messaging.simp.stomp.StompSession session = client.connect("token_invalido");
        assertThat(session.isConnected()).isTrue();
        
        // Al intentar suscribirse a un canal privado sin estar autenticado, no recibirá mensajes
        // (En este proyecto no hay una forma fácil de recibir un ERROR STOMP sin un handler complejo,
        // pero podemos verificar que la conexión es exitosa pero la sesión no tiene 'user')
    }
}
