package com.secretpanda.codenames.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.secretpanda.codenames.dto.social.ChatMessageDTO;
import com.secretpanda.codenames.dto.social.EnviarMensajeDTO;
import com.secretpanda.codenames.exception.BadRequestException;
import com.secretpanda.codenames.exception.GameLogicException;
import com.secretpanda.codenames.exception.NotFoundException;
import com.secretpanda.codenames.model.Chat;
import com.secretpanda.codenames.model.JugadorPartida;
import com.secretpanda.codenames.model.Partida;
import com.secretpanda.codenames.repository.ChatRepository;
import com.secretpanda.codenames.repository.JugadorPartidaRepository;
import com.secretpanda.codenames.repository.PartidaRepository;

@Service
public class ChatService {

    private final ChatRepository             chatRepository;
    private final PartidaRepository          partidaRepository;
    private final JugadorPartidaRepository   jugadorPartidaRepository;
    private final ProfanityFilterService     profanityFilterService;

    public ChatService(ChatRepository chatRepository,
                       PartidaRepository partidaRepository,
                       JugadorPartidaRepository jugadorPartidaRepository,
                       ProfanityFilterService profanityFilterService) {
        this.chatRepository           = chatRepository;
        this.partidaRepository        = partidaRepository;
        this.jugadorPartidaRepository = jugadorPartidaRepository;
        this.profanityFilterService   = profanityFilterService;
    }

    @Transactional
    public ChatMessageDTO procesarMensaje(EnviarMensajeDTO dto, String idGoogle) {
        if (dto.getMensaje() == null || dto.getMensaje().isBlank()) {
            throw new BadRequestException("El mensaje no puede estar vacío.");
        }

        Partida partida = partidaRepository.findById(dto.getIdPartida())
                .orElseThrow(() -> new NotFoundException("Partida no encontrada."));

        JugadorPartida jp = jugadorPartidaRepository
                .findByJugador_IdGoogleAndPartida_IdPartida(idGoogle, partida.getIdPartida())
                .orElseThrow(() -> new BadRequestException("No perteneces a esta partida."));

        // El jefe puede leer el chat pero NO puede escribir (RF-23)
        if (JugadorPartida.Rol.lider.equals(jp.getRol())) {
            throw new GameLogicException("El jefe de espías no puede escribir en el chat.");
        }

        // Aplicar filtro profesional
        ProfanityFilterService.FilterResult result = profanityFilterService.filter(dto.getMensaje());
        String mensajeFiltrado = result.filteredText();

        // Guardar en BD
        Chat chat = new Chat();
        chat.setPartida(partida);
        chat.setJugadorPartida(jp);
        chat.setMensaje(mensajeFiltrado);
        chat = chatRepository.save(chat);

        // Construir DTO de respuesta
        ChatMessageDTO respuesta = new ChatMessageDTO();
        respuesta.setIdMensaje(chat.getIdMensaje());
        respuesta.setIdPartida(partida.getIdPartida());
        respuesta.setIdJugador(jp.getJugador().getIdGoogle());
        respuesta.setTag(jp.getJugador().getTag());
        respuesta.setEquipo(jp.getEquipo().name());
        respuesta.setMensaje(mensajeFiltrado);
        respuesta.setFecha(chat.getFecha());
        respuesta.setEsValido(!result.wasCensored());

        return respuesta;
    }
}
