package com.secretpanda.codenames.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import com.secretpanda.codenames.model.Partida;
import com.secretpanda.codenames.model.Turno;
import com.secretpanda.codenames.repository.PartidaRepository;
import com.secretpanda.codenames.repository.TableroCartaRepository;
import com.secretpanda.codenames.repository.TurnoRepository;
import com.secretpanda.codenames.repository.VotoCartaRepository;
import com.secretpanda.codenames.repository.JugadorPartidaRepository;
import com.secretpanda.codenames.model.JugadorPartida;

@ExtendWith(MockitoExtension.class)
class JuegoServiceTest {

    @Mock private PartidaRepository partidaRepository;
    @Mock private TableroCartaRepository tableroCartaRepository;
    @Mock private TurnoRepository turnoRepository;
    @Mock private VotoCartaRepository votoCartaRepository;
    @Mock private JugadorPartidaRepository jugadorPartidaRepository;
    @Mock private SimpMessagingTemplate messagingTemplate;
    @Mock private LeaderboardService leaderboardService;
    @Mock private JugadorService jugadorService;
    @Mock private TemporizadorService temporizadorService;

    @InjectMocks private JuegoService juegoService;

    @Test
    void testDarPista_CreaNuevoTurnoYNotifica() {
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

        when(partidaRepository.findById(idPartida)).thenReturn(Optional.of(p));
        when(jugadorPartidaRepository.findByJugador_IdGoogleAndPartida_IdPartida(idGoogle, idPartida))
                .thenReturn(Optional.of(jp));
        when(turnoRepository.findFirstByPartida_IdPartidaOrderByNumTurnoDesc(idPartida))
                .thenReturn(Optional.of(turnoExistente));
        
        juegoService.darPista(idPartida, "Espía", 2, idGoogle);

        assertEquals("Espía", turnoExistente.getPalabraPista());
        assertEquals(2, turnoExistente.getPistaNumero());
        verify(turnoRepository).save(turnoExistente);
    }
}
