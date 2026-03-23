package com.secretpanda.codenames.service;

import java.util.Arrays;
import java.util.List;

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

    // Lista básica de palabras prohibidas — en producción reemplazar por una librería (e.g. better-profanity)
    private static final List<String> PALABRAS_PROHIBIDAS = Arrays.asList(
            "tonto", "tontito", "cara culo", "botijo"
            // Ampliar con términos reales
    );

    private final ChatRepository             chatRepository;
    private final PartidaRepository          partidaRepository;
    private final JugadorPartidaRepository   jugadorPartidaRepository;

    public ChatService(ChatRepository chatRepository,
                       PartidaRepository partidaRepository,
                       JugadorPartidaRepository jugadorPartidaRepository) {
        this.chatRepository           = chatRepository;
        this.partidaRepository        = partidaRepository;
        this.jugadorPartidaRepository = jugadorPartidaRepository;
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

        // Aplicar filtro
        boolean[] censurado = { false };
        String mensajeFiltrado = aplicarFiltro(dto.getMensaje(), censurado);

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
        respuesta.setEsValido(!censurado[0]);

        return respuesta;
    }

    private String aplicarFiltro(String mensaje, boolean[] censurado) {
        String resultado = mensaje;
        for (String palabra : PALABRAS_PROHIBIDAS) {
            String nuevo = resultado.replaceAll("(?i)" + java.util.regex.Pattern.quote(palabra), "***");
            if (!nuevo.equals(resultado)) {
                censurado[0] = true;
            }
            resultado = nuevo;
        }
        return resultado;
    }
}
