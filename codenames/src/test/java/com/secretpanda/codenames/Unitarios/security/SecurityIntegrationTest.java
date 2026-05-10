package com.secretpanda.codenames.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.anyString;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.secretpanda.codenames.model.Jugador;
import com.secretpanda.codenames.repository.JugadorRepository;
import java.util.Optional;
import jakarta.servlet.http.Cookie;

/**
 * Prueba de integración para validar la correcta configuración de seguridad
 * y el funcionamiento del filtro JWT.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private JugadorRepository jugadorRepository;

    /**
     * Prueba que los endpoints públicos no requieran autenticación.
     */
    @Test
    public void shouldAllowAccessToPublicEndpointsWithoutAuth() throws Exception {
        mockMvc.perform(get("/api/temas/activos"))
                .andExpect(status().isOk());
    }

    /**
     * Prueba que un endpoint protegido devuelve 403 (Forbidden) si no hay token.
     * El filtro JwtFilter no intercepta, y Spring Security rechaza la petición.
     */
    @Test
    public void shouldReturn403WhenAccessingProtectedEndpointWithoutToken() throws Exception {
        mockMvc.perform(get("/api/jugadores"))
                .andExpect(status().isForbidden()); // Spring Boot devuelve 403 por defecto para accesos denegados
    }

    /**
     * Prueba que un endpoint protegido devuelve 403 si el token es inválido.
     */
    @Test
    public void shouldReturn403WhenTokenIsInvalid() throws Exception {
        when(jwtService.esTokenValido("token_invalido")).thenReturn(false);
        when(jwtService.extraerIdGoogle("token_invalido")).thenReturn("user123");

        Cookie invalidCookie = new Cookie("token_sesion", "token_invalido");

        mockMvc.perform(get("/api/jugadores").cookie(invalidCookie))
                .andExpect(status().isForbidden());
    }

    /**
     * Prueba que se permite el acceso a un endpoint protegido con un token válido.
     */
    @Test
    public void shouldAllowAccessWithValidToken() throws Exception {
        String validToken = "token_valido";
        String userId = "user123";

        Jugador jugador = new Jugador();
        jugador.setIdGoogle(userId);
        jugador.setTokenActual(validToken); // Evitar invalidación por multi-sesión
        jugador.setActivo(true);

        when(jwtService.extraerIdGoogle(validToken)).thenReturn(userId);
        when(jwtService.esTokenValido(validToken)).thenReturn(true);
        when(jugadorRepository.findById(userId)).thenReturn(Optional.of(jugador));
        when(jugadorRepository.findTokenActualById(userId)).thenReturn(Optional.of(validToken));

        Cookie validCookie = new Cookie("token_sesion", validToken);

        mockMvc.perform(get("/api/jugadores").cookie(validCookie))
                .andExpect(status().isOk());
    }
}