package com.secretpanda.codenames.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import com.secretpanda.codenames.model.Jugador;
import com.secretpanda.codenames.model.JugadorPartida;
import com.secretpanda.codenames.model.Partida;
import com.secretpanda.codenames.model.Tema;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class JugadorPartidaRepositoryTest {

    @Autowired private JugadorPartidaRepository jugadorPartidaRepository;
    @Autowired private JugadorRepository jugadorRepository;
    @Autowired private PartidaRepository partidaRepository;
    @Autowired private TemaRepository temaRepository;

    @Test
    void testExistsByJugadorAndEstadoIn_DebeDetectarPartidasActivas() {
        // 1. Setup Data
        Jugador j = new Jugador();
        j.setIdGoogle("user_test_repo");
        j.setTag("RepoTester");
        jugadorRepository.save(j);

        Tema t = new Tema();
        t.setNombre("TestRepoTema");
        temaRepository.save(t);

        Partida p = new Partida();
        p.setCreador(j);
        p.setTema(t);
        p.setEstado(Partida.EstadoPartida.esperando);
        p.setCodigoPartida("REPO12");
        partidaRepository.save(p);

        JugadorPartida jp = new JugadorPartida();
        jp.setJugador(j);
        jp.setPartida(p);
        jp.setEquipo(JugadorPartida.Equipo.rojo);
        jp.setRol(JugadorPartida.Rol.agente);
        jp.setAbandono(false);
        jugadorPartidaRepository.save(jp);

        // 2. Ejecutar consulta optimizada que creamos
        boolean tieneActiva = jugadorPartidaRepository
            .existsByJugador_IdGoogleAndPartida_EstadoInAndAbandonoFalse(
                "user_test_repo", 
                List.of(Partida.EstadoPartida.en_curso, Partida.EstadoPartida.esperando)
            );

        assertThat(tieneActiva).isTrue();
    }
}
