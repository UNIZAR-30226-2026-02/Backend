package com.secretpanda.codenames.Integracion;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.secretpanda.codenames.Integracion.config.IntegrationTestBase;
import com.secretpanda.codenames.dto.juego.GameStateDTO;
import com.secretpanda.codenames.model.Jugador;
import com.secretpanda.codenames.model.JugadorPartida;
import com.secretpanda.codenames.model.PalabraTema;
import com.secretpanda.codenames.model.Partida;
import com.secretpanda.codenames.model.Tema;
import com.secretpanda.codenames.repository.JugadorPartidaRepository;
import com.secretpanda.codenames.repository.JugadorRepository;
import com.secretpanda.codenames.repository.PalabraTemaRepository;
import com.secretpanda.codenames.repository.PartidaRepository;
import com.secretpanda.codenames.repository.TemaRepository;

public class JuegoAvanzadoIT extends IntegrationTestBase {

    @Autowired private JugadorRepository jugadorRepository;
    @Autowired private PartidaRepository partidaRepository;
    @Autowired private TemaRepository temaRepository;
    @Autowired private PalabraTemaRepository palabraTemaRepository;
    @Autowired private JugadorPartidaRepository jugadorPartidaRepository;

    @Test
    void testVictoriaInmediata_Asesino() throws Exception {
        // GIVEN: Partida en curso, turno rojo
        setupTemaBasico();
        for (int i = 0; i < 5; i++) {
            PalabraTema p = new PalabraTema(); p.setValor("P" + i); p.setTema(temaRepository.findById(1).get()); p.setActivo(true); palabraTemaRepository.save(p);
        }

        Jugador l1 = new Jugador(); l1.setIdGoogle("l1"); l1.setTag("L1"); l1.setActivo(true); jugadorRepository.save(l1);
        Jugador a1 = new Jugador(); a1.setIdGoogle("a1"); a1.setTag("A1"); a1.setActivo(true); jugadorRepository.save(a1);

        Partida p = new Partida(); p.setCreador(l1); p.setTema(temaRepository.findById(1).get()); p.setEstado(Partida.EstadoPartida.en_curso); p.setCodigoPartida("KILL");
        p = partidaRepository.save(p);

        JugadorPartida jpl1 = new JugadorPartida(); jpl1.setJugador(l1); jpl1.setPartida(p); jpl1.setEquipo(JugadorPartida.Equipo.rojo); jpl1.setRol(JugadorPartida.Rol.lider); jugadorPartidaRepository.save(jpl1);
        JugadorPartida jpa1 = new JugadorPartida(); jpa1.setJugador(a1); jpa1.setPartida(p); jpa1.setEquipo(JugadorPartida.Equipo.rojo); jpa1.setRol(JugadorPartida.Rol.agente); jugadorPartidaRepository.save(jpa1);

        // Tablero con asesino en 0,0
        jdbcTemplate.execute("INSERT INTO tablero_carta (id_partida, fila, columna, id_palabra, tipo, estado) VALUES (" + 
            p.getIdPartida() + ", 0, 0, (SELECT id_palabra FROM palabra_tema WHERE valor='P0'), 'asesino', 'oculta')");
        Integer idAsesino = jdbcTemplate.queryForObject("SELECT id_carta_tablero FROM tablero_carta WHERE fila=0 AND columna=0", Integer.class);

        // Turno
        jdbcTemplate.execute("INSERT INTO turno (id_partida, id_jugador_partida, num_turno, palabra_pista, pista_numero, aciertos_turno) VALUES (" + 
            p.getIdPartida() + ", " + jpl1.getIdJugadorPartida() + ", 1, 'peligro', 1, 0)");

        // WHEN: Agente vota asesino
        com.secretpanda.codenames.Integracion.config.StompTestClient client = new com.secretpanda.codenames.Integracion.config.StompTestClient(port);
        org.springframework.messaging.simp.stomp.StompSession s1 = client.connect(generateValidToken("a1"));
        
        com.secretpanda.codenames.controller.JuegoController.VotarPayload vp = new com.secretpanda.codenames.controller.JuegoController.VotarPayload(idAsesino, null);
        s1.send("/app/partidas/" + p.getIdPartida() + "/votar", vp);

        // THEN: Partida finalizada, rojoGana = false
        Thread.sleep(1000); // Esperar resolución async
        Partida pFinal = partidaRepository.findById(p.getIdPartida()).get();
        assertThat(pFinal.getEstado()).isEqualTo(Partida.EstadoPartida.finalizada);
        assertThat(pFinal.getRojoGana()).isFalse();
    }
}
