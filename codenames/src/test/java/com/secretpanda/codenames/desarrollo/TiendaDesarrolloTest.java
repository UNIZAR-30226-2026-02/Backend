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
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import com.secretpanda.codenames.model.Jugador;
import com.secretpanda.codenames.model.Tema;
import com.secretpanda.codenames.model.Personalizacion;
import com.secretpanda.codenames.model.InventarioTema;
import com.secretpanda.codenames.model.InventarioPersonalizacion;
import com.secretpanda.codenames.repository.JugadorRepository;
import com.secretpanda.codenames.repository.TemaRepository;
import com.secretpanda.codenames.repository.PersonalizacionRepository;
import com.secretpanda.codenames.repository.InventarioTemaRepository;
import com.secretpanda.codenames.repository.InventarioPersonalizacionRepository;
import com.secretpanda.codenames.repository.JugadorLogroRepository;
import com.secretpanda.codenames.repository.JugadorPartidaRepository;
import com.secretpanda.codenames.repository.LogroRepository;
import com.secretpanda.codenames.service.TiendaService;
import com.secretpanda.codenames.service.JugadorService;
import com.secretpanda.codenames.util.EstadisticasCalculator;

/**
 * Pruebas de Desarrollo: Personalización y Tienda (RF-9 a RF-11)
 */
@ExtendWith(MockitoExtension.class)
public class TiendaDesarrolloTest {

    @Mock private TemaRepository temaRepository;
    @Mock private PersonalizacionRepository personalizacionRepository;
    @Mock private InventarioTemaRepository inventarioTemaRepository;
    @Mock private InventarioPersonalizacionRepository inventarioPersoRepository;
    @Mock private JugadorRepository jugadorRepository;
    @Mock private JugadorLogroRepository jugadorLogroRepository;
    @Mock private JugadorPartidaRepository jugadorPartidaRepository;
    @Mock private LogroRepository logroRepository;
    @Mock private EstadisticasCalculator calculator;
    @Mock private JugadorService jugadorService;
    @Mock private org.springframework.context.ApplicationEventPublisher eventPublisher;
    
    @InjectMocks private TiendaService tiendaService;

    @Test
    void testAdquirirTemaEnTienda_Rf9() {
        String id = "google_123";
        Jugador j = new Jugador();
        j.setBalas(200);

        Tema t = new Tema();
        t.setIdTema(5);
        t.setPrecioBalas(100);

        when(jugadorRepository.findByIdForUpdate(id)).thenReturn(Optional.of(j));
        when(temaRepository.findById(5)).thenReturn(Optional.of(t));
        when(inventarioTemaRepository.existsById_IdJugadorAndId_IdTema(id, 5)).thenReturn(false);

        tiendaService.comprarTema(id, 5);

        verify(jugadorService).modificarBalas(id, -100);
        verify(inventarioTemaRepository).save(any(InventarioTema.class));
    }

    @Test
    void testConfigurarPersonalizacionAdquirida_Rf10() {
        String id = "google_123";
        Personalizacion p = new Personalizacion();
        p.setIdPersonalizacion(10);
        p.setTipo(Personalizacion.TipoPersonalizacion.tablero);
        p.setValorVisual("FF0000");

        InventarioPersonalizacion inv = new InventarioPersonalizacion();
        inv.setPersonalizacion(p);
        inv.setEquipado(false);

        JugadorService realJugadorService = new JugadorService(jugadorRepository, inventarioTemaRepository, inventarioPersoRepository, jugadorPartidaRepository, jugadorLogroRepository, calculator, logroRepository, mock(SimpMessagingTemplate.class));

        when(inventarioPersoRepository.findById_IdJugadorAndId_IdPersonalizacion(id, 10))
            .thenReturn(Optional.of(inv));

        realJugadorService.equiparItem(10, true, id);

        assertTrue(inv.isEquipado());
        verify(inventarioPersoRepository).save(inv);
    }

    @Test
    void testRecompensasFinPartida_Rf11() {
        String id = "google_123";
        Jugador j = new Jugador();
        j.setIdGoogle(id);
        j.setBalas(0);
        
        JugadorService realJugadorService = new JugadorService(jugadorRepository, null, null, null, jugadorLogroRepository, null, null, mock(SimpMessagingTemplate.class));
        ReflectionTestUtils.setField(realJugadorService, "balasGanador", 20);
        ReflectionTestUtils.setField(realJugadorService, "balasDerrota", 10);

        when(jugadorRepository.findByIdForUpdate(id)).thenReturn(Optional.of(j));
        lenient().when(jugadorRepository.findById(id)).thenReturn(Optional.of(j));
        when(jugadorLogroRepository.findById_IdJugadorAndCompletadoFalse(id)).thenReturn(List.of());

        realJugadorService.procesarFinPartida(id, true, 0, 0, com.secretpanda.codenames.model.JugadorPartida.Rol.agente);
        assertEquals(20, j.getBalas());

        realJugadorService.procesarFinPartida(id, false, 0, 0, com.secretpanda.codenames.model.JugadorPartida.Rol.agente);
        assertEquals(30, j.getBalas()); 
    }
}
