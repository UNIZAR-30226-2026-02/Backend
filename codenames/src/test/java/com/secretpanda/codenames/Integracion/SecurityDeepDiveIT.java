package com.secretpanda.codenames.Integracion;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.stomp.StompSession;

import com.secretpanda.codenames.Integracion.config.IntegrationTestBase;
import com.secretpanda.codenames.Integracion.config.StompTestClient;
import com.secretpanda.codenames.model.Jugador;
import com.secretpanda.codenames.model.JugadorPartida;
import com.secretpanda.codenames.model.Partida;
import com.secretpanda.codenames.model.Tema;
import com.secretpanda.codenames.repository.JugadorPartidaRepository;
import com.secretpanda.codenames.repository.JugadorRepository;
import com.secretpanda.codenames.repository.PartidaRepository;
import com.secretpanda.codenames.repository.TemaRepository;

public class SecurityDeepDiveIT extends IntegrationTestBase {

    @Autowired private JugadorRepository jugadorRepository;
    @Autowired private PartidaRepository partidaRepository;
    @Autowired private TemaRepository temaRepository;
    @Autowired private JugadorPartidaRepository jugadorPartidaRepository;

    @Test
    void testAgente_NoPuedeDarPista() throws Exception {
        // GIVEN: Un agente en una partida en curso
        setupTemaBasico();
        Jugador a1 = new Jugador(); a1.setIdGoogle("a1"); a1.setTag("A1"); a1.setActivo(true); jugadorRepository.save(a1);
        Partida p = new Partida(); p.setCreador(a1); p.setTema(temaRepository.findById(1).get()); p.setEstado(Partida.EstadoPartida.en_curso); p.setCodigoPartida("SEC");
        p = partidaRepository.save(p);
        JugadorPartida jpa1 = new JugadorPartida(); jpa1.setJugador(a1); jpa1.setPartida(p); jpa1.setEquipo(JugadorPartida.Equipo.rojo); jpa1.setRol(JugadorPartida.Rol.agente); jugadorPartidaRepository.save(jpa1);

        StompTestClient client = new StompTestClient(port);
        StompSession s1 = client.connect(generateValidToken("a1"));

        // WHEN: Agente intenta dar pista
        com.secretpanda.codenames.controller.JuegoController.PistaPayload pp = new com.secretpanda.codenames.controller.JuegoController.PistaPayload("prohibido", 1);
        s1.send("/app/partidas/" + p.getIdPartida() + "/pista", pp);

        // THEN: No debería crearse ningún turno con esa pista
        Thread.sleep(1000);
        long turnosConPista = jdbcTemplate.queryForObject("SELECT count(*) FROM turno WHERE palabra_pista = 'prohibido'", Long.class);
        assertThat(turnosConPista).isZero();
    }
}
