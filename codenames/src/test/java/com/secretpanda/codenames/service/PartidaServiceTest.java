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
        ReflectionTestUtils.setField(partidaService, "balasPenalizacionAbandono", 10);
    }

    @Test
    void testCrearPartida_JugadorConPartidaActiva_LanzaExcepcion() {
        when(jugadorPartidaRepository.existsByJugador_IdGoogleAndPartida_EstadoInAndAbandonoFalse(
                eq("id_google"), anyList())).thenReturn(true);

        CrearPartidaDTO dto = new CrearPartidaDTO();
        assertThrows(GameLogicException.class, () -> {
            partidaService.crearPartida(dto, "id_google");
        });
    }

    @Test
    void testCrearPartida_SinTemaEnInventario_LanzaExcepcion() {
        when(jugadorPartidaRepository.existsByJugador_IdGoogleAndPartida_EstadoInAndAbandonoFalse(
                eq("id_google"), anyList())).thenReturn(false);

        Tema tema = new Tema();
        tema.setIdTema(1);
        when(temaRepository.findById(1)).thenReturn(Optional.of(tema));

        Jugador creador = new Jugador();
        creador.setIdGoogle("id_google");
        when(jugadorRepository.findById("id_google")).thenReturn(Optional.of(creador));

        when(inventarioTemaRepository.existsById_IdJugadorAndId_IdTema("id_google", 1)).thenReturn(false);

        CrearPartidaDTO dto = new CrearPartidaDTO();
        dto.setIdTema(1);

        assertThrows(GameLogicException.class, () -> {
            partidaService.crearPartida(dto, "id_google");
        });
    }

    @Test
    void testCrearPartida_Exito_GeneraCodigoYAsignaRojoAgente() {
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

        LobbyStatusDTO result = partidaService.crearPartida(dto, "id_google");

        assertNotNull(result);
        assertEquals("ABCDEF", result.getCodigoPartida());
        assertEquals("esperando", result.getEstado());
        verify(jugadorPartidaRepository).save(argThat(jp -> 
            jp.getJugador().getIdGoogle().equals("id_google") &&
            jp.getEquipo() == JugadorPartida.Equipo.rojo &&
            jp.getRol() == JugadorPartida.Rol.agente
        ));
    }
}
