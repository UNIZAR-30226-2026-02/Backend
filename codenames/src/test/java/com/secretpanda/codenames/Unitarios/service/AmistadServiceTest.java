package com.secretpanda.codenames.Unitarios.service;

import com.secretpanda.codenames.service.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import com.secretpanda.codenames.exception.BadRequestException;
import com.secretpanda.codenames.exception.NotFoundException;
import com.secretpanda.codenames.model.Amistad;
import com.secretpanda.codenames.model.AmistadId;
import com.secretpanda.codenames.model.Jugador;
import com.secretpanda.codenames.repository.AmistadRepository;
import com.secretpanda.codenames.repository.JugadorRepository;

/**
 * Suite de pruebas unitarias para AmistadService.
 * Valida la lógica social: enviar, aceptar y rechazar amistades.
 */
@ExtendWith(MockitoExtension.class)
public class AmistadServiceTest {

    @Mock private AmistadRepository amistadRepository;
    @Mock private JugadorRepository jugadorRepository;
    @Mock private SimpMessagingTemplate messagingTemplate;
    @Mock private ApplicationEventPublisher eventPublisher;

    @InjectMocks private AmistadService amistadService;

    /**
     * Prueba: shouldThrowExceptionWhenAddingSelf
     * Verifica que no se pueda enviar una solicitud de amistad a uno mismo.
     */
    @Test
    public void shouldThrowExceptionWhenAddingSelf() {
        Jugador emisor = new Jugador();
        emisor.setIdGoogle("user1");
        emisor.setTag("Panda");

        when(jugadorRepository.findById("user1")).thenReturn(Optional.of(emisor));
        when(jugadorRepository.findByTagAndActivoTrue("Panda")).thenReturn(Optional.of(emisor));

        assertThrows(BadRequestException.class, () -> {
            amistadService.enviarSolicitud("user1", "Panda");
        });
    }

    /**
     * Prueba: shouldThrowExceptionIfRequestAlreadyExists
     * Verifica que no se pueda enviar una solicitud si ya existe una relación
     * (pendiente o aceptada) entre ambos jugadores, independientemente del orden.
     */
    @Test
    public void shouldThrowExceptionIfRequestAlreadyExists() {
        Jugador emisor = new Jugador();
        emisor.setIdGoogle("user1");

        Jugador receptor = new Jugador();
        receptor.setIdGoogle("user2");
        receptor.setTag("Gamer");

        when(jugadorRepository.findById("user1")).thenReturn(Optional.of(emisor));
        when(jugadorRepository.findByTagAndActivoTrue("Gamer")).thenReturn(Optional.of(receptor));

        // Simulamos que ya hay una solicitud pendiente enviada por el emisor al receptor
        Amistad mockAmistad = new Amistad();
        mockAmistad.setEstado(Amistad.EstadoAmistad.pendiente);
        mockAmistad.setSolicitante(emisor); // el emisor ya la había enviado antes
        mockAmistad.setReceptor(receptor);

        when(amistadRepository.findAmistadEntreJugadores("user1", "user2"))
                .thenReturn(Optional.of(mockAmistad));

        assertThrows(BadRequestException.class, () -> {
            amistadService.enviarSolicitud("user1", "Gamer");
        });
    }

    /**
     * Prueba: shouldAcceptRequestSuccessfully
     * Verifica que una solicitud pendiente pase a estado aceptada.
     */
    @Test
    public void shouldAcceptRequestSuccessfully() {
        String receptorId = "user1";
        String emisorId = "user2";

        AmistadId aid = new AmistadId();
        aid.setIdSolicitante(emisorId);
        aid.setIdReceptor(receptorId);

        Amistad solicitud = new Amistad();
        solicitud.setId(aid);
        solicitud.setEstado(Amistad.EstadoAmistad.pendiente);
        
        Jugador emisor = new Jugador();
        emisor.setIdGoogle(emisorId);
        solicitud.setSolicitante(emisor);
        
        Jugador receptor = new Jugador();
        receptor.setIdGoogle(receptorId);
        solicitud.setReceptor(receptor);

        when(amistadRepository.findById(aid))
                .thenReturn(Optional.of(solicitud));

        amistadService.gestionarSolicitud(receptorId, emisorId, "aceptada");

        assertEquals(Amistad.EstadoAmistad.aceptada, solicitud.getEstado());
        verify(amistadRepository).save(solicitud);
        // Dos para actualizar lista amigos, 1 para notificar al solicitante, 1 para lista solicitudes receptor
        verify(messagingTemplate, times(4)).convertAndSendToUser(anyString(), anyString(), any());
    }
}