package com.secretpanda.codenames.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.secretpanda.codenames.dto.auth.AuthResponseDTO;
import com.secretpanda.codenames.exception.BadRequestException;
import com.secretpanda.codenames.model.InventarioTema;
import com.secretpanda.codenames.model.Jugador;
import com.secretpanda.codenames.model.Tema;
import com.secretpanda.codenames.repository.InventarioTemaRepository;
import com.secretpanda.codenames.repository.JugadorPartidaRepository;
import com.secretpanda.codenames.repository.JugadorRepository;
import com.secretpanda.codenames.repository.TemaRepository;
import com.secretpanda.codenames.security.GoogleAuthService;
import com.secretpanda.codenames.security.GoogleAuthService.DatosGoogle;
import com.secretpanda.codenames.security.JwtService;
import com.secretpanda.codenames.util.EstadisticasCalculator;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private GoogleAuthService googleAuthService;
    @Mock private JwtService jwtService;
    @Mock private JugadorRepository jugadorRepository;
    @Mock private JugadorPartidaRepository jugadorPartidaRepository;
    @Mock private EstadisticasCalculator calculator;
    @Mock private TemaRepository temaRepository;
    @Mock private InventarioTemaRepository inventarioTemaRepository;

    @InjectMocks private AuthService authService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "temaBasicoId", 1);
    }

    @Test
    void testLogin_JugadorNoExiste_RetornaEsNuevoTrue() {
        DatosGoogle datos = new DatosGoogle("id_google", "test@test.com", "Test");
        when(googleAuthService.verificarToken("token_valido")).thenReturn(datos);
        when(jugadorRepository.findById("id_google")).thenReturn(Optional.empty());

        AuthResponseDTO response = authService.login("token_valido");

        assertTrue(response.isEsNuevo());
        assertNull(response.getToken());
    }

    @Test
    void testLogin_JugadorExisteYEstaInactivo_RetornaEsNuevoTrue() {
        DatosGoogle datos = new DatosGoogle("id_google", "test@test.com", "Test");
        Jugador jugador = new Jugador();
        jugador.setIdGoogle("id_google");
        jugador.setActivo(false);

        when(googleAuthService.verificarToken("token_valido")).thenReturn(datos);
        when(jugadorRepository.findById("id_google")).thenReturn(Optional.of(jugador));

        AuthResponseDTO response = authService.login("token_valido");

        assertTrue(response.isEsNuevo());
        assertNull(response.getToken());
    }

    @Test
    void testRegistro_JugadorYaActivo_LanzaExcepcion() {
        DatosGoogle datos = new DatosGoogle("id_google", "test@test.com", "Test");
        Jugador jugador = new Jugador();
        jugador.setIdGoogle("id_google");
        jugador.setActivo(true);

        when(googleAuthService.verificarToken("token_valido")).thenReturn(datos);
        when(jugadorRepository.findById("id_google")).thenReturn(Optional.of(jugador));

        assertThrows(BadRequestException.class, () -> {
            authService.registro("token_valido", "NuevoTag");
        });
    }

    @Test
    void testRegistro_JugadorInactivo_ReactivacionCorrecta() {
        DatosGoogle datos = new DatosGoogle("id_google", "test@test.com", "Test");
        Jugador jugadorInactivo = new Jugador();
        jugadorInactivo.setIdGoogle("id_google");
        jugadorInactivo.setActivo(false);
        jugadorInactivo.setTag("ViejoTag");
        jugadorInactivo.setVictorias(100);

        when(googleAuthService.verificarToken("token_valido")).thenReturn(datos);
        when(jugadorRepository.findById("id_google")).thenReturn(Optional.of(jugadorInactivo));
        when(jwtService.generarToken("id_google")).thenReturn("jwt_mock");
        
        // Simular que el tema básico no existe en inventario, se lo debe asignar
        when(inventarioTemaRepository.existsById_IdJugadorAndId_IdTema("id_google", 1)).thenReturn(false);
        when(temaRepository.findById(1)).thenReturn(Optional.of(new Tema()));

        AuthResponseDTO response = authService.registro("token_valido", "NuevoTag");

        assertFalse(response.isEsNuevo());
        assertEquals("jwt_mock", response.getToken());
        assertTrue(jugadorInactivo.isActivo());
        assertEquals("NuevoTag", jugadorInactivo.getTag());
        assertEquals(0, jugadorInactivo.getVictorias()); // Verifica que se resetean las estadísticas
        verify(inventarioTemaRepository).save(any(InventarioTema.class));
        verify(jugadorRepository).save(jugadorInactivo);
    }
}
