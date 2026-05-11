package com.secretpanda.codenames.Integracion;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.stomp.StompSession;

import com.secretpanda.codenames.Integracion.config.IntegrationTestBase;
import com.secretpanda.codenames.Integracion.config.StompTestClient;
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

public class JuegoWebSocketIT extends IntegrationTestBase {

    @Autowired private JugadorRepository jugadorRepository;
    @Autowired private PartidaRepository partidaRepository;
    @Autowired private TemaRepository temaRepository;
    @Autowired private PalabraTemaRepository palabraTemaRepository;
    @Autowired private JugadorPartidaRepository jugadorPartidaRepository;

    @Test
    void testInicializarPartida_DebeRepartirCartasYNotificar() throws Exception {
        // GIVEN: 4 jugadores y una partida en lobby
        setupTemaBasico();

        for (int i = 0; i < 20; i++) {
            PalabraTema p = new PalabraTema();
            p.setValor("Palabra" + i);
            p.setTema(temaRepository.findById(1).get());
            p.setActivo(true);
            palabraTemaRepository.save(p);
        }

        List<Jugador> jugadores = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            Jugador j = new Jugador();
            j.setIdGoogle("j" + i);
            j.setTag("P" + i);
            j.setActivo(true);
            jugadorRepository.save(j);
            jugadores.add(j);
        }

        Partida partida = new Partida();
        partida.setCreador(jugadores.get(0));
        partida.setTema(temaRepository.findById(1).get());
        partida.setMaxJugadores(4);
        partida.setEstado(Partida.EstadoPartida.esperando);
        partida.setCodigoPartida("TESTW");
        partida = partidaRepository.save(partida);

        for (int i = 0; i < 4; i++) {
            JugadorPartida jp = new JugadorPartida();
            jp.setJugador(jugadores.get(i));
            jp.setPartida(partida);
            jp.setEquipo(i < 2 ? JugadorPartida.Equipo.rojo : JugadorPartida.Equipo.azul);
            jp.setRol(i % 2 == 0 ? JugadorPartida.Rol.lider : JugadorPartida.Rol.agente);
            jugadorPartidaRepository.save(jp);
        }

        StompTestClient client = new StompTestClient(port);
        List<StompSession> sessions = new ArrayList<>();
        List<BlockingQueue<GameStateDTO>> queues = new ArrayList<>();

        for (int i = 0; i < 4; i++) {
            String token = generateValidToken("j" + i);
            StompSession session = client.connect(token);
            sessions.add(session);
            queues.add(client.subscribe(session, "/user/queue/partidas/" + partida.getIdPartida() + "/estado", GameStateDTO.class));
        }

        // WHEN: El creador inicia la partida
        String tokenLider = generateValidToken("j0");
        restTemplate.getRestTemplate().getInterceptors().add((request, body, execution) -> {
            request.getHeaders().add("Authorization", "Bearer " + tokenLider);
            return execution.execute(request, body);
        });
        restTemplate.put("/api/partida/" + partida.getIdPartida() + "/iniciar", null);
        restTemplate.getRestTemplate().getInterceptors().clear();

        // THEN: Todos reciben el GameState
        for (int i = 0; i < 4; i++) {
            GameStateDTO state = queues.get(i).poll(5, TimeUnit.SECONDS);
            assertThat(state).isNotNull();
            assertThat(state.getTablero().getCartas()).hasSize(20);
            assertThat(state.getEstado()).isEqualTo("en_curso");
        }
    }

    @Test
    void testVotacionConcurrente_DebeRechazarSegundoVotoInvalido() throws Exception {
        // GIVEN: Una partida en curso con 2 agentes en el equipo rojo
        setupTemaBasico();

        for (int i = 0; i < 20; i++) {
            PalabraTema p = new PalabraTema();
            p.setValor("P" + i);
            p.setTema(temaRepository.findById(1).get());
            p.setActivo(true);
            palabraTemaRepository.save(p);
        }

        Jugador l1 = new Jugador(); l1.setIdGoogle("l1"); l1.setTag("L1"); l1.setActivo(true); jugadorRepository.save(l1);
        Jugador a1 = new Jugador(); a1.setIdGoogle("a1"); a1.setTag("A1"); a1.setActivo(true); jugadorRepository.save(a1);
        Jugador a2 = new Jugador(); a2.setIdGoogle("a2"); a2.setTag("A2"); a2.setActivo(true); jugadorRepository.save(a2);

        Partida partida = new Partida();
        partida.setCreador(l1);
        partida.setTema(temaRepository.findById(1).get());
        partida.setMaxJugadores(4);
        partida.setEstado(Partida.EstadoPartida.en_curso);
        partida.setCodigoPartida("VOTETEST");
        partida = partidaRepository.save(partida);

        JugadorPartida jpl1 = new JugadorPartida(); jpl1.setJugador(l1); jpl1.setPartida(partida); jpl1.setEquipo(JugadorPartida.Equipo.rojo); jpl1.setRol(JugadorPartida.Rol.lider); jugadorPartidaRepository.save(jpl1);
        JugadorPartida jpa1 = new JugadorPartida(); jpa1.setJugador(a1); jpa1.setPartida(partida); jpa1.setEquipo(JugadorPartida.Equipo.rojo); jpa1.setRol(JugadorPartida.Rol.agente); jugadorPartidaRepository.save(jpa1);
        JugadorPartida jpa2 = new JugadorPartida(); jpa2.setJugador(a2); jpa2.setPartida(partida); jpa2.setEquipo(JugadorPartida.Equipo.rojo); jpa2.setRol(JugadorPartida.Rol.agente); jugadorPartidaRepository.save(jpa2);

        // Generar tablero
        jdbcTemplate.execute("INSERT INTO tablero_carta (id_partida, fila, columna, id_palabra, tipo, estado) VALUES (" + 
            partida.getIdPartida() + ", 0, 0, (SELECT id_palabra FROM palabra_tema WHERE valor='P0'), 'rojo', 'oculta')");
        jdbcTemplate.execute("INSERT INTO tablero_carta (id_partida, fila, columna, id_palabra, tipo, estado) VALUES (" + 
            partida.getIdPartida() + ", 0, 1, (SELECT id_palabra FROM palabra_tema WHERE valor='P1'), 'rojo', 'oculta')");
        
        Integer idCarta0 = jdbcTemplate.queryForObject("SELECT id_carta_tablero FROM tablero_carta WHERE fila=0 AND columna=0", Integer.class);
        Integer idCarta1 = jdbcTemplate.queryForObject("SELECT id_carta_tablero FROM tablero_carta WHERE fila=0 AND columna=1", Integer.class);

        // Crear turno
        jdbcTemplate.execute("INSERT INTO turno (id_partida, id_jugador_partida, num_turno, palabra_pista, pista_numero, aciertos_turno) VALUES (" + 
            partida.getIdPartida() + ", " + jpl1.getIdJugadorPartida() + ", 1, 'pista', 2, 0)");
        
        StompTestClient client = new StompTestClient(port);
        StompSession s1 = client.connect(generateValidToken("a1"));
        StompSession s2 = client.connect(generateValidToken("a2"));
        
        BlockingQueue<GameStateDTO> q1 = client.subscribe(s1, "/user/queue/partidas/" + partida.getIdPartida() + "/estado", GameStateDTO.class);

        // WHEN: Ambos agentes votan a cartas distintas
        // Usamos records para el payload
        com.secretpanda.codenames.controller.JuegoController.VotarPayload v1 = new com.secretpanda.codenames.controller.JuegoController.VotarPayload(idCarta0, null);
        com.secretpanda.codenames.controller.JuegoController.VotarPayload v2 = new com.secretpanda.codenames.controller.JuegoController.VotarPayload(idCarta1, null);

        s1.send("/app/partidas/" + partida.getIdPartida() + "/votar", v1);
        s2.send("/app/partidas/" + partida.getIdPartida() + "/votar", v2);

        // THEN: Debería haber un empate (si llegan casi a la vez y el sistema no está bloqueado)
        // Pero en la realidad, uno llegará antes. Si el sistema es correcto, tras el 2º voto
        // se resolverá la votación. Si hay empate (2 cartas distintas), el equipo falla (RF-17).
        
        // Esperamos el broadcast
        GameStateDTO finalState = null;
        for(int i=0; i<3; i++) { // Puede haber varios broadcasts (uno por voto, uno por resolución)
             GameStateDTO s = q1.poll(2, TimeUnit.SECONDS);
             if (s != null) finalState = s;
        }

        assertThat(finalState).isNotNull();
        // Según JuegoService.resolverVotacion (RF-17), si hay empate se pierde el turno
        // En nuestro caso, a1 vota carta0, a2 vota carta1 -> EMPATE
        // Verificamos que ninguna se haya revelado en BD
        long reveladas = jdbcTemplate.queryForObject("SELECT count(*) FROM tablero_carta WHERE estado='revelada'", Long.class);
        assertThat(reveladas).isZero();
    }
}
