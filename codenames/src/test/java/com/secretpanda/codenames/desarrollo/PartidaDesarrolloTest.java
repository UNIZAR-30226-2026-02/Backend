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
import org.springframework.test.util.ReflectionTestUtils;

import com.secretpanda.codenames.dto.partida.CrearPartidaDTO;
import com.secretpanda.codenames.model.Jugador;
import com.secretpanda.codenames.model.Partida;
import com.secretpanda.codenames.model.Tema;
import com.secretpanda.codenames.model.JugadorPartida;
import com.secretpanda.codenames.repository.PartidaRepository;
import com.secretpanda.codenames.repository.JugadorRepository;
import com.secretpanda.codenames.repository.TemaRepository;
import com.secretpanda.codenames.repository.JugadorPartidaRepository;
import com.secretpanda.codenames.repository.InventarioTemaRepository;
import com.secretpanda.codenames.repository.TableroCartaRepository;
import com.secretpanda.codenames.service.PartidaService;
import com.secretpanda.codenames.service.LobbyService;
import com.secretpanda.codenames.service.JugadorService;
import com.secretpanda.codenames.service.JuegoService;
import com.secretpanda.codenames.util.CodigoPartidaGenerator;

/**
 * Pruebas de Desarrollo: Dinámica de Sala y Partidas (RF-13, RF-14, RF-15, RF-23.x, RF-34, RF-35)
 */
@ExtendWith(MockitoExtension.class)
public class PartidaDesarrolloTest {

    @Mock private PartidaRepository partidaRepository;
    @Mock private JugadorRepository jugadorRepository;
    @Mock private TemaRepository temaRepository;
    @Mock private JugadorPartidaRepository jugadorPartidaRepository;
    @Mock private InventarioTemaRepository inventarioTemaRepository;
    @Mock private TableroCartaRepository tableroCartaRepository;
    @Mock private CodigoPartidaGenerator codigoGenerator;
    @Mock private LobbyService lobbyService;
    @Mock private JugadorService jugadorService;
    @Mock private JuegoService juegoService;

    @InjectMocks private PartidaService partidaService;

    @Test
    void testCrearPartidaConfigurable_Rf13() {
        String id = "creador_id";
        Jugador creador = new Jugador();
        creador.setIdGoogle(id);
        creador.setTag("Admin");

        Tema tema = new Tema();
        tema.setIdTema(2);
        tema.setNombre("Ciber");

        when(jugadorRepository.findById(id)).thenReturn(Optional.of(creador));
        when(temaRepository.findById(2)).thenReturn(Optional.of(tema));
        when(inventarioTemaRepository.existsById_IdJugadorAndId_IdTema(id, 2)).thenReturn(true);
        when(codigoGenerator.generarCodigo()).thenReturn("ABC123");

        Partida partidaGuardada = new Partida();
        partidaGuardada.setIdPartida(1);
        partidaGuardada.setTema(tema);
        partidaGuardada.setCreador(creador);
        partidaGuardada.setCodigoPartida("ABC123");
        partidaGuardada.setEstado(Partida.EstadoPartida.esperando);

        when(partidaRepository.save(any(Partida.class))).thenReturn(partidaGuardada);

        CrearPartidaDTO dto = new CrearPartidaDTO();
        dto.setIdTema(2);
        dto.setTiempoEspera(90); 
        dto.setMaxJugadores(10); 
        dto.setEsPublica(true);

        partidaService.crearPartida(dto, id);

        verify(partidaRepository).save(argThat(p -> 
            p.getTiempoEspera() == 90 && p.getMaxJugadores() == 10 && p.isEsPublica()
        ));
    }

    @Test
    void testUnirsePartidaPrivadaConCodigo_Rf14() {
        String id = "unido_id";
        Partida p = new Partida();
        p.setIdPartida(1);
        p.setEsPublica(false);
        p.setMaxJugadores(8);
        p.setEstado(Partida.EstadoPartida.esperando);

        when(partidaRepository.findByCodigoPartida("XYZ789")).thenReturn(Optional.of(p));
        lenient().when(partidaRepository.findByIdForUpdate(1)).thenReturn(Optional.of(p));
        when(jugadorRepository.findById(id)).thenReturn(Optional.of(new Jugador()));

        partidaService.unirsePartidaPrivada("XYZ789", id);

        verify(jugadorPartidaRepository).save(any(JugadorPartida.class));
    }

    @Test
    void testRestriccionUnirsePublicaSinTema_Rf15() {
        String id = "pobre_id";
        Partida p = new Partida();
        p.setIdPartida(2);
        p.setEsPublica(true);
        p.setTema(new Tema());
        p.getTema().setIdTema(3);
        p.getTema().setNombre("Tema Caro");

        lenient().when(partidaRepository.findByIdForUpdate(2)).thenReturn(Optional.of(p));
        lenient().when(partidaRepository.findById(2)).thenReturn(Optional.of(p));
        when(inventarioTemaRepository.existsById_IdJugadorAndId_IdTema(id, 3)).thenReturn(false);

        assertThrows(RuntimeException.class, () -> {
            partidaService.unirsePartidaPublica(2, id);
        });
    }

    @Test
    void testValidacionMinimoJugadoresParaIniciar_Rf23_5() {
        Partida p = new Partida();
        p.setIdPartida(1);
        p.setEstado(Partida.EstadoPartida.esperando);
        p.setCreador(new Jugador());
        p.getCreador().setIdGoogle("admin");

        List<JugadorPartida> soloTresJugadores = new ArrayList<>();
        for(int i=0; i<2; i++) {
            JugadorPartida jpR = new JugadorPartida(); jpR.setEquipo(JugadorPartida.Equipo.rojo);
            soloTresJugadores.add(jpR);
        }
        JugadorPartida jpA = new JugadorPartida(); jpA.setEquipo(JugadorPartida.Equipo.azul);
        soloTresJugadores.add(jpA);

        when(partidaRepository.findById(1)).thenReturn(Optional.of(p));
        when(jugadorPartidaRepository.findByPartida_IdPartidaAndAbandonoFalse(1)).thenReturn(soloTresJugadores);

        LobbyService lobbyServiceReal = new LobbyService(partidaRepository, jugadorRepository, jugadorPartidaRepository, temaRepository, inventarioTemaRepository, null, null);
        
        assertThrows(RuntimeException.class, () -> lobbyServiceReal.iniciarPartida(1, "admin"));
    }

    @Test
    void testAbandonoPenalizacionBalas_Rf35() {
        Integer idPartida = 10;
        String idGoogle = "quitter_id";
        Partida p = new Partida();
        p.setIdPartida(idPartida);
        p.setEstado(Partida.EstadoPartida.en_curso);
        p.setCreador(new Jugador());
        p.getCreador().setIdGoogle("other");
        
        JugadorPartida jp = new JugadorPartida();
        jp.setJugador(new Jugador());
        jp.getJugador().setIdGoogle(idGoogle);
        jp.setEquipo(JugadorPartida.Equipo.rojo);
        jp.setRol(JugadorPartida.Rol.agente);

        lenient().when(partidaRepository.findByIdForUpdate(idPartida)).thenReturn(Optional.of(p));
        when(jugadorPartidaRepository.findByJugador_IdGoogleAndPartida_IdPartida(idGoogle, idPartida)).thenReturn(Optional.of(jp));
        
        ReflectionTestUtils.setField(partidaService, "balasPenalizacionAbandono", 5);

        partidaService.abandonar(idPartida, idGoogle, false);

        verify(jugadorService).modificarBalas(idGoogle, -5);
    }
}
