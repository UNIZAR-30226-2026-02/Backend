package com.secretpanda.codenames.Integracion;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.secretpanda.codenames.Integracion.config.IntegrationTestBase;
import com.secretpanda.codenames.dto.social.AmistadDTO;
import com.secretpanda.codenames.dto.social.RankingDTO;
import com.secretpanda.codenames.model.Jugador;
import com.secretpanda.codenames.repository.JugadorRepository;

public class AmistadIntegrationIT extends IntegrationTestBase {

    @Autowired private JugadorRepository jugadorRepository;

    @Test
    void testFlujoAmistadCompleto() {
        // GIVEN: 2 jugadores
        Jugador j1 = new Jugador(); j1.setIdGoogle("j1"); j1.setTag("Panda1"); j1.setActivo(true); jugadorRepository.save(j1);
        Jugador j2 = new Jugador(); j2.setIdGoogle("j2"); j2.setTag("Panda2"); j2.setActivo(true); jugadorRepository.save(j2);

        String token1 = generateValidToken("j1");
        String token2 = generateValidToken("j2");

        // 1. j1 envía solicitud a j2
        HttpHeaders h1 = new HttpHeaders(); h1.setBearerAuth(token1);
        restTemplate.postForEntity("/api/amigos/solicitudes", new HttpEntity<>(Map.of("tag_receptor", "Panda2"), h1), Void.class);

        // 2. j2 verifica solicitudes pendientes
        HttpHeaders h2 = new HttpHeaders(); h2.setBearerAuth(token2);
        ResponseEntity<List<AmistadDTO>> resPendientes = restTemplate.exchange(
            "/api/amigos/solicitudes", 
            HttpMethod.GET, 
            new HttpEntity<>(h2), 
            new ParameterizedTypeReference<List<AmistadDTO>>() {}
        );
        assertThat(resPendientes.getBody()).isNotNull();
        assertThat(resPendientes.getBody()).hasSize(1);
        assertThat(resPendientes.getBody().get(0).getTagSolicitante()).isEqualTo("Panda1");

        // 3. j2 acepta solicitud
        restTemplate.put("/api/amigos/solicitudes", new HttpEntity<>(Map.of("id_solicitante", "j1", "estado", "aceptada"), h2), Void.class);

        // 4. Ambos verifican lista de amigos
        ResponseEntity<List<RankingDTO>> resAmigos1 = restTemplate.exchange(
            "/api/amigos", HttpMethod.GET, new HttpEntity<>(h1), new ParameterizedTypeReference<List<RankingDTO>>() {}
        );
        assertThat(resAmigos1.getBody()).isNotNull();
        assertThat(resAmigos1.getBody()).hasSize(1);
        assertThat(resAmigos1.getBody().get(0).getTag()).isEqualTo("Panda2");

        ResponseEntity<List<RankingDTO>> resAmigos2 = restTemplate.exchange(
            "/api/amigos", HttpMethod.GET, new HttpEntity<>(h2), new ParameterizedTypeReference<List<RankingDTO>>() {}
        );
        assertThat(resAmigos2.getBody()).isNotNull();
        assertThat(resAmigos2.getBody()).hasSize(1);
        assertThat(resAmigos2.getBody().get(0).getTag()).isEqualTo("Panda1");
    }

    @Test
    void testSolicitudAMismo_DebeFallar() {
        Jugador j1 = new Jugador(); j1.setIdGoogle("jx"); j1.setTag("PX"); j1.setActivo(true); jugadorRepository.save(j1);
        String token = generateValidToken("jx");
        HttpHeaders h = new HttpHeaders(); h.setBearerAuth(token);
        
        ResponseEntity<Void> res = restTemplate.postForEntity("/api/amigos/solicitudes", new HttpEntity<>(Map.of("tag_receptor", "PX"), h), Void.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
