package com.secretpanda.codenames.Unitarios.service;

import com.secretpanda.codenames.service.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.List;

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
import com.secretpanda.codenames.model.JugadorPartida;
import com.secretpanda.codenames.model.Partida;
import com.secretpanda.codenames.model.Tema;
import com.secretpanda.codenames.repository.InventarioTemaRepository;
import com.secretpanda.codenames.repository.JugadorPartidaRepository;
import com.secretpanda.codenames.repository.JugadorRepository;
import com.secretpanda.codenames.repository.TemaRepository;
import com.secretpanda.codenames.security.GoogleAuthService;
import com.secretpanda.codenames.security.GoogleAuthService.DatosGoogle;
import com.secretpanda.codenames.security.JwtService;
import com.secretpanda.codenames.util.EstadisticasCalculator;

/**
 * Suite de pruebas unitarias para AuthService.
 * Valida de forma exhaustiva los flujos de autenticación, registro, y reactivación de cuentas.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    // Mocks de dependencias requeridas por AuthService
    @Mock private GoogleAuthService googleAuthService;
    @Mock private JwtService jwtService;
    @Mock private JugadorRepository jugadorRepository;
    @Mock private JugadorPartidaRepository jugadorPartidaRepository;
    @Mock private EstadisticasCalculator calculator;
    @Mock private TemaRepository temaRepository;
    @Mock private InventarioTemaRepository inventarioTemaRepository;
    @Mock private JugadorService jugadorService;

    // Inyección de los mocks en la instancia a testear
    @InjectMocks private AuthService authService;

    @BeforeEach
    void setUp() {
        // Configuramos el valor por defecto inyectado por @Value en el servicio
        ReflectionTestUtils.setField(authService, "temaBasicoId", 1);
    }

    /**
     * Prueba: shouldReturnNewUserStatusWhenPlayerDoesNotExist
     * Verifica que si un jugador no existe en la base de datos, 
     * el servicio devuelva un estado indicando que es un usuario nuevo (esNuevo = true).
     */
    @Test
    void shouldReturnNewUserStatusWhenPlayerDoesNotExist() {
        // 1. Preparación (Arrange)
        // Simulamos los datos devueltos por Google al verificar el token
        DatosGoogle datos = new DatosGoogle("id_google", "test@test.com", "Test");
        when(googleAuthService.verificarToken("token_valido")).thenReturn(datos);
        // Simulamos que el repositorio no encuentra al jugador
        when(jugadorRepository.findById("id_google")).thenReturn(Optional.empty());

        // 2. Ejecución (Act)
        // Llamamos al método login del servicio
        AuthResponseDTO response = authService.login("token_valido");

        // 3. Verificación (Assert)
        // Verificamos que la respuesta indique que es un usuario nuevo
        assertTrue(response.isEsNuevo());
        // Verificamos que no se haya generado un token JWT
        assertNull(response.getToken());
    }

    /**
     * Prueba: shouldReturnNewUserStatusWhenPlayerIsInactive
     * Verifica que si un jugador existe pero está inactivo, 
     * el servicio lo trate como un usuario nuevo para forzar su registro/reactivación.
     */
    @Test
    void shouldReturnNewUserStatusWhenPlayerIsInactive() {
        // 1. Preparación (Arrange)
        DatosGoogle datos = new DatosGoogle("id_google", "test@test.com", "Test");
        Jugador jugador = new Jugador();
        jugador.setIdGoogle("id_google");
        jugador.setActivo(false); // Estado inactivo

        when(googleAuthService.verificarToken("token_valido")).thenReturn(datos);
        when(jugadorRepository.findById("id_google")).thenReturn(Optional.of(jugador));

        // 2. Ejecución (Act)
        AuthResponseDTO response = authService.login("token_valido");

        // 3. Verificación (Assert)
        assertTrue(response.isEsNuevo());
        assertNull(response.getToken());
    }

    /**
     * Prueba: shouldThrowExceptionWhenRegisteringAlreadyActivePlayer
     * Verifica que un jugador activo no pueda volver a registrarse.
     */
    @Test
    void shouldThrowExceptionWhenRegisteringAlreadyActivePlayer() {
        // 1. Preparación (Arrange)
        DatosGoogle datos = new DatosGoogle("id_google", "test@test.com", "Test");
        Jugador jugador = new Jugador();
        jugador.setIdGoogle("id_google");
        jugador.setActivo(true); // Estado activo

        when(googleAuthService.verificarToken("token_valido")).thenReturn(datos);
        when(jugadorRepository.findById("id_google")).thenReturn(Optional.of(jugador));

        // 2 & 3. Ejecución y Verificación (Act & Assert)
        // Esperamos una BadRequestException ya que el jugador ya está activo
        assertThrows(BadRequestException.class, () -> {
            authService.registro("token_valido", "NuevoTag");
        });
    }

    /**
     * Prueba: shouldReactivatePlayerAndResetStatsCorrectly
     * Verifica que si un usuario inactivo se registra de nuevo, 
     * su cuenta se reactive, sus estadísticas se reinicien, 
     * se le asigne el tema básico si no lo tiene, y se inicialicen sus logros.
     */
    @Test
    void shouldReactivatePlayerAndResetStatsCorrectly() {
        // 1. Preparación (Arrange)
        DatosGoogle datos = new DatosGoogle("id_google", "test@test.com", "Test");
        Jugador jugadorInactivo = new Jugador();
        jugadorInactivo.setIdGoogle("id_google");
        jugadorInactivo.setActivo(false);
        jugadorInactivo.setTag("ViejoTag");
        jugadorInactivo.setVictorias(100); // Estadísticas que deben reiniciarse

        when(googleAuthService.verificarToken("token_valido")).thenReturn(datos);
        when(jugadorRepository.findById("id_google")).thenReturn(Optional.of(jugadorInactivo));
        when(jwtService.generarToken("id_google")).thenReturn("jwt_mock");
        
        // Simulamos que el jugador NO tiene el tema básico
        when(inventarioTemaRepository.existsById_IdJugadorAndId_IdTema("id_google", 1)).thenReturn(false);
        when(temaRepository.findById(1)).thenReturn(Optional.of(new Tema()));

        // 2. Ejecución (Act)
        AuthResponseDTO response = authService.registro("token_valido", "NuevoTag");

        // 3. Verificación (Assert)
        // Verificamos que la respuesta indique que NO es un usuario nuevo (registro exitoso)
        assertFalse(response.isEsNuevo());
        // Verificamos que se haya devuelto el token JWT
        assertEquals("jwt_mock", response.getToken());
        // Verificamos la reactivación y actualización de datos
        assertTrue(jugadorInactivo.isActivo());
        assertEquals("NuevoTag", jugadorInactivo.getTag());
        // Verificamos el reinicio de estadísticas
        assertEquals(0, jugadorInactivo.getVictorias()); 
        // Verificamos que se haya guardado el tema básico en el inventario
        verify(inventarioTemaRepository).save(any(InventarioTema.class));
        // Verificamos que se haya guardado el jugador (registro + saveAndFlush en sesión única)
        verify(jugadorRepository, times(1)).save(jugadorInactivo);
        verify(jugadorRepository, times(1)).saveAndFlush(jugadorInactivo);
        // Verificamos que se hayan inicializado los logros del jugador (Evita falso positivo)
        verify(jugadorService).inicializarLogros(jugadorInactivo);
    }

    /**
     * Prueba: shouldReturnExistingUserWithTokenAndActiveMatch
     * Verifica que si el jugador ya existe y está activo,
     * el login devuelva sus datos, el token JWT y el ID de la partida activa si tiene una.
     */
    @Test
    void shouldReturnExistingUserWithTokenAndActiveMatch() {
        // 1. Preparación (Arrange)
        DatosGoogle datos = new DatosGoogle("id_google", "test@test.com", "Test");
        Jugador jugador = new Jugador();
        jugador.setIdGoogle("id_google");
        jugador.setActivo(true); // Estado activo

        when(googleAuthService.verificarToken("token_valido")).thenReturn(datos);
        when(jugadorRepository.findById("id_google")).thenReturn(Optional.of(jugador));
        when(jwtService.generarToken("id_google")).thenReturn("jwt_mock");

        // Simulamos que el jugador tiene una partida en curso
        Partida partidaActiva = new Partida();
        partidaActiva.setIdPartida(99);
        JugadorPartida jp = new JugadorPartida();
        jp.setPartida(partidaActiva);
        
        when(jugadorPartidaRepository.findFirstByJugador_IdGoogleAndPartida_EstadoInAndAbandonoFalse(
                eq("id_google"), anyList())).thenReturn(Optional.of(jp));

        // 2. Ejecución (Act)
        AuthResponseDTO response = authService.login("token_valido");

        // 3. Verificación (Assert)
        // Verificamos que la respuesta indique que NO es nuevo
        assertFalse(response.isEsNuevo());
        // Verificamos el token
        assertEquals("jwt_mock", response.getToken());
        // Verificamos que se devuelva el ID de la partida activa (en el DTO de respuesta)
        assertEquals(99, response.getPartidaActivaId());
        // Verificamos que se haya guardado el token actual en el jugador (sesión única)
        verify(jugadorRepository).saveAndFlush(jugador);
        assertEquals("jwt_mock", jugador.getTokenActual());
    }
}