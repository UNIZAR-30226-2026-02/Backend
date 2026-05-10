package com.secretpanda.codenames.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import com.secretpanda.codenames.model.Amistad;
import com.secretpanda.codenames.model.AmistadId;
import com.secretpanda.codenames.model.Jugador;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AmistadRepositoryTest {

    @Autowired private AmistadRepository amistadRepository;
    @Autowired private JugadorRepository jugadorRepository;

    @Test
    void testFindAmistadEntreJugadores_DebeDetectarBidireccional() {
        Jugador p1 = new Jugador(); p1.setIdGoogle("A"); p1.setTag("A");
        Jugador p2 = new Jugador(); p2.setIdGoogle("B"); p2.setTag("B");
        jugadorRepository.saveAll(List.of(p1, p2));

        AmistadId id = new AmistadId();
        id.setIdSolicitante("A");
        id.setIdReceptor("B");

        Amistad a = new Amistad();
        a.setId(id);
        a.setSolicitante(p1);
        a.setReceptor(p2);
        a.setEstado(Amistad.EstadoAmistad.aceptada);
        amistadRepository.save(a);

        Optional<Amistad> found1 = amistadRepository.findAmistadEntreJugadores("A", "B");
        assertThat(found1).isPresent();

        Optional<Amistad> found2 = amistadRepository.findAmistadEntreJugadores("B", "A");
        assertThat(found2).isPresent();
    }
}
