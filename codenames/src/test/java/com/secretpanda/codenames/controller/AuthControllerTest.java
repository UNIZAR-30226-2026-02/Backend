package com.secretpanda.codenames.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtService jwtService;

    @Test
    void testLogin_ConTokenGoogleValido_Devuelve200YDTO() throws Exception {
        LoginRequestDTO requestDTO = new LoginRequestDTO();
        requestDTO.setIdGoogle("token_simulado_google");

        AuthResponseDTO mockResponse = AuthResponseDTO.existente("jwt_mock", new JugadorDTO(), null);
        when(authService.login("token_simulado_google")).thenReturn(mockResponse);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.es_nuevo").value(false))
                .andExpect(jsonPath("$.token").value("jwt_mock"));
    }
}
