package com.secretpanda.codenames.Integracion;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import com.secretpanda.codenames.Integracion.config.IntegrationTestBase;
import com.secretpanda.codenames.dto.social.RankingDTO;
import com.secretpanda.codenames.model.Jugador;
import com.secretpanda.codenames.repository.JugadorRepository;

public class LeaderboardIntegrationIT extends IntegrationTestBase {

    @Autowired private JugadorRepository jugadorRepository;

    @Test
    void testRankingGlobal_OrdenacionCorrecta() {
        // GIVEN: 3 jugadores con distintos puntos
        Jugador j1 = new Jugador(); j1.setIdGoogle("j1"); j1.setTag("J1"); j1.setVictorias(10); j1.setNumAciertos(100); j1.setActivo(true);
        Jugador j2 = new Jugador(); j2.setIdGoogle("j2"); j2.setTag("J2"); j2.setVictorias(20); j2.setNumAciertos(50); j2.setActivo(true);
        Jugador j3 = new Jugador(); j3.setIdGoogle("j3"); j3.setTag("J3"); j3.setVictorias(20); j3.setNumAciertos(80); j3.setActivo(true);
        jugadorRepository.saveAll(List.of(j1, j2, j3));

        String token = generateValidToken("j1");
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.setBearerAuth(token);

        // WHEN
        ResponseEntity<List<RankingDTO>> res = restTemplate.exchange(
            "/api/leaderboard/global", 
            HttpMethod.GET, 
            new org.springframework.http.HttpEntity<>(headers), 
            new ParameterizedTypeReference<List<RankingDTO>>() {}
        );

        // THEN: J3 (20v, 80a) > J2 (20v, 50a) > J1 (10v, 100a)
        List<RankingDTO> ranking = res.getBody();
        assertThat(ranking).hasSize(3);
        assertThat(ranking.get(0).getTag()).isEqualTo("J3");
        assertThat(ranking.get(1).getTag()).isEqualTo("J2");
        assertThat(ranking.get(2).getTag()).isEqualTo("J1");
    }
}
