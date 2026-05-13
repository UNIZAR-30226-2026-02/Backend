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
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.secretpanda.codenames.model.Jugador;
import com.secretpanda.codenames.model.JugadorPartida;
import com.secretpanda.codenames.model.Partida;
import com.secretpanda.codenames.model.TableroCarta;
import com.secretpanda.codenames.model.Tema;
import com.secretpanda.codenames.model.PalabraTema;
import com.secretpanda.codenames.model.Turno;
import com.secretpanda.codenames.model.VotoCarta;
import com.secretpanda.codenames.repository.PartidaRepository;
import com.secretpanda.codenames.repository.TableroCartaRepository;
import com.secretpanda.codenames.repository.PalabraTemaRepository;
import com.secretpanda.codenames.repository.TurnoRepository;
import com.secretpanda.codenames.repository.VotoCartaRepository;
import com.secretpanda.codenames.repository.JugadorPartidaRepository;
import com.secretpanda.codenames.repository.JugadorRepository;
import com.secretpanda.codenames.service.JuegoService;
import com.secretpanda.codenames.service.TemporizadorService;
import com.secretpanda.codenames.service.LeaderboardService;
import com.secretpanda.codenames.service.JugadorService;
import com.secretpanda.codenames.mapper.juego.CartaMapper;

/**
 * Pruebas de Desarrollo: Mecánicas Internas del Juego (RF-12, RF-16 a RF-22, RF-24, RF-25, RF-34)
 */
@ExtendWith(MockitoExtension.class)
public class MecanicasJuegoDesarrolloTest {

    @Mock private PartidaRepository partidaRepository;
    @Mock private TableroCartaRepository tableroCartaRepository;
    @Mock private PalabraTemaRepository palabraTemaRepository;
    @Mock private TurnoRepository turnoRepository;
    @Mock private VotoCartaRepository votoCartaRepository;
    @Mock private JugadorPartidaRepository jugadorPartidaRepository;
    @Mock private JugadorRepository jugadorRepository;
    @Mock private TemporizadorService temporizadorService;
    @Mock private LeaderboardService leaderboardService;
    @Mock private JugadorService jugadorService;
    @Mock private SimpMessagingTemplate messagingTemplate;
    @Mock private ApplicationContext applicationContext;

    @InjectMocks private JuegoService juegoService;

    @Test
    void testFlujoEstandarCodigoSecreto_Rf12() {
        Partida p = new Partida();
        p.setIdPartida(1);
        p.setEstado(Partida.EstadoPartida.en_curso);
        p.setTema(new Tema());
        p.getTema().setIdTema(1);
        
        List<JugadorPartida> jps = new ArrayList<>();
        for(int i=0; i<4; i++) {
            JugadorPartida jp = new JugadorPartida();
            jp.setJugador(new Jugador());
            jp.getJugador().setIdGoogle("user_" + i);
            jp.getJugador().setTag("User" + i);
            jp.setEquipo(i < 2 ? JugadorPartida.Equipo.rojo : JugadorPartida.Equipo.azul);
            jp.setRol(i % 2 == 0 ? JugadorPartida.Rol.lider : JugadorPartida.Rol.agente);
            jps.add(jp);
        }

        List<PalabraTema> palabras = new ArrayList<>();
        for(int i=0; i<20; i++) palabras.add(new PalabraTema());
        when(palabraTemaRepository.findByTema_IdTemaAndActivoTrue(1)).thenReturn(palabras);
        lenient().when(partidaRepository.findById(1)).thenReturn(Optional.of(p));
        lenient().when(jugadorPartidaRepository.findByPartida_IdPartida(1)).thenReturn(jps);

        juegoService.inicializarPartida(p, jps);

        verify(tableroCartaRepository).saveAll(any());
        verify(turnoRepository).save(any());
        verify(temporizadorService).iniciarTemporizador(any(), anyInt(), any());
    }

    @Test
    void testAsignacionRolesAleatoria_Rf16() {
        Partida p = new Partida();
        p.setIdPartida(1);
        p.setEstado(Partida.EstadoPartida.en_curso);
        p.setTema(new Tema());
        p.getTema().setIdTema(1);

        List<JugadorPartida> jps = new ArrayList<>();
        for(int i=0; i<4; i++) {
            JugadorPartida jp = new JugadorPartida();
            jp.setJugador(new Jugador());
            jp.getJugador().setIdGoogle("user_" + i);
            jp.setEquipo(i < 2 ? JugadorPartida.Equipo.rojo : JugadorPartida.Equipo.azul);
            jp.setRol(i % 2 == 0 ? JugadorPartida.Rol.lider : JugadorPartida.Rol.agente);
            jps.add(jp);
        }

        List<PalabraTema> palabras = new ArrayList<>();
        for(int i=0; i<20; i++) palabras.add(new PalabraTema());
        when(palabraTemaRepository.findByTema_IdTemaAndActivoTrue(any())).thenReturn(palabras);
        lenient().when(partidaRepository.findById(1)).thenReturn(Optional.of(p));
        lenient().when(jugadorPartidaRepository.findByPartida_IdPartida(1)).thenReturn(jps);
        
        juegoService.inicializarPartida(p, jps);

        long jefes = jps.stream().filter(j -> j.getRol() == JugadorPartida.Rol.lider).count();
        assertEquals(2, jefes); 
    }

    @Test
    void testGeneracionTablero4x5_Rf17() {
        Partida p = new Partida();
        p.setIdPartida(1);
        p.setEstado(Partida.EstadoPartida.en_curso);
        p.setTema(new Tema());
        p.getTema().setIdTema(1);

        List<PalabraTema> palabras = new ArrayList<>();
        for(int i=0; i<20; i++) palabras.add(new PalabraTema());
        
        when(palabraTemaRepository.findByTema_IdTemaAndActivoTrue(any())).thenReturn(palabras);
        lenient().when(partidaRepository.findById(1)).thenReturn(Optional.of(p));

        List<JugadorPartida> jps = new ArrayList<>();
        JugadorPartida l1 = new JugadorPartida(); l1.setEquipo(JugadorPartida.Equipo.rojo); l1.setRol(JugadorPartida.Rol.lider);
        l1.setJugador(new Jugador()); l1.getJugador().setIdGoogle("l1");
        JugadorPartida l2 = new JugadorPartida(); l2.setEquipo(JugadorPartida.Equipo.azul); l2.setRol(JugadorPartida.Rol.lider);
        l2.setJugador(new Jugador()); l2.getJugador().setIdGoogle("l2");
        jps.add(l1); jps.add(l2);
        lenient().when(jugadorPartidaRepository.findByPartida_IdPartida(1)).thenReturn(jps);

        juegoService.inicializarPartida(p, jps);

        verify(tableroCartaRepository).saveAll(argThat(list -> ((List)list).size() == 20));
    }

    @Test
    void testVistasDiferentesLiderVsAgente_Rf18() {
        TableroCarta cartaOculta = new TableroCarta();
        cartaOculta.setTipo(TableroCarta.TipoCarta.asesino);
        cartaOculta.setEstado(TableroCarta.EstadoCarta.oculta);
        cartaOculta.setPalabra(new PalabraTema());
        cartaOculta.getPalabra().setValor("Bomba");

        var dtoLider = CartaMapper.toDTO(cartaOculta, true);
        assertEquals("asesino", dtoLider.getTipo());

        var dtoAgente = CartaMapper.toDTO(cartaOculta, false);
        assertNull(dtoAgente.getTipo()); 
    }

    @Test
    void testValidacionPistaSinEspaciosNiEspeciales_Rf19() {
        String id = "lider_id";
        Partida p = new Partida();
        p.setIdPartida(1);
        p.setEstado(Partida.EstadoPartida.en_curso);
        JugadorPartida jp = new JugadorPartida();
        jp.setRol(JugadorPartida.Rol.lider);
        jp.setEquipo(JugadorPartida.Equipo.rojo);

        when(partidaRepository.findByIdForUpdate(1)).thenReturn(Optional.of(p));
        lenient().when(partidaRepository.findById(1)).thenReturn(Optional.of(p));
        when(jugadorPartidaRepository.findByJugador_IdGoogleAndPartida_IdPartida(id, 1)).thenReturn(Optional.of(jp));

        assertThrows(RuntimeException.class, () -> juegoService.darPista(1, "dos palabras", 2, id));
        assertThrows(RuntimeException.class, () -> juegoService.darPista(1, "pista!", 1, id));
    }

    @Test
    void testExpiracionTemporizadorPasaTurno_Rf34() {
        Partida p = new Partida();
        p.setIdPartida(1);
        p.setEstado(Partida.EstadoPartida.en_curso);
        
        Turno t = new Turno();
        JugadorPartida jp = new JugadorPartida();
        jp.setJugador(new Jugador());
        jp.getJugador().setIdGoogle("u1");
        jp.setEquipo(JugadorPartida.Equipo.rojo);
        jp.setRol(JugadorPartida.Rol.lider);
        t.setJugadorPartida(jp);
        t.setPalabraPista(null); 

        JugadorPartida jpRival = new JugadorPartida();
        jpRival.setJugador(new Jugador());
        jpRival.getJugador().setIdGoogle("u2");
        jpRival.setEquipo(JugadorPartida.Equipo.azul);
        jpRival.setRol(JugadorPartida.Rol.lider);

        when(partidaRepository.findByIdForUpdate(1)).thenReturn(Optional.of(p));
        when(partidaRepository.findById(1)).thenReturn(Optional.of(p));
        when(turnoRepository.findFirstByPartida_IdPartidaOrderByNumTurnoDesc(1)).thenReturn(Optional.of(t));
        when(jugadorPartidaRepository.findByPartida_IdPartida(1)).thenReturn(List.of(jp, jpRival));

        juegoService.forzarFinTurno(1);

        verify(turnoRepository, atLeastOnce()).save(any(Turno.class));
    }

    @Test
    void testDerrotaPorRevelarAsesino_Rf24() {
        TransactionSynchronizationManager.initSynchronization();
        try {
            Partida p = new Partida();
            p.setIdPartida(1);
            p.setEstado(Partida.EstadoPartida.en_curso);
            p.setJugadores(new ArrayList<>());
            
            Turno t = new Turno();
            t.setIdTurno(10);
            
            TableroCarta asesino = new TableroCarta();
            asesino.setTipo(TableroCarta.TipoCarta.asesino);
            asesino.setIdCartaTablero(666);
            asesino.setEstado(TableroCarta.EstadoCarta.oculta);

            VotoCarta v = new VotoCarta();
            v.setCartaTablero(asesino);
            JugadorPartida jp = new JugadorPartida();
            jp.setJugador(new Jugador());
            jp.getJugador().setIdGoogle("u1");
            jp.setEquipo(JugadorPartida.Equipo.rojo);
            jp.setRol(JugadorPartida.Rol.agente);
            v.setJugadorPartida(jp);

            when(votoCartaRepository.findByTurno_IdTurnoAndCartaReveladaIsNull(10)).thenReturn(List.of(v));
            when(tableroCartaRepository.findById(666)).thenReturn(Optional.of(asesino));
            lenient().when(partidaRepository.findById(1)).thenReturn(Optional.of(p));
            lenient().when(jugadorPartidaRepository.findByPartida_IdPartida(1)).thenReturn(List.of(jp));

            juegoService.resolverVotacion(p, t, JugadorPartida.Equipo.rojo);
            
            assertEquals(Partida.EstadoPartida.finalizada, p.getEstado());
            assertFalse(p.getRojoGana()); 
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void testVotarCartasUnaAUna_Rf20() {
        TransactionSynchronizationManager.initSynchronization();
        try {
            String idGoogle = "agente_id";
            Integer idPartida = 1;
            Integer idCarta = 15;

            Partida p = new Partida();
            p.setIdPartida(idPartida);
            p.setEstado(Partida.EstadoPartida.en_curso);
            
            JugadorPartida jp = new JugadorPartida();
            jp.setIdJugadorPartida(100);
            jp.setJugador(new Jugador());
            jp.getJugador().setIdGoogle(idGoogle);
            jp.getJugador().setTag("Agente");
            jp.setRol(JugadorPartida.Rol.agente);
            jp.setEquipo(JugadorPartida.Equipo.rojo);

            Turno t = new Turno();
            t.setIdTurno(1);
            t.setPartida(p);
            t.setJugadorPartida(new JugadorPartida());
            t.getJugadorPartida().setEquipo(JugadorPartida.Equipo.rojo);
            t.setPalabraPista("Pista");

            TableroCarta carta = new TableroCarta();
            carta.setEstado(TableroCarta.EstadoCarta.oculta);

            when(partidaRepository.findByIdForUpdate(idPartida)).thenReturn(Optional.of(p));
            when(jugadorPartidaRepository.findByJugador_IdGoogleAndPartida_IdPartida(idGoogle, idPartida)).thenReturn(Optional.of(jp));
            when(turnoRepository.findFirstByPartida_IdPartidaOrderByNumTurnoDesc(idPartida)).thenReturn(Optional.of(t));
            when(tableroCartaRepository.findById(idCarta)).thenReturn(Optional.of(carta));
            when(jugadorPartidaRepository.findByPartida_IdPartidaAndAbandonoFalse(idPartida)).thenReturn(List.of(jp));

            juegoService.votar(idPartida, idCarta, null, idGoogle);

            verify(votoCartaRepository).saveAndFlush(any(VotoCarta.class));
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void testMostrarAciertosAlFinalizar_Rf25() {
        Integer idPartida = 1;
        Partida p = new Partida();
        p.setIdPartida(idPartida);
        p.setEstado(Partida.EstadoPartida.finalizada);
        p.setRojoGana(true);

        when(partidaRepository.findById(idPartida)).thenReturn(Optional.of(p));
        JugadorPartida jp = new JugadorPartida();
        jp.setJugador(new Jugador());
        jp.getJugador().setIdGoogle("u1");
        when(jugadorPartidaRepository.findByJugador_IdGoogleAndPartida_IdPartida(any(), eq(idPartida))).thenReturn(Optional.of(jp));
        
        when(tableroCartaRepository.countByPartida_IdPartidaAndTipoAndEstado(eq(idPartida), eq(TableroCarta.TipoCarta.rojo), eq(TableroCarta.EstadoCarta.revelada))).thenReturn(9L);
        when(tableroCartaRepository.countByPartida_IdPartidaAndTipoAndEstado(eq(idPartida), eq(TableroCarta.TipoCarta.azul), eq(TableroCarta.EstadoCarta.revelada))).thenReturn(5L);

        var fin = juegoService.getFinPartida(idPartida, "user_id");

        assertEquals("Rojo", fin.getEquipoGanador());
        assertEquals(9, fin.getAciertosRojo());
        assertEquals(5, fin.getAciertosAzul());
    }
}
