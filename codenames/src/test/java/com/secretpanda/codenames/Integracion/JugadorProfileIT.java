package com.secretpanda.codenames.Integracion;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.secretpanda.codenames.Integracion.config.IntegrationTestBase;
import com.secretpanda.codenames.dto.jugador.ActualizarPerfilDTO;
import com.secretpanda.codenames.dto.jugador.JugadorDTO;
import com.secretpanda.codenames.dto.jugador.TemaInventarioDTO;
import com.secretpanda.codenames.model.InventarioTema;
import com.secretpanda.codenames.model.InventarioTemaId;
import com.secretpanda.codenames.model.Jugador;
import com.secretpanda.codenames.model.Tema;
import com.secretpanda.codenames.repository.InventarioTemaRepository;
import com.secretpanda.codenames.repository.JugadorRepository;
import com.secretpanda.codenames.repository.TemaRepository;

public class JugadorProfileIT extends IntegrationTestBase {

    @Autowired private JugadorRepository jugadorRepository;
    @Autowired private TemaRepository temaRepository;
    @Autowired private InventarioTemaRepository inventarioTemaRepository;

    @Test
    void testActualizarPerfil_TagDuplicado_DebeFallar() {
        // GIVEN: 2 jugadores
        Jugador j1 = new Jugador(); j1.setIdGoogle("j1"); j1.setTag("Panda1"); j1.setActivo(true); jugadorRepository.save(j1);
        Jugador j2 = new Jugador(); j2.setIdGoogle("j2"); j2.setTag("Panda2"); j2.setActivo(true); jugadorRepository.save(j2);

        String token1 = generateValidToken("j1");
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token1);

        ActualizarPerfilDTO update = new ActualizarPerfilDTO();
        update.setTag("Panda2");

        // WHEN
        ResponseEntity<String> res = restTemplate.exchange(
            "/api/jugadores", 
            HttpMethod.PUT, 
            new HttpEntity<>(update, headers), 
            String.class
        );

        // THEN
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void testGetTemas_DebeListarAdquiridos() {
        // GIVEN: Jugador con un tema extra
        setupTemaBasico();
        Jugador j1 = new Jugador(); j1.setIdGoogle("j1"); j1.setTag("P1"); j1.setActivo(true); jugadorRepository.save(j1);
        
        jdbcTemplate.execute("INSERT INTO tema (id_tema, nombre, precio_balas, activo) VALUES (2, 'Cine', 0, true)");
        Tema t1 = temaRepository.findById(1).get();
        Tema t2 = temaRepository.findById(2).get();

        InventarioTema it1 = new InventarioTema();
        InventarioTemaId id1 = new InventarioTemaId();
        id1.setIdJugador("j1"); id1.setIdTema(1);
        it1.setId(id1);
        it1.setJugador(j1); it1.setTema(t1);
        
        InventarioTema it2 = new InventarioTema();
        InventarioTemaId id2 = new InventarioTemaId();
        id2.setIdJugador("j1"); id2.setIdTema(2);
        it2.setId(id2);
        it2.setJugador(j1); it2.setTema(t2);

        inventarioTemaRepository.saveAll(List.of(it1, it2));

        String token = generateValidToken("j1");
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        // WHEN
        ResponseEntity<List<TemaInventarioDTO>> res = restTemplate.exchange(
            "/api/jugadores/temas", 
            HttpMethod.GET, 
            new HttpEntity<>(headers), 
            new ParameterizedTypeReference<List<TemaInventarioDTO>>() {}
        );

        // THEN
        assertThat(res.getBody()).hasSize(2);
        assertThat(res.getBody().stream().map(TemaInventarioDTO::getNombre)).containsExactlyInAnyOrder("Básico", "Cine");
    }
}
