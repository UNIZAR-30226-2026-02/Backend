package com.secretpanda.codenames;

import static org.assertj.core.api.Assertions.assertThat;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import com.secretpanda.codenames.model.*;
import com.secretpanda.codenames.repository.*;

@SpringBootTest
class CodenamesApplicationTests {

    @Autowired private JugadorRepository jugadorRepository;
    @Autowired private TemaRepository temaRepository;
    @Autowired private PalabraTemaRepository palabraTemaRepository;
    @Autowired private PartidaRepository partidaRepository;
    @Autowired private TableroCartaRepository tableroCartaRepository;
    @Autowired private AmistadRepository amistadRepository;

    @Test
    @Transactional
    @Rollback(true) 
    void testFlujoIntegralSistema() {
        System.out.println("\n🚀 INICIANDO TEST DE INTEGRACIÓN COMPLETO");

        // 1. JUGADORES
        Jugador j1 = crearJugador("google_01", "panda@test.com", "PandaMaster");
        Jugador j2 = crearJugador("google_02", "ninja@test.com", "NinjaCoder");
        jugadorRepository.saveAll(List.of(j1, j2));

        // 2. AMISTAD
        AmistadId amistadId = new AmistadId(j1.getIdGoogle(), j2.getIdGoogle());
        Amistad amistad = new Amistad();
        amistad.setId(amistadId);
        amistad.setSolicitante(j1);
        amistad.setReceptor(j2);
        amistad.setEstado(Amistad.EstadoAmistad.ACEPTADA);
        amistadRepository.save(amistad);

        // 3. TEMA Y PALABRAS
        Tema temaTech = new Tema();
        temaTech.setNombre("Tecnología " + System.currentTimeMillis()); // Evitar duplicados
        temaTech.setPrecioBalas(0);
        temaTech.setActivo(true);
        temaRepository.save(temaTech);

        PalabraTema pt = new PalabraTema();
        pt.setValor("Java"); // Tu modelo usa 'valor'
        pt.setTema(temaTech);
        pt.setActivo(true);
        palabraTemaRepository.save(pt);

        // 4. PARTIDA
        Partida partida = new Partida();
        partida.setCodigoPartida("PANDA-" + System.currentTimeMillis());
        partida.setEstado(Partida.EstadoPartida.ESPERANDO);
        partida.setCreador(j1); // El modelo exige un creador
        partida.setTema(temaTech);
        partidaRepository.save(partida);

        // 5. TABLERO
        TableroCarta carta = new TableroCarta();
        carta.setPartida(partida);
        carta.setFila(0);
        carta.setColumna(0);
        carta.setPalabra(pt); // Tu modelo pide el objeto PalabraTema, no un String
        carta.setTipo("AZUL");
        tableroCartaRepository.save(carta);

        System.out.println("✅ TEST FINALIZADO CON ÉXITO");
    }

    private Jugador crearJugador(String id, String email, String tag) {
        Jugador j = new Jugador();
        j.setIdGoogle(id);
        j.setEmail(email);
        j.setTag(tag);
        return j;
    }
}