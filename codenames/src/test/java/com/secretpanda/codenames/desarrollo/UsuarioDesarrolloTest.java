package com.secretpanda.codenames.desarrollo;

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
import org.springframework.messaging.simp.SimpMessagingTemplate;

import com.secretpanda.codenames.dto.auth.AuthResponseDTO;
import com.secretpanda.codenames.dto.jugador.ActualizarPerfilDTO;
import com.secretpanda.codenames.dto.jugador.JugadorDTO;
import com.secretpanda.codenames.model.Jugador;
import com.secretpanda.codenames.model.JugadorPartida;
import com.secretpanda.codenames.repository.JugadorRepository;
import com.secretpanda.codenames.repository.JugadorPartidaRepository;
import com.secretpanda.codenames.repository.InventarioTemaRepository;
import com.secretpanda.codenames.repository.InventarioPersonalizacionRepository;
import com.secretpanda.codenames.repository.TemaRepository;
import com.secretpanda.codenames.repository.PersonalizacionRepository;
import com.secretpanda.codenames.repository.AmistadRepository;
import com.secretpanda.codenames.repository.JugadorLogroRepository;
import com.secretpanda.codenames.repository.LogroRepository;
import com.secretpanda.codenames.service.AuthService;
import com.secretpanda.codenames.service.JugadorService;
import com.secretpanda.codenames.service.PartidaService;
import com.secretpanda.codenames.service.ProfanityFilterService;
import com.secretpanda.codenames.security.GoogleAuthService;
import com.secretpanda.codenames.security.JwtService;
import com.secretpanda.codenames.util.EstadisticasCalculator;

/**
 * Pruebas de Desarrollo: Gestión de Usuarios y Perfiles (RF-1 a RF-8)
 */
@ExtendWith(MockitoExtension.class)
public class UsuarioDesarrolloTest {

    @Mock private GoogleAuthService googleAuthService;
    @Mock private JwtService jwtService;
    @Mock private JugadorRepository jugadorRepository;
    @Mock private JugadorPartidaRepository jugadorPartidaRepository;
    @Mock private InventarioTemaRepository inventarioTemaRepository;
    @Mock private InventarioPersonalizacionRepository inventarioPersoRepository;
    @Mock private TemaRepository temaRepository;
    @Mock private PersonalizacionRepository personalizacionRepository;
    @Mock private AmistadRepository amistadRepository;
    @Mock private JugadorLogroRepository jugadorLogroRepository;
    @Mock private LogroRepository logroRepository;
    @Mock private EstadisticasCalculator calculator;
    @Mock private SimpMessagingTemplate messagingTemplate;
    @Mock private PartidaService partidaService;
    @Mock private ProfanityFilterService profanityFilterService;
    
    @InjectMocks private AuthService authService;
    private JugadorService jugadorService;

    @BeforeEach
    void setUp() {
        jugadorService = new JugadorService(jugadorRepository, inventarioTemaRepository, inventarioPersoRepository, jugadorPartidaRepository, jugadorLogroRepository, calculator, logroRepository, messagingTemplate, profanityFilterService);
        ReflectionTestUtils.setField(authService, "temaBasicoId", 1);
        ReflectionTestUtils.setField(authService, "jugadorService", jugadorService);
        ReflectionTestUtils.setField(authService, "profanityFilterService", profanityFilterService);
    }
    @Test
    void testRegistroConGoogle_Rf1() {
        GoogleAuthService.DatosGoogle datos = new GoogleAuthService.DatosGoogle("google_123", "test@test.com", "User Test");
        lenient().when(googleAuthService.verificarToken("token_valid")).thenReturn(datos);
        lenient().when(jugadorRepository.findById("google_123")).thenReturn(Optional.empty());
        lenient().when(jugadorRepository.existsByTagAndActivoTrue("TagUnico")).thenReturn(false);
        lenient().when(jwtService.generarToken("google_123")).thenReturn("jwt_panda");
        lenient().when(profanityFilterService.filter(anyString())).thenReturn(new ProfanityFilterService.FilterResult("TagUnico", false));

        AuthResponseDTO response = authService.registro("token_valid", "TagUnico");

        assertFalse(response.isEsNuevo());
        assertEquals("jwt_panda", response.getToken());
        verify(jugadorRepository).save(any(Jugador.class));
    }

    @Test
    void testLoginConGoogle_Rf2() {
        GoogleAuthService.DatosGoogle datos = new GoogleAuthService.DatosGoogle("google_123", "test@test.com", "User Test");
        Jugador j = new Jugador();
        j.setIdGoogle("google_123");
        j.setActivo(true);

        lenient().when(googleAuthService.verificarToken("token_valid")).thenReturn(datos);
        lenient().when(jugadorRepository.findById("google_123")).thenReturn(Optional.of(j));
        lenient().when(jwtService.generarToken("google_123")).thenReturn("jwt_panda");

        AuthResponseDTO response = authService.login("token_valid");

        assertFalse(response.isEsNuevo());
        assertEquals("jwt_panda", response.getToken());
    }

    @Test
    void testDesactivarCuenta_Rf4() {
        Jugador j = new Jugador();
        j.setIdGoogle("google_123");
        j.setActivo(true);
        j.setBalas(500);

        when(jugadorRepository.findById("google_123")).thenReturn(Optional.of(j));
        lenient().when(jugadorPartidaRepository.findFirstByJugador_IdGoogleAndPartida_EstadoInAndAbandonoFalse(anyString(), anyList())).thenReturn(Optional.empty());

        authService.desactivarCuenta("google_123");

        assertFalse(j.isActivo());
        assertEquals(0, j.getBalas()); 
    }

    @Test
    void testConsultarInformacionPersonal_Rf5() {
        Jugador j = new Jugador();
        j.setIdGoogle("google_123");
        j.setTag("PandaMaster");
        j.setBalas(100);

        when(jugadorRepository.findById("google_123")).thenReturn(Optional.of(j));

        JugadorDTO perfil = jugadorService.getPerfil("google_123");

        assertEquals("PandaMaster", perfil.getTag());
        assertEquals(100, perfil.getBalas());
    }

    @Test
    void testModificarTagYFoto_Rf6() {
        Jugador j = new Jugador();
        j.setIdGoogle("google_123");
        j.setTag("TagViejo");

        when(jugadorRepository.findById("google_123")).thenReturn(Optional.of(j));
        when(jugadorRepository.existsByTagAndActivoTrue("TagNuevo")).thenReturn(false);

        ActualizarPerfilDTO dto = new ActualizarPerfilDTO();
        dto.setTag("TagNuevo");
        dto.setFotoPerfil("icon_2");

        when(profanityFilterService.filter("TagNuevo")).thenReturn(new ProfanityFilterService.FilterResult("TagNuevo", false));

        jugadorService.actualizarPerfil(dto, "google_123");

        assertEquals("TagNuevo", j.getTag());
        assertEquals("icon_2", j.getFotoPerfil());
    }

    @Test
    void testConsultarHistorial_Rf7() {
        String id = "google_123";
        Jugador j = new Jugador();
        j.setIdGoogle(id);
        j.setActivo(true);
        j.setPartidasJugadas(5);
        when(jugadorRepository.findById(id)).thenReturn(Optional.of(j));
        
        when(jugadorPartidaRepository.findHistoryByJugadorId(id)).thenReturn(List.of(new JugadorPartida()));

        var historial = jugadorService.getHistorial(id);

        assertNotNull(historial);
        assertTrue(historial.size() <= 30); 
    }

    @Test
    void testTagDiferenciacionMayusculas_Rf1() {
        when(jugadorRepository.existsByTagAndActivoTrue("Panda")).thenReturn(false);

        ActualizarPerfilDTO dtoPanda = new ActualizarPerfilDTO();
        dtoPanda.setTag("Panda");
        
        Jugador j = new Jugador();
        j.setIdGoogle("user_2");
        j.setTag("OtroTag");
        when(jugadorRepository.findById("user_2")).thenReturn(Optional.of(j));
        when(profanityFilterService.filter("Panda")).thenReturn(new ProfanityFilterService.FilterResult("Panda", false));

        assertDoesNotThrow(() -> jugadorService.actualizarPerfil(dtoPanda, "user_2"));
        assertEquals("Panda", j.getTag());
        
        verify(jugadorRepository).existsByTagAndActivoTrue("Panda");
    }

    @Test
    void testConsultarLogrosYMedallas_Rf8() {
        String id = "google_123";
        lenient().when(logroRepository.findByActivoTrue()).thenReturn(List.of()); 
        lenient().when(jugadorLogroRepository.findById_IdJugador(id)).thenReturn(List.of());

        var logros = jugadorService.getLogros(id);

        assertNotNull(logros);
    }

    @Test
    void testCerrarSesion_Rf3() {
        var request = mock(jakarta.servlet.http.HttpServletRequest.class);
        var response = mock(jakarta.servlet.http.HttpServletResponse.class);
        com.secretpanda.codenames.controller.AuthController authController = new com.secretpanda.codenames.controller.AuthController(authService);
        
        authController.logout(request, response);
        
        verify(response).addHeader(eq("Set-Cookie"), contains("token_sesion=;"));
    }
}
