package com.secretpanda.codenames.Integracion;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.stomp.StompSession;

import com.secretpanda.codenames.Integracion.config.IntegrationTestBase;
import com.secretpanda.codenames.Integracion.config.StompTestClient;
import com.secretpanda.codenames.dto.social.ChatMessageDTO;
import com.secretpanda.codenames.dto.social.EnviarMensajeDTO;
import com.secretpanda.codenames.model.Jugador;
import com.secretpanda.codenames.model.JugadorPartida;
import com.secretpanda.codenames.model.Partida;
import com.secretpanda.codenames.model.Tema;
import com.secretpanda.codenames.repository.JugadorPartidaRepository;
import com.secretpanda.codenames.repository.JugadorRepository;
import com.secretpanda.codenames.repository.PartidaRepository;
import com.secretpanda.codenames.repository.TemaRepository;

public class ChatIntegrationIT extends IntegrationTestBase {

    @Autowired private JugadorRepository jugadorRepository;
    @Autowired private PartidaRepository partidaRepository;
    @Autowired private TemaRepository temaRepository;
    @Autowired private JugadorPartidaRepository jugadorPartidaRepository;

    @Test
    void testChatEquipo_Aislamiento_EquipoAzulNoLeeARojo() throws Exception {
        // GIVEN: 2 jugadores en equipos distintos
        setupTemaBasico();
        
        Jugador r1 = new Jugador(); r1.setIdGoogle("r1"); r1.setTag("R1"); r1.setActivo(true); jugadorRepository.save(r1);
        Jugador a1 = new Jugador(); a1.setIdGoogle("a1"); a1.setTag("A1"); a1.setActivo(true); jugadorRepository.save(a1);

        Partida p = new Partida(); p.setCreador(r1); p.setTema(temaRepository.findById(1).get()); p.setEstado(Partida.EstadoPartida.en_curso); p.setCodigoPartida("CHAT");
        p = partidaRepository.save(p);

        JugadorPartida jpr1 = new JugadorPartida(); jpr1.setJugador(r1); jpr1.setPartida(p); jpr1.setEquipo(JugadorPartida.Equipo.rojo); jpr1.setRol(JugadorPartida.Rol.agente); jugadorPartidaRepository.save(jpr1);
        JugadorPartida jpa1 = new JugadorPartida(); jpa1.setJugador(a1); jpa1.setPartida(p); jpa1.setEquipo(JugadorPartida.Equipo.azul); jpa1.setRol(JugadorPartida.Rol.agente); jugadorPartidaRepository.save(jpa1);

        StompTestClient client = new StompTestClient(port);
        StompSession sr1 = client.connect(generateValidToken("r1"));
        StompSession sa1 = client.connect(generateValidToken("a1"));

        BlockingQueue<ChatMessageDTO> qr1 = client.subscribe(sr1, "/topic/partidas/" + p.getIdPartida() + "/chat/rojo", ChatMessageDTO.class);
        BlockingQueue<ChatMessageDTO> qa1 = client.subscribe(sa1, "/topic/partidas/" + p.getIdPartida() + "/chat/azul", ChatMessageDTO.class);

        Thread.sleep(500); // Wait for subscriptions to propagate

        // WHEN: r1 envía mensaje
        EnviarMensajeDTO msg = new EnviarMensajeDTO();
        msg.setIdPartida(p.getIdPartida());
        msg.setMensaje("Hola equipo rojo");
        sr1.send("/app/partidas/" + p.getIdPartida() + "/chat", msg);

        // THEN: r1 recibe el mensaje, a1 NO recibe nada
        ChatMessageDTO rec = qr1.poll(10, TimeUnit.SECONDS);
        assertThat(rec).isNotNull();
        assertThat(rec.getMensaje()).isEqualTo("Hola equipo rojo");

        ChatMessageDTO noRec = qa1.poll(2, TimeUnit.SECONDS);
        assertThat(noRec).isNull();
    }

    @Test
    void testEnvioMensajeGlobal_ConInsulto_DebeCensurarse() throws Exception {
        // En este proyecto el chat es por equipo, no hay "global" según el controlador.
        // Pero vamos a testear la censura en el chat de equipo.
        setupTemaBasico();
        Jugador r1 = new Jugador(); r1.setIdGoogle("rx"); r1.setTag("RX"); r1.setActivo(true); jugadorRepository.save(r1);
        Partida p = new Partida(); p.setCreador(r1); p.setTema(temaRepository.findById(1).get()); p.setEstado(Partida.EstadoPartida.en_curso); p.setCodigoPartida("CHAT2");
        p = partidaRepository.save(p);
        JugadorPartida jpr1 = new JugadorPartida(); jpr1.setJugador(r1); jpr1.setPartida(p); jpr1.setEquipo(JugadorPartida.Equipo.rojo); jpr1.setRol(JugadorPartida.Rol.agente); jugadorPartidaRepository.save(jpr1);

        StompTestClient client = new StompTestClient(port);
        StompSession sr1 = client.connect(generateValidToken("rx"));
        BlockingQueue<ChatMessageDTO> qr1 = client.subscribe(sr1, "/topic/partidas/" + p.getIdPartida() + "/chat/rojo", ChatMessageDTO.class);

        Thread.sleep(500);

        // WHEN: Envía insulto (configurado en application-test.yml: tonto,mierda,puto)
        EnviarMensajeDTO msg = new EnviarMensajeDTO();
        msg.setIdPartida(p.getIdPartida());
        msg.setMensaje("Eres un tonto");
        sr1.send("/app/partidas/" + p.getIdPartida() + "/chat", msg);

        // THEN: El mensaje llega censurado
        ChatMessageDTO rec = qr1.poll(10, TimeUnit.SECONDS);
        assertThat(rec).isNotNull();
        assertThat(rec.getMensaje()).isEqualTo("Eres un *****");
    }
}
