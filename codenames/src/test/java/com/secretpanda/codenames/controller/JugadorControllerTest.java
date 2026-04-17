package com.secretpanda.codenames.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.security.Principal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.secretpanda.codenames.dto.jugador.ActualizarPerfilDTO;
import com.secretpanda.codenames.dto.jugador.JugadorDTO;
import com.secretpanda.codenames.security.JwtService;
import com.secretpanda.codenames.service.JugadorService;

@WebMvcTest(JugadorController.class)
@AutoConfigureMockMvc(addFilters = false)
class JugadorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JugadorService jugadorService;

    @MockBean
    private JwtService jwtService;

    @Test
    void testGetPerfil_Exito_DevuelveDatos() throws Exception {
        JugadorDTO mockDTO = new JugadorDTO();
        mockDTO.setIdGoogle("test_id");
        mockDTO.setTag("PandaTester");
        mockDTO.setBalas(100);

        when(jugadorService.getPerfil("test_id")).thenReturn(mockDTO);

        Principal mockPrincipal = () -> "test_id";

        mockMvc.perform(get("/api/jugadores")
                .principal(mockPrincipal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tag").value("PandaTester"))
                .andExpect(jsonPath("$.balas").value(100)); // Usando JsonProperty
    }

    @Test
    void testActualizarPerfil_Valido_DevuelveActualizado() throws Exception {
        ActualizarPerfilDTO requestDTO = new ActualizarPerfilDTO();
        requestDTO.setTag("NuevoTag");

        JugadorDTO mockDTO = new JugadorDTO();
        mockDTO.setTag("NuevoTag");

        when(jugadorService.actualizarPerfil(any(ActualizarPerfilDTO.class), eq("test_id")))
                .thenReturn(mockDTO);

        Principal mockPrincipal = () -> "test_id";

        mockMvc.perform(put("/api/jugadores")
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tag").value("NuevoTag"));
    }
}
