package com.secretpanda.codenames.desarrollo;

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
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import com.secretpanda.codenames.dto.social.EnviarMensajeDTO;
import com.secretpanda.codenames.model.Jugador;
import com.secretpanda.codenames.model.JugadorPartida;
import com.secretpanda.codenames.model.Partida;
import com.secretpanda.codenames.model.Amistad;
import com.secretpanda.codenames.model.AmistadId;
import com.secretpanda.codenames.repository.ChatRepository;
import com.secretpanda.codenames.repository.JugadorPartidaRepository;
import com.secretpanda.codenames.repository.PartidaRepository;
import com.secretpanda.codenames.repository.AmistadRepository;
import com.secretpanda.codenames.repository.JugadorRepository;
import com.secretpanda.codenames.service.ChatService;
import com.secretpanda.codenames.service.AmistadService;
import com.secretpanda.codenames.service.ProfanityFilterService;
import com.secretpanda.codenames.service.LeaderboardService;

/**
 * Pruebas de Desarrollo: Comunicación y Social (RF-26 a RF-33, RF-36)
 */
@ExtendWith(MockitoExtension.class)
public class SocialDesarrolloTest {

    @Mock private ChatRepository chatRepository;
    @Mock private PartidaRepository partidaRepository;
    @Mock private JugadorPartidaRepository jugadorPartidaRepository;
    @Mock private ProfanityFilterService profanityFilterService;
    @Mock private AmistadRepository amistadRepository;
    @Mock private JugadorRepository jugadorRepository;
    @Mock private SimpMessagingTemplate messagingTemplate;
    @Mock private ApplicationEventPublisher eventPublisher;

    @InjectMocks private ChatService chatService;
    @InjectMocks private AmistadService amistadService;
    @InjectMocks private LeaderboardService leaderboardService;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(leaderboardService, "rankingGlobalSize", 10);
        ReflectionTestUtils.setField(leaderboardService, "rankingAmigosSize", 100);
    }

    @Test
    void testLiderNoPuedeEscribirEnChat_Rf26() {
        String id = "lider_id";
        JugadorPartida jp = new JugadorPartida();
        jp.setRol(JugadorPartida.Rol.lider);

        when(partidaRepository.findById(1)).thenReturn(Optional.of(new Partida()));
        when(jugadorPartidaRepository.findByJugador_IdGoogleAndPartida_IdPartida(id, 1)).thenReturn(Optional.of(jp));

        EnviarMensajeDTO dto = new EnviarMensajeDTO();
        dto.setIdPartida(1);
        dto.setMensaje("Trampa!");

        assertThrows(RuntimeException.class, () -> chatService.procesarMensaje(dto, id));
    }

    @Test
    void testAñadirAmigoYSolicitud_Rf29() {
        String solicitanteId = "user_A";
        Jugador receptor = new Jugador();
        receptor.setIdGoogle("user_B");
        receptor.setTag("Panda#1");
        receptor.setActivo(true);

        when(jugadorRepository.findById(solicitanteId)).thenReturn(Optional.of(new Jugador()));
        when(jugadorRepository.findByTagAndActivoTrue("Panda#1")).thenReturn(Optional.of(receptor));
        when(amistadRepository.findAmistadEntreJugadores(any(), any())).thenReturn(Optional.empty());

        amistadService.enviarSolicitud(solicitanteId, "Panda#1");

        verify(amistadRepository).save(any(Amistad.class));
    }

    @Test
    void testGestionarSolicitudAceptar_Rf31() {
        AmistadId id = new AmistadId();
        id.setIdSolicitante("A");
        id.setIdReceptor("B");
        
        Amistad a = new Amistad();
        a.setId(id);
        a.setEstado(Amistad.EstadoAmistad.pendiente);
        a.setSolicitante(new Jugador());
        a.setReceptor(new Jugador());

        when(amistadRepository.findById(id)).thenReturn(Optional.of(a));

        amistadService.gestionarSolicitud("B", "A", "aceptada");

        assertEquals(Amistad.EstadoAmistad.aceptada, a.getEstado());
        verify(amistadRepository).save(a);
    }

    @Test
    void testLeaderboardGlobalTop10_Rf33() {
        when(jugadorRepository.findByActivoTrueOrderByVictoriasDescNumAciertosDesc(any())).thenReturn(List.of());

        var ranking = leaderboardService.getGlobalRanking();

        assertNotNull(ranking);
    }

    @Test
    void testFiltroPalabrasOfensivas_Rf36() {
        ProfanityFilterService filter = new ProfanityFilterService(new String[]{"insulto"});
        var result = filter.filter("Eres un insulto");
        assertTrue(result.wasCensored());
        assertEquals("Eres un *******", result.filteredText());
    }

    @Test
    void testBuscarAmigosPorTag_Rf30() {
        String miId = "google_me";
        when(jugadorRepository.findByTagContainingIgnoreCaseAndActivoTrue(eq("Panda"), any())).thenReturn(List.of());

        var resultados = amistadService.buscarJugadores("Panda", miId);

        assertNotNull(resultados);
        verify(jugadorRepository).findByTagContainingIgnoreCaseAndActivoTrue(eq("Panda"), any());
    }
}
