package com.secretpanda.codenames.Unitarios.service;

import com.secretpanda.codenames.service.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.List;
import java.time.LocalDateTime;

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
import com.secretpanda.codenames.model.JugadorLogro;
import com.secretpanda.codenames.model.Logro;
import com.secretpanda.codenames.repository.InventarioPersonalizacionRepository;
import com.secretpanda.codenames.repository.InventarioTemaRepository;
import com.secretpanda.codenames.repository.JugadorLogroRepository;
import com.secretpanda.codenames.repository.JugadorPartidaRepository;
import com.secretpanda.codenames.repository.JugadorRepository;
import com.secretpanda.codenames.repository.LogroRepository;
import com.secretpanda.codenames.util.EstadisticasCalculator;

/**
 * Suite de pruebas unitarias para JugadorService.
 * Valida actualizaciones de perfil, cálculos de final de partida (balas)
 * y la lógica de progreso y desbloqueo de logros.
 */
@ExtendWith(MockitoExtension.class)
public class JugadorServiceTest {

    @Mock private JugadorRepository jugadorRepository;
    @Mock private InventarioTemaRepository inventarioTemaRepository;
    @Mock private InventarioPersonalizacionRepository inventarioPersonalizacionRepository;
    @Mock private JugadorPartidaRepository jugadorPartidaRepository;
    @Mock private JugadorLogroRepository jugadorLogroRepository;
    @Mock private EstadisticasCalculator calculator;
    @Mock private LogroRepository logroRepository;
    @Mock private SimpMessagingTemplate messagingTemplate;

    @InjectMocks private JugadorService jugadorService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jugadorService, "balasGanador", 20);
        ReflectionTestUtils.setField(jugadorService, "balasDerrota", 10);
    }

    /**
     * Prueba: shouldProcessEndGameCorrectlyForWinner
     * Verifica que cuando se procesa el fin de partida para un ganador,
     * se le otorguen las balas correspondientes (20), y se sumen sus estadísticas de aciertos/fallos.
     */
    @Test
    void shouldProcessEndGameCorrectlyForWinner() {
        // 1. Preparación (Arrange)
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

        // 2. Ejecución (Act)
        // Procesamos el fin de partida donde el jugador ha ganado, acertado 5 cartas y fallado 2
        jugadorService.procesarFinPartida(idGoogle, true, 5, 2);

        // 3. Verificación (Assert)
        // Verificamos balas: 100 + 20
        assertEquals(120, jugador.getBalas());
        // Verificamos estadísticas actualizadas
        assertEquals(6, jugador.getPartidasJugadas());
        assertEquals(3, jugador.getVictorias());
        assertEquals(15, jugador.getNumAciertos());
        assertEquals(7, jugador.getNumFallos());
        
        verify(jugadorRepository).save(jugador);
    }

    /**
     * Prueba: shouldProcessEndGameCorrectlyForLoser
     * Verifica que cuando un jugador pierde, se le otorgue la recompensa base de derrota (10 balas).
     */
    @Test
    void shouldProcessEndGameCorrectlyForLoser() {
        // 1. Preparación (Arrange)
        String idGoogle = "user123";
        Jugador jugador = new Jugador();
        jugador.setIdGoogle(idGoogle);
        jugador.setBalas(100);

        when(jugadorRepository.findByIdForUpdate(idGoogle)).thenReturn(Optional.of(jugador));
        when(jugadorRepository.findById(idGoogle)).thenReturn(Optional.of(jugador));

        // 2. Ejecución (Act)
        jugadorService.procesarFinPartida(idGoogle, false, 2, 5);

        // 3. Verificación (Assert)
        // 100 + 10 por derrota
        assertEquals(110, jugador.getBalas());
        verify(jugadorRepository).save(jugador);
    }

    /**
     * Prueba: shouldGuaranteeMinimumZeroBalasWhenModifying
     * Verifica que el inventario de balas nunca baje de cero, incluso ante deducciones fuertes.
     */
    @Test
    void shouldGuaranteeMinimumZeroBalasWhenModifying() {
        // 1. Preparación (Arrange)
        String idGoogle = "user123";
        Jugador jugador = new Jugador();
        jugador.setIdGoogle(idGoogle);
        jugador.setBalas(10);

        when(jugadorRepository.findByIdForUpdate(idGoogle)).thenReturn(Optional.of(jugador));

        // 2. Ejecución (Act)
        // Restamos 20 balas a un balance de 10
        jugadorService.modificarBalas(idGoogle, -20);

        // 3. Verificación (Assert)
        // No deben existir balas negativas
        assertEquals(0, jugador.getBalas());
        verify(jugadorRepository).save(jugador);
    }

    /**
     * Prueba: shouldUnlockAchievementAndRewardBulletsWhenRequirementMet
     * Verifica que al actualizar el progreso de logros, si se cumple el requisito de uno,
     * el logro se marque como completado y se otorguen las balas correspondientes.
     */
    @Test
    void shouldUnlockAchievementAndRewardBulletsWhenRequirementMet() {
        // 1. Preparación (Arrange)
        String idGoogle = "user123";
        Jugador jugador = new Jugador();
        jugador.setIdGoogle(idGoogle);
        jugador.setPartidasJugadas(10); // Tiene 10 partidas jugadas
        jugador.setBalas(100);

        Logro logroPartidas = new Logro();
        logroPartidas.setEstadisticaClave("partidas_jugadas");
        logroPartidas.setValorObjetivo(10); // Objetivo: 10 partidas
        logroPartidas.setTipo(Logro.TipoLogro.logro);
        logroPartidas.setNombre("Veterano");

        JugadorLogro progreso = new JugadorLogro();
        progreso.setJugador(jugador);
        progreso.setLogro(logroPartidas);
        progreso.setProgresoActual(9);
        progreso.setCompletado(false);

        when(jugadorRepository.findById(idGoogle)).thenReturn(Optional.of(jugador));
        when(jugadorRepository.findByIdForUpdate(idGoogle)).thenReturn(Optional.of(jugador)); // Por el modificarBalas
        // Simulamos que este logro estaba pendiente
        when(jugadorLogroRepository.findById_IdJugadorAndCompletadoFalse(idGoogle))
                .thenReturn(List.of(progreso));

        // 2. Ejecución (Act)
        jugadorService.actualizarProgresoLogros(idGoogle);

        // 3. Verificación (Assert)
        // El logro debe marcarse completado
        assertTrue(progreso.isCompletado());
        assertNotNull(progreso.getFechaDesbloqueo());
        assertEquals(10, progreso.getProgresoActual());

        // Verificamos que se le otorgaron las 50 balas del logro (100 iniciales + 50 = 150)
        assertEquals(150, jugador.getBalas());

        // Verificamos que se guardaron los cambios del progreso
        verify(jugadorLogroRepository).saveAll(anyList());
    }
}