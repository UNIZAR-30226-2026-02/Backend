package com.secretpanda.codenames.desarrollo;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.List;
import java.util.ArrayList;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import com.secretpanda.codenames.model.Jugador;
import com.secretpanda.codenames.model.JugadorPartida;
import com.secretpanda.codenames.model.Partida;
import com.secretpanda.codenames.model.TableroCarta;
import com.secretpanda.codenames.model.Turno;
import com.secretpanda.codenames.model.VotoCarta;
import com.secretpanda.codenames.repository.PartidaRepository;
import com.secretpanda.codenames.repository.TableroCartaRepository;
import com.secretpanda.codenames.repository.TurnoRepository;
import com.secretpanda.codenames.repository.VotoCartaRepository;
import com.secretpanda.codenames.repository.JugadorPartidaRepository;
import com.secretpanda.codenames.service.JuegoService;
import com.secretpanda.codenames.service.TemporizadorService;
import com.secretpanda.codenames.service.LeaderboardService;
import com.secretpanda.codenames.service.JugadorService;

import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;

/**
 * Pruebas de Desarrollo: Reconexión y Timeouts (RF-34, RF-35)
 */
@ExtendWith(MockitoExtension.class)
public class ReconexionTimeoutDesarrolloTest {

    @Mock private PartidaRepository partidaRepository;
    @Mock private TableroCartaRepository tableroCartaRepository;
    @Mock private TurnoRepository turnoRepository;
    @Mock private VotoCartaRepository votoCartaRepository;
    @Mock private JugadorPartidaRepository jugadorPartidaRepository;
    @Mock private TemporizadorService temporizadorService;
    @Mock private LeaderboardService leaderboardService;
    @Mock private JugadorService jugadorService;
    @Mock private SimpMessagingTemplate messagingTemplate;
    @Mock private ApplicationContext applicationContext;

    @InjectMocks private JuegoService juegoService;

    @BeforeEach
    void setUp() {
        TransactionSynchronizationManager.initSynchronization();
    }

    @AfterEach
    void tearDown() {
        TransactionSynchronizationManager.clearSynchronization();
    }

    private JugadorPartida crearJugador(String id, JugadorPartida.Equipo equipo) {
        JugadorPartida jp = new JugadorPartida();
        jp.setEquipo(equipo);
        jp.setJugador(new Jugador());
        jp.getJugador().setIdGoogle(id);
        jp.getJugador().setTag("Tag_" + id);
        return jp;
    }

    @Test
    void testTimeoutConVotacionPorMayoria_Rf34() {
        Integer idPartida = 1;
        Partida p = new Partida();
        p.setIdPartida(idPartida);
        p.setEstado(Partida.EstadoPartida.en_curso);
        p.setTiempoEspera(60);

        Turno t = new Turno();
        t.setIdTurno(10);
        t.setPalabraPista("Pista");
        t.setPistaNumero(1);
        JugadorPartida jpLider = crearJugador("l1", JugadorPartida.Equipo.rojo);
        t.setJugadorPartida(jpLider);

        TableroCarta carta1 = new TableroCarta(); carta1.setIdCartaTablero(101); carta1.setTipo(TableroCarta.TipoCarta.rojo);
        TableroCarta carta2 = new TableroCarta(); carta2.setIdCartaTablero(102); carta2.setTipo(TableroCarta.TipoCarta.azul);

        List<VotoCarta> votos = new ArrayList<>();
        for(int i=0; i<2; i++) {
            VotoCarta v = new VotoCarta(); 
            v.setCartaTablero(carta1); 
            v.setJugadorPartida(crearJugador("a"+i, JugadorPartida.Equipo.rojo));
            votos.add(v);
        }
        VotoCarta v2 = new VotoCarta(); 
        v2.setCartaTablero(carta2); 
        v2.setJugadorPartida(crearJugador("a3", JugadorPartida.Equipo.rojo));
        votos.add(v2);

        when(partidaRepository.findByIdForUpdate(idPartida)).thenReturn(Optional.of(p));
        lenient().when(partidaRepository.findById(idPartida)).thenReturn(Optional.of(p));
        when(turnoRepository.findFirstByPartida_IdPartidaOrderByNumTurnoDesc(idPartida)).thenReturn(Optional.of(t));
        when(votoCartaRepository.findByTurno_IdTurnoAndCartaReveladaIsNull(10)).thenReturn(votos);
        when(tableroCartaRepository.findById(101)).thenReturn(Optional.of(carta1));
        
        juegoService.forzarFinTurno(idPartida);

        verify(tableroCartaRepository, atLeastOnce()).save(argThat(c -> c.getIdCartaTablero() == 101 && c.getEstado() == TableroCarta.EstadoCarta.revelada));
    }

    @Test
    void testTimeoutSinVotos_Rf34() {
        Integer idPartida = 1;
        Partida p = new Partida();
        p.setIdPartida(idPartida);
        p.setEstado(Partida.EstadoPartida.en_curso);

        Turno t = new Turno();
        t.setIdTurno(10);
        t.setPalabraPista("Pista");
        t.setPistaNumero(1);
        JugadorPartida jpLider = crearJugador("l1", JugadorPartida.Equipo.rojo);
        jpLider.setRol(JugadorPartida.Rol.lider);
        t.setJugadorPartida(jpLider);

        JugadorPartida jpRival = crearJugador("rival", JugadorPartida.Equipo.azul);
        jpRival.setRol(JugadorPartida.Rol.lider);

        when(partidaRepository.findByIdForUpdate(idPartida)).thenReturn(Optional.of(p));
        lenient().when(partidaRepository.findById(idPartida)).thenReturn(Optional.of(p));
        when(turnoRepository.findFirstByPartida_IdPartidaOrderByNumTurnoDesc(idPartida)).thenReturn(Optional.of(t));
        when(votoCartaRepository.findByTurno_IdTurnoAndCartaReveladaIsNull(10)).thenReturn(List.of());
        when(jugadorPartidaRepository.findByPartida_IdPartida(idPartida)).thenReturn(List.of(jpLider, jpRival));
        when(turnoRepository.findByPartida_IdPartidaOrderByNumTurnoAsc(idPartida)).thenReturn(List.of(t));
        lenient().when(applicationContext.getBean(JuegoService.class)).thenReturn(juegoService);

        juegoService.forzarFinTurno(idPartida);

        verify(turnoRepository).save(argThat(nuevoTurno -> 
            nuevoTurno.getJugadorPartida().getEquipo() == JugadorPartida.Equipo.azul
        ));
    }

    @Test
    void testTimeoutEmpateVotos_NoRevelaNada_Rf34() {
        Integer idPartida = 1;
        Partida p = new Partida();
        p.setIdPartida(idPartida);
        p.setEstado(Partida.EstadoPartida.en_curso);

        Turno t = new Turno();
        t.setIdTurno(10);
        t.setPalabraPista("Pista");
        t.setPistaNumero(1);
        JugadorPartida jpLider = crearJugador("l1", JugadorPartida.Equipo.rojo);
        t.setJugadorPartida(jpLider);

        TableroCarta carta1 = new TableroCarta(); carta1.setIdCartaTablero(101);
        TableroCarta carta2 = new TableroCarta(); carta2.setIdCartaTablero(102);

        List<VotoCarta> votos = new ArrayList<>();
        VotoCarta v1 = new VotoCarta(); 
        v1.setCartaTablero(carta1); 
        v1.setJugadorPartida(crearJugador("a1", JugadorPartida.Equipo.rojo));
        votos.add(v1);
        
        VotoCarta v2 = new VotoCarta(); 
        v2.setCartaTablero(carta2); 
        v2.setJugadorPartida(crearJugador("a2", JugadorPartida.Equipo.rojo));
        votos.add(v2);

        JugadorPartida jpRival = crearJugador("rival", JugadorPartida.Equipo.azul);
        jpRival.setRol(JugadorPartida.Rol.lider);

        when(partidaRepository.findByIdForUpdate(idPartida)).thenReturn(Optional.of(p));
        lenient().when(partidaRepository.findById(idPartida)).thenReturn(Optional.of(p));
        when(turnoRepository.findFirstByPartida_IdPartidaOrderByNumTurnoDesc(idPartida)).thenReturn(Optional.of(t));
        when(votoCartaRepository.findByTurno_IdTurnoAndCartaReveladaIsNull(10)).thenReturn(votos);
        when(jugadorPartidaRepository.findByPartida_IdPartida(idPartida)).thenReturn(List.of(jpLider, jpRival));
        when(turnoRepository.findByPartida_IdPartidaOrderByNumTurnoAsc(idPartida)).thenReturn(List.of(t));
        lenient().when(applicationContext.getBean(JuegoService.class)).thenReturn(juegoService);

        juegoService.forzarFinTurno(idPartida);

        verify(tableroCartaRepository, never()).save(any());
        verify(turnoRepository).save(argThat(nuevoTurno -> nuevoTurno.getNumTurno() == 2));
    }
}
