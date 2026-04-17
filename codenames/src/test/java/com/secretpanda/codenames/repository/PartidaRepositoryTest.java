package com.secretpanda.codenames.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import com.secretpanda.codenames.model.Jugador;
import com.secretpanda.codenames.model.Partida;
import com.secretpanda.codenames.model.Tema;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PartidaRepositoryTest {

    @Autowired private PartidaRepository partidaRepository;
    @Autowired private JugadorRepository jugadorRepository;
    @Autowired private TemaRepository temaRepository;

    @Test
    void testFindPartidasPublicasDisponibles_DebeFiltrarPorEstado() {
        Jugador j = new Jugador();
        j.setIdGoogle("p_repo"); j.setTag("P_Repo");
        jugadorRepository.save(j);

        Tema t = new Tema();
        t.setNombre("Tema_Repo");
        temaRepository.save(t);

        Partida p1 = new Partida();
        p1.setCreador(j); p1.setTema(t); p1.setCodigoPartida("PUB1");
        p1.setEsPublica(true); p1.setEstado(Partida.EstadoPartida.esperando);
        p1.setMaxJugadores(4);

        Partida p2 = new Partida();
        p2.setCreador(j); p2.setTema(t); p2.setCodigoPartida("PRIV1");
        p2.setEsPublica(false); p2.setEstado(Partida.EstadoPartida.esperando);
        
        partidaRepository.saveAll(List.of(p1, p2));

        List<Partida> disponibles = partidaRepository.findPartidasPublicasDisponibles(Partida.EstadoPartida.esperando);
        
        assertThat(disponibles).hasSize(1);
        assertThat(disponibles.get(0).getCodigoPartida()).isEqualTo("PUB1");
    }
}
