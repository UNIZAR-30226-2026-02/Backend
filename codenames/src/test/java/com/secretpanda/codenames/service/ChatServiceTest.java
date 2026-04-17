package com.secretpanda.codenames.service;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.secretpanda.codenames.dto.social.ChatMessageDTO;
import com.secretpanda.codenames.dto.social.EnviarMensajeDTO;
import com.secretpanda.codenames.exception.GameLogicException;
import com.secretpanda.codenames.model.Chat;
import com.secretpanda.codenames.model.Jugador;
import com.secretpanda.codenames.model.JugadorPartida;
import com.secretpanda.codenames.model.Partida;
import com.secretpanda.codenames.repository.ChatRepository;
import com.secretpanda.codenames.repository.JugadorPartidaRepository;
import com.secretpanda.codenames.repository.PartidaRepository;

@ExtendWith(MockitoExtension.class)
public class ChatServiceTest {

    @Mock
    private ChatRepository chatRepository;
    @Mock
    private PartidaRepository partidaRepository;
    @Mock
    private JugadorPartidaRepository jugadorPartidaRepository;
    @Mock
    private ProfanityFilterService profanityFilterService;

    @InjectMocks
    private ChatService chatService;

    @Test
    void testProcesarMensaje_LiderLanzaExcepcion() {
        String idGoogle = "user123";
        EnviarMensajeDTO dto = new EnviarMensajeDTO();
        dto.setIdPartida(1);
        dto.setMensaje("Hola");

        Partida partida = new Partida();
        partida.setIdPartida(1);

        JugadorPartida jp = new JugadorPartida();
        jp.setRol(JugadorPartida.Rol.lider);

        when(partidaRepository.findById(1)).thenReturn(Optional.of(partida));
        when(jugadorPartidaRepository.findByJugador_IdGoogleAndPartida_IdPartida(idGoogle, 1))
                .thenReturn(Optional.of(jp));

        assertThrows(GameLogicException.class, () -> chatService.procesarMensaje(dto, idGoogle));
    }

    @Test
    void testProcesarMensaje_Exito() {
        String idGoogle = "user123";
        EnviarMensajeDTO dto = new EnviarMensajeDTO();
        dto.setIdPartida(1);
        dto.setMensaje("Hola insulto");

        Partida partida = new Partida();
        partida.setIdPartida(1);

        Jugador jugador = new Jugador();
        jugador.setIdGoogle(idGoogle);
        jugador.setTag("Player1");

        JugadorPartida jp = new JugadorPartida();
        jp.setRol(JugadorPartida.Rol.agente);
        jp.setEquipo(JugadorPartida.Equipo.azul);
        jp.setJugador(jugador);

        ProfanityFilterService.FilterResult filterResult = new ProfanityFilterService.FilterResult("Hola ****", true);

        when(partidaRepository.findById(1)).thenReturn(Optional.of(partida));
        when(jugadorPartidaRepository.findByJugador_IdGoogleAndPartida_IdPartida(idGoogle, 1))
                .thenReturn(Optional.of(jp));
        when(profanityFilterService.filter("Hola insulto")).thenReturn(filterResult);
        when(chatRepository.save(any(Chat.class))).thenAnswer(i -> i.getArgument(0));

        ChatMessageDTO result = chatService.procesarMensaje(dto, idGoogle);

        assertNotNull(result);
        assertEquals("Hola ****", result.getMensaje());
        assertFalse(result.isEsValido());
        verify(profanityFilterService).filter("Hola insulto");
        verify(chatRepository).save(any(Chat.class));
    }
}
