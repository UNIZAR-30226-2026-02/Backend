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
import com.secretpanda.codenames.model.PalabraTema;
import com.secretpanda.codenames.model.TableroCarta;
import com.secretpanda.codenames.model.TableroCarta.TipoCarta;
import com.secretpanda.codenames.model.TableroCarta.EstadoCarta;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TableroCartaRepositoryTest {

    @Autowired private TableroCartaRepository tableroCartaRepository;
    @Autowired private PartidaRepository partidaRepository;
    @Autowired private JugadorRepository jugadorRepository;
    @Autowired private TemaRepository temaRepository;
    @Autowired private PalabraTemaRepository palabraTemaRepository;

    @Test
    void testCountByPartidaAndTipoAndEstado_DebeContarCorrectamente() {
        Jugador j = new Jugador();
        j.setIdGoogle("p1"); j.setTag("P1");
        jugadorRepository.save(j);

        Tema t = new Tema();
        t.setNombre("TemaTest");
        temaRepository.save(t);

        PalabraTema pt1 = new PalabraTema();
        pt1.setValor("Palabra1"); pt1.setTema(t);
        palabraTemaRepository.save(pt1);

        PalabraTema pt2 = new PalabraTema();
        pt2.setValor("Palabra2"); pt2.setTema(t);
        palabraTemaRepository.save(pt2);

        Partida p = new Partida();
        p.setCreador(j); p.setTema(t); p.setCodigoPartida("TAB1");
        partidaRepository.save(p);

        TableroCarta c1 = new TableroCarta();
        c1.setPartida(p); c1.setTipo(TipoCarta.rojo); c1.setEstado(EstadoCarta.oculta);
        c1.setFila(0); c1.setColumna(0); c1.setPalabra(pt1);
        
        TableroCarta c2 = new TableroCarta();
        c2.setPartida(p); c2.setTipo(TipoCarta.rojo); c2.setEstado(EstadoCarta.revelada);
        c2.setFila(0); c2.setColumna(1); c2.setPalabra(pt2);

        tableroCartaRepository.saveAll(List.of(c1, c2));

        long ocultasRojo = tableroCartaRepository.countByPartida_IdPartidaAndTipoAndEstado(p.getIdPartida(), TipoCarta.rojo, EstadoCarta.oculta);
        assertThat(ocultasRojo).isEqualTo(1);
    }
}
