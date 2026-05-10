package com.secretpanda.codenames.Unitarios.service;

import com.secretpanda.codenames.service.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.secretpanda.codenames.model.Jugador;
import com.secretpanda.codenames.model.JugadorPartida;
import com.secretpanda.codenames.model.Partida;
import com.secretpanda.codenames.model.Turno;
import com.secretpanda.codenames.model.TableroCarta;
import com.secretpanda.codenames.model.PalabraTema;
import com.secretpanda.codenames.model.Tema;
import com.secretpanda.codenames.repository.PartidaRepository;
import com.secretpanda.codenames.repository.TableroCartaRepository;
import com.secretpanda.codenames.repository.TurnoRepository;
import com.secretpanda.codenames.repository.VotoCartaRepository;
import com.secretpanda.codenames.repository.JugadorPartidaRepository;
import com.secretpanda.codenames.repository.PalabraTemaRepository;
import com.secretpanda.codenames.repository.JugadorRepository;

/**
 * Suite de pruebas unitarias para JuegoService.
 * Valida la lógica central del juego: turnos, votaciones, inicialización de tablero.
 */
@ExtendWith(MockitoExtension.class)
class JuegoServiceTest {

    @Mock private PartidaRepository partidaRepository;
    @Mock private TableroCartaRepository tableroCartaRepository;
    @Mock private TurnoRepository turnoRepository;
    @Mock private VotoCartaRepository votoCartaRepository;
    @Mock private JugadorPartidaRepository jugadorPartidaRepository;
    @Mock private PalabraTemaRepository palabraTemaRepository;
    @Mock private JugadorRepository jugadorRepository;
    @Mock private SimpMessagingTemplate messagingTemplate;
    @Mock private LeaderboardService leaderboardService;
    @Mock private JugadorService jugadorService;
    @Mock private TemporizadorService temporizadorService;
    @Mock private ApplicationContext applicationContext;

    @InjectMocks private JuegoService juegoService;

    /**
     * Prueba: shouldUpdateCurrentTurnAndNotifyWhenGivingClue
     * Verifica que cuando el jefe da una pista, el turno se actualice con la palabra
     * y se guarde en la BD.
     */
    @Test
    void shouldUpdateCurrentTurnAndNotifyWhenGivingClue() {
        // 1. Preparación (Arrange)
        Integer idPartida = 1;
        String idGoogle = "user1";
        
        Partida p = new Partida();
        p.setIdPartida(idPartida);
        p.setEstado(Partida.EstadoPartida.en_curso);
        p.setTiempoEspera(30);

        JugadorPartida jp = new JugadorPartida();
        jp.setRol(JugadorPartida.Rol.lider);
        jp.setEquipo(JugadorPartida.Equipo.rojo);

        Turno turnoExistente = new Turno();
        turnoExistente.setNumTurno(1);
        turnoExistente.setJugadorPartida(jp);

        when(partidaRepository.findByIdForUpdate(idPartida)).thenReturn(Optional.of(p));
        when(partidaRepository.findById(idPartida)).thenReturn(Optional.of(p));
        when(jugadorPartidaRepository.findByJugador_IdGoogleAndPartida_IdPartida(idGoogle, idPartida))
                .thenReturn(Optional.of(jp));
        when(turnoRepository.findFirstByPartida_IdPartidaOrderByNumTurnoDesc(idPartida))
                .thenReturn(Optional.of(turnoExistente));
        
        // 2. Ejecución (Act)
        juegoService.darPista(idPartida, "Espía", 2, idGoogle);

        // 3. Verificación (Assert)
        // Verificamos que el turno recibió la pista
        assertEquals("Espía", turnoExistente.getPalabraPista());
        assertEquals(2, turnoExistente.getPistaNumero());
        // Verificamos que se persisten los cambios del turno
        verify(turnoRepository).save(turnoExistente);
        // Verificamos que se actualizó la fecha de inicio de turno en la partida
        verify(partidaRepository).save(p);
    }

    /**
     * Prueba: shouldInitializeBoardCorrectlyAndDistributeCards
     * Verifica que la inicialización del tablero cree exactamente 20 cartas y las asigne
     * al tablero, requiriendo 20 palabras disponibles en el repositorio.
     */
    @Test
    void shouldInitializeBoardCorrectlyAndDistributeCards() {
        // 1. Preparación (Arrange)
        Partida partida = new Partida();
        partida.setIdPartida(10);
        partida.setEstado(Partida.EstadoPartida.en_curso);
        Tema tema = new Tema();
        tema.setIdTema(1);
        partida.setTema(tema);

        when(partidaRepository.findById(10)).thenReturn(Optional.of(partida));

        List<JugadorPartida> jugadores = new ArrayList<>();
        JugadorPartida liderRojo = new JugadorPartida();
        liderRojo.setEquipo(JugadorPartida.Equipo.rojo);
        liderRojo.setRol(JugadorPartida.Rol.lider);
        JugadorPartida liderAzul = new JugadorPartida();
        liderAzul.setEquipo(JugadorPartida.Equipo.azul);
        liderAzul.setRol(JugadorPartida.Rol.lider);
        jugadores.add(liderRojo);
        jugadores.add(liderAzul);

        // Preparamos 20 palabras de test
        List<PalabraTema> palabras = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            PalabraTema pt = new PalabraTema();
            pt.setValor("Palabra" + i);
            palabras.add(pt);
        }
        
        when(palabraTemaRepository.findByTema_IdTemaAndActivoTrue(1)).thenReturn(palabras);
        
        // 2. Ejecución (Act)
        juegoService.inicializarPartida(partida, jugadores);

        // 3. Verificación (Assert)
        // Verificamos que se llamaron a guardar las 20 cartas generadas (4x5)
        verify(tableroCartaRepository).saveAll(argThat(lista -> {
            List<TableroCarta> cartas = (List<TableroCarta>) lista;
            return cartas.size() == 20 && 
                   cartas.stream().filter(c -> c.getTipo() == TableroCarta.TipoCarta.asesino).count() == 1;
        }));
        
        // Verificamos que se crea el turno inicial
        verify(turnoRepository).save(any(Turno.class));
        
        // Verificamos que se activa el temporizador
        verify(temporizadorService).iniciarTemporizador(eq(10), anyInt(), any());
    }
}