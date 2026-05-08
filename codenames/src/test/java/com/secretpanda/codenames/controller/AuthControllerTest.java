package com.secretpanda.codenames.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.secretpanda.codenames.dto.auth.AuthResponseDTO;
import com.secretpanda.codenames.dto.auth.LoginRequestDTO;
import com.secretpanda.codenames.dto.jugador.JugadorDTO;
import com.secretpanda.codenames.security.JwtService;
import com.secretpanda.codenames.service.AuthService;

/**
 * Suite de pruebas unitarias para AuthController.
 * Valida los endpoints de autenticación y la correcta gestión de cookies de seguridad.
 */
@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false) // Deshabilitamos filtros de seguridad para test unitario aislado
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private com.secretpanda.codenames.repository.JugadorRepository jugadorRepository;

    @MockBean
    private JwtService jwtService;

    /**
     * Prueba: shouldReturnOkAndAuthDTOWhenGoogleTokenIsValid
     * Verifica que al proporcionar un token de Google válido de un usuario existente,
     * el controlador devuelva estado 200, la respuesta DTO completa,
     * y más críticamente, establezca correctamente la Cookie de sesión 'token_sesion'
     * con las directivas de seguridad HttpOnly y Secure requeridas.
     */
    @Test
    void shouldReturnOkAndAuthDTOWhenGoogleTokenIsValid() throws Exception {
        // 1. Preparación (Arrange)
        // Creamos el payload de la petición de login
        LoginRequestDTO requestDTO = new LoginRequestDTO();
        requestDTO.setIdGoogle("token_simulado_google");

        // Creamos la respuesta simulada del AuthService indicando que el usuario ya existe y su token JWT
        AuthResponseDTO mockResponse = AuthResponseDTO.existente("jwt_mock", new JugadorDTO(), null);
        when(authService.login("token_simulado_google")).thenReturn(mockResponse);

        // 2 & 3. Ejecución y Verificación (Act & Assert)
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                // Verificamos estado 200 OK
                .andExpect(status().isOk())
                // Verificamos que el body JSON tenga la estructura esperada
                .andExpect(jsonPath("$.es_nuevo").value(false))
                .andExpect(jsonPath("$.token").value("jwt_mock"))
                // Verificamos rigurosamente que la Cookie de seguridad se establece correctamente
                .andExpect(cookie().value("token_sesion", "jwt_mock"))
                .andExpect(cookie().httpOnly("token_sesion", true))
                .andExpect(cookie().secure("token_sesion", true));
    }
}