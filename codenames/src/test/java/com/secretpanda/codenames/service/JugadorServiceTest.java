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
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import com.secretpanda.codenames.dto.jugador.ActualizarPerfilDTO;
import com.secretpanda.codenames.dto.jugador.JugadorDTO;
import com.secretpanda.codenames.model.Jugador;
import com.secretpanda.codenames.repository.InventarioPersonalizacionRepository;
import com.secretpanda.codenames.repository.InventarioTemaRepository;
import com.secretpanda.codenames.repository.JugadorLogroRepository;
import com.secretpanda.codenames.repository.JugadorPartidaRepository;
import com.secretpanda.codenames.repository.JugadorRepository;
import com.secretpanda.codenames.repository.LogroRepository;
import com.secretpanda.codenames.util.EstadisticasCalculator;

@ExtendWith(MockitoExtension.class)
public class JugadorServiceTest {

    @Mock
    private JugadorRepository jugadorRepository;
    @Mock
    private InventarioTemaRepository inventarioTemaRepository;
    @Mock
    private InventarioPersonalizacionRepository inventarioPersonalizacionRepository;
    @Mock
    private JugadorPartidaRepository jugadorPartidaRepository;
    @Mock
    private JugadorLogroRepository jugadorLogroRepository;
    @Mock
    private EstadisticasCalculator calculator;
    @Mock
    private LogroRepository logroRepository;
    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private JugadorService jugadorService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jugadorService, "balasGanador", 20);
        ReflectionTestUtils.setField(jugadorService, "balasDerrota", 10);
    }

    @Test
    void testProcesarFinPartida_Ganador() {
        String idGoogle = "user123";
        Jugador jugador = new Jugador();
        jugador.setIdGoogle(idGoogle);
        jugador.setBalas(100);
        jugador.setPartidasJugadas(5);
        jugador.setVictorias(2);
        jugador.setNumAciertos(10);
        jugador.setNumFallos(5);

        when(jugadorRepository.findByIdForUpdate(idGoogle)).thenReturn(Optional.of(jugador));
        when(jugadorRepository.findById(idGoogle)).thenReturn(Optional.of(jugador));

        jugadorService.procesarFinPartida(idGoogle, true, 5, 2);

        assertEquals(120, jugador.getBalas());
        assertEquals(6, jugador.getPartidasJugadas());
        assertEquals(3, jugador.getVictorias());
        assertEquals(15, jugador.getNumAciertos());
        assertEquals(7, jugador.getNumFallos());
        verify(jugadorRepository).save(jugador);
        verify(messagingTemplate).convertAndSendToUser(eq(idGoogle), eq("/queue/balas"), eq(120));
    }

    @Test
    void testProcesarFinPartida_Perdedor() {
        String idGoogle = "user123";
        Jugador jugador = new Jugador();
        jugador.setIdGoogle(idGoogle);
        jugador.setBalas(100);

        when(jugadorRepository.findByIdForUpdate(idGoogle)).thenReturn(Optional.of(jugador));
        when(jugadorRepository.findById(idGoogle)).thenReturn(Optional.of(jugador));

        jugadorService.procesarFinPartida(idGoogle, false, 2, 5);

        assertEquals(110, jugador.getBalas());
        verify(jugadorRepository).save(jugador);
    }

    @Test
    void testModificarBalas_NoNegativos() {
        String idGoogle = "user123";
        Jugador jugador = new Jugador();
        jugador.setIdGoogle(idGoogle);
        jugador.setBalas(10);

        when(jugadorRepository.findByIdForUpdate(idGoogle)).thenReturn(Optional.of(jugador));

        jugadorService.modificarBalas(idGoogle, -20);

        assertEquals(0, jugador.getBalas());
        verify(jugadorRepository).save(jugador);
    }

    @Test
    void testActualizarPerfil_VerificaGuardado() {
        String idGoogle = "user123";
        Jugador jugador = new Jugador();
        jugador.setIdGoogle(idGoogle);
        jugador.setTag("TagOriginal");

        ActualizarPerfilDTO dto = new ActualizarPerfilDTO();
        dto.setTag("NuevoTag");
        dto.setFotoPerfil("nueva_foto.png");

        when(jugadorRepository.findById(idGoogle)).thenReturn(Optional.of(jugador));
        when(jugadorRepository.existsByTagAndActivoTrue("NuevoTag")).thenReturn(false);

        JugadorDTO result = jugadorService.actualizarPerfil(dto, idGoogle);

        assertNotNull(result);
        assertEquals("NuevoTag", jugador.getTag());
        assertEquals("nueva_foto.png", jugador.getFotoPerfil());
        verify(jugadorRepository).save(jugador);
    }
}
