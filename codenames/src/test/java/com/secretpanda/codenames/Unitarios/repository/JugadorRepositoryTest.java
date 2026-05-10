package com.secretpanda.codenames.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import com.secretpanda.codenames.model.Jugador;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class JugadorRepositoryTest {

    @Autowired
    private JugadorRepository jugadorRepository;

    @Test
    void testSaveAndFind_DebePersistirCorrectamente() {
        Jugador j = new Jugador();
        j.setIdGoogle("test_persistence_id");
        j.setTag("TestPersist");
        j.setBalas(50);
        j.setActivo(true);

        jugadorRepository.save(j);

        Optional<Jugador> found = jugadorRepository.findById("test_persistence_id");
        assertThat(found).isPresent();
        assertThat(found.get().getTag()).isEqualTo("TestPersist");
    }

    @Test
    void testExistsByTagAndActivoTrue_DebeDetectarDuplicados() {
        Jugador j = new Jugador();
        j.setIdGoogle("id_1");
        j.setTag("PandaRepetido");
        j.setActivo(true);
        jugadorRepository.save(j);

        boolean exists = jugadorRepository.existsByTagAndActivoTrue("PandaRepetido");
        assertThat(exists).isTrue();

        boolean notExists = jugadorRepository.existsByTagAndActivoTrue("Inexistente");
        assertThat(notExists).isFalse();
    }
}
