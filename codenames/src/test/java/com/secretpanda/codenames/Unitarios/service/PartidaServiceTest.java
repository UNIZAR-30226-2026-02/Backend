package com.secretpanda.codenames.service;

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
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import com.secretpanda.codenames.dto.partida.CrearPartidaDTO;
import com.secretpanda.codenames.dto.partida.LobbyStatusDTO;
import com.secretpanda.codenames.exception.GameLogicException;
import com.secretpanda.codenames.model.Jugador;
import com.secretpanda.codenames.model.JugadorPartida;
import com.secretpanda.codenames.model.Partida;
import com.secretpanda.codenames.model.Tema;
import com.secretpanda.codenames.repository.InventarioTemaRepository;
import com.secretpanda.codenames.repository.JugadorPartidaRepository;
import com.secretpanda.codenames.repository.JugadorRepository;
import com.secretpanda.codenames.repository.PartidaRepository;
import com.secretpanda.codenames.repository.TableroCartaRepository;
import com.secretpanda.codenames.repository.TemaRepository;
import com.secretpanda.codenames.util.CodigoPartidaGenerator;

/**
 * Suite de pruebas unitarias para PartidaService.
 * Valida la creación de partidas, la lógica de unión (join) y el abandono (con penalizaciones).
 */
@ExtendWith(MockitoExtension.class)
class PartidaServiceTest {

    @Mock private PartidaRepository partidaRepository;
    @Mock private JugadorRepository jugadorRepository;
    @Mock private TemaRepository temaRepository;
    @Mock private JugadorPartidaRepository jugadorPartidaRepository;
    @Mock private InventarioTemaRepository inventarioTemaRepository;
    @Mock private TableroCartaRepository tableroCartaRepository;
    @Mock private CodigoPartidaGenerator codigoGenerator;
    @Mock private SimpMessagingTemplate messagingTemplate;
    @Mock private JugadorService jugadorService;
    @Mock private JuegoService juegoService;

    @InjectMocks private PartidaService partidaService;

    @BeforeEach
    void setUp() {
        // Configuramos la propiedad inyectada por @Value
        ReflectionTestUtils.setField(partidaService, "balasPenalizacionAbandono", 5);
    }

    /**
     * Prueba: shouldThrowExceptionWhenCreatingPartidaWithActiveSession
     * Verifica que no se permita crear una partida si el jugador ya tiene una sesión activa.
     */
    @Test
    void shouldThrowExceptionWhenCreatingPartidaWithActiveSession() {
        // 1. Preparación (Arrange)
        // Simulamos que el jugador ya está en una partida que no ha sido abandonada
        when(jugadorPartidaRepository.existsByJugador_IdGoogleAndPartida_EstadoInAndAbandonoFalse(
                eq("id_google"), anyList())).thenReturn(true);

        CrearPartidaDTO dto = new CrearPartidaDTO();
        
        // 2 & 3. Ejecución y Verificación (Act & Assert)
        // Se espera que la creación falle
        assertThrows(GameLogicException.class, () -> {
            partidaService.crearPartida(dto, "id_google");
        });
    }

    /**
     * Prueba: shouldThrowExceptionWhenCreatingPartidaWithoutRequiredTema
     * Verifica que no se permita crear una partida si el jugador no posee el tema seleccionado en su inventario.
     */
    @Test
    void shouldThrowExceptionWhenCreatingPartidaWithoutRequiredTema() {
        // 1. Preparación (Arrange)
        when(jugadorPartidaRepository.existsByJugador_IdGoogleAndPartida_EstadoInAndAbandonoFalse(
                eq("id_google"), anyList())).thenReturn(false);

        // Configuramos el tema que se intentará usar
        Tema tema = new Tema();
        tema.setIdTema(1);
        when(temaRepository.findById(1)).thenReturn(Optional.of(tema));

        // Configuramos el creador
        Jugador creador = new Jugador();
        creador.setIdGoogle("id_google");
        when(jugadorRepository.findById("id_google")).thenReturn(Optional.of(creador));

        // Simulamos explícitamente que el jugador NO posee el tema
        when(inventarioTemaRepository.existsById_IdJugadorAndId_IdTema("id_google", 1)).thenReturn(false);

        CrearPartidaDTO dto = new CrearPartidaDTO();
        dto.setIdTema(1);

        // 2 & 3. Ejecución y Verificación (Act & Assert)
        assertThrows(GameLogicException.class, () -> {
            partidaService.crearPartida(dto, "id_google");
        });
    }

    /**
     * Prueba: shouldCreatePartidaSuccessfullyAndAssignInitialRole
     * Verifica la creación exitosa de una partida, asegurándose de que al creador se le asigne
     * el equipo Rojo por defecto y, al ser el primero, asuma el rol de Agente (según la lógica de la app,
     * el rol líder debe asignarse internamente o luego).
     */
    @Test
    void shouldCreatePartidaSuccessfullyAndAssignInitialRole() {
        // 1. Preparación (Arrange)
        when(jugadorPartidaRepository.existsByJugador_IdGoogleAndPartida_EstadoInAndAbandonoFalse(
                eq("id_google"), anyList())).thenReturn(false);

        Tema tema = new Tema();
        tema.setIdTema(1);
        tema.setNombre("Hacker");
        when(temaRepository.findById(1)).thenReturn(Optional.of(tema));

        Jugador creador = new Jugador();
        creador.setIdGoogle("id_google");
        creador.setTag("CreadorTest");
        when(jugadorRepository.findById("id_google")).thenReturn(Optional.of(creador));

        when(inventarioTemaRepository.existsById_IdJugadorAndId_IdTema("id_google", 1)).thenReturn(true);
        when(codigoGenerator.generarCodigo()).thenReturn("ABCDEF");
        when(partidaRepository.existsByCodigoPartida("ABCDEF")).thenReturn(false);
        
        Partida partidaGuardada = new Partida();
        partidaGuardada.setIdPartida(10);
        partidaGuardada.setCodigoPartida("ABCDEF");
        partidaGuardada.setEstado(Partida.EstadoPartida.esperando);
        partidaGuardada.setTema(tema);
        partidaGuardada.setCreador(creador);

        when(partidaRepository.save(any(Partida.class))).thenReturn(partidaGuardada);

        CrearPartidaDTO dto = new CrearPartidaDTO();
        dto.setIdTema(1);
        dto.setMaxJugadores(4);
        dto.setEsPublica(false);

        // 2. Ejecución (Act)
        LobbyStatusDTO result = partidaService.crearPartida(dto, "id_google");

        // 3. Verificación (Assert)
        assertNotNull(result);
        assertEquals("ABCDEF", result.getCodigoPartida());
        assertEquals("esperando", result.getEstado());
        
        // Verificamos que se guarda la relación JugadorPartida asignando al creador el equipo rojo.
        // El rol no se asigna explícitamente en el service al crear, por lo que será null en el test.
        verify(jugadorPartidaRepository).save(argThat(jp -> 
            jp.getJugador().getIdGoogle().equals("id_google") &&
            jp.getEquipo() == JugadorPartida.Equipo.rojo
        ));
    }

    /**
     * Prueba: shouldApplyPenaltyAndEndGameIfLeaderAbandonsActiveMatch
     * Verifica que si el líder abandona una partida EN CURSO:
     * 1. Se le penaliza restándole balas.
     * 2. Se guarda su estado de abandono.
     * 3. Su equipo pierde automáticamente.
     */
    @Test
    void shouldApplyPenaltyAndEndGameIfLeaderAbandonsActiveMatch() {
        // 1. Preparación (Arrange)
        Integer idPartida = 10;
        String idGoogle = "lider_rojo";

        // Simulamos la partida en curso
        Partida partida = new Partida();
        partida.setIdPartida(idPartida);
        partida.setEstado(Partida.EstadoPartida.en_curso);

        Jugador jugador = new Jugador();
        jugador.setIdGoogle(idGoogle);

        // Simulamos que quien abandona es el líder del equipo rojo
        JugadorPartida jpLider = new JugadorPartida();
        jpLider.setJugador(jugador);
        jpLider.setPartida(partida);
        jpLider.setEquipo(JugadorPartida.Equipo.rojo);
        jpLider.setRol(JugadorPartida.Rol.lider);

        // Asignamos la lista al modelo de la partida para cuando finalice
        partida.setJugadores(List.of(jpLider));

        when(partidaRepository.findByIdForUpdate(idPartida)).thenReturn(Optional.of(partida));
        when(jugadorPartidaRepository.findByJugador_IdGoogleAndPartida_IdPartida(idGoogle, idPartida))
                .thenReturn(Optional.of(jpLider));
        when(jugadorPartidaRepository.findByPartida_IdPartida(idPartida)).thenReturn(List.of(jpLider));

        // 2. Ejecución (Act)
        partidaService.abandonar(idPartida, idGoogle);

        // 3. Verificación (Assert)
        // Verificamos que se modificaron las balas restando 5 como penalización
        verify(jugadorService).modificarBalas(idGoogle, -5);

        // Verificamos que se guardó el abandono
        assertTrue(jpLider.isAbandono());
        verify(jugadorPartidaRepository).save(jpLider);

        // Verificamos que el equipo rojo perdió automáticamente porque el líder rojo abandonó (rojoGana = false)
        assertEquals(Partida.EstadoPartida.finalizada, partida.getEstado());
        assertFalse(partida.getRojoGana());
        verify(partidaRepository).save(partida);

        // Verificamos que se notificó el cambio de estado de la partida
        verify(juegoService).broadcastEstado(idPartida);
    }
}