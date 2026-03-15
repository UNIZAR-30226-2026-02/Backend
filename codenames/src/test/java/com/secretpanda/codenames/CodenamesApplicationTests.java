package com.secretpanda.codenames;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.secretpanda.codenames.model.Amistad;
import com.secretpanda.codenames.model.AmistadId;
import com.secretpanda.codenames.model.Jugador;
import com.secretpanda.codenames.model.JugadorPartida;
import com.secretpanda.codenames.model.PalabraTema;
import com.secretpanda.codenames.model.Partida;
import com.secretpanda.codenames.model.TableroCarta;
import com.secretpanda.codenames.model.Tema;
import com.secretpanda.codenames.repository.AmistadRepository;
import com.secretpanda.codenames.repository.JugadorPartidaRepository;
import com.secretpanda.codenames.repository.JugadorRepository;
import com.secretpanda.codenames.repository.PalabraTemaRepository;
import com.secretpanda.codenames.repository.PartidaRepository;
import com.secretpanda.codenames.repository.TableroCartaRepository;
import com.secretpanda.codenames.repository.TemaRepository;

@SpringBootTest
class CodenamesApplicationTests {

    @Autowired private JugadorRepository jugadorRepository;
    @Autowired private TemaRepository temaRepository;
    @Autowired private PalabraTemaRepository palabraTemaRepository;
    @Autowired private PartidaRepository partidaRepository;
    @Autowired private TableroCartaRepository tableroCartaRepository;
    @Autowired private AmistadRepository amistadRepository;
    @Autowired private JugadorPartidaRepository jugadorPartidaRepository;

    @Test
    @Transactional
    void testSistemaCompleto_DesdeUsuarioHastaTablero() {
        System.out.println("\n🕵️‍♂️ INICIANDO TEST DE INTEGRACIÓN END-TO-END");

        // 1. JUGADORES
        Jugador p1 = new Jugador();
        p1.setIdGoogle("google_panda_01");
        p1.setTag("PandaMaster");
        p1.setBalas(100);

        Jugador p2 = new Jugador();
        p2.setIdGoogle("google_ninja_02");
        p2.setTag("NinjaCoder");
        
        jugadorRepository.saveAll(List.of(p1, p2));
        System.out.println("✅ Jugadores creados.");

        // 2. SISTEMA SOCIAL (Amistad)
        AmistadId idAmistad = new AmistadId(p1.getIdGoogle(), p2.getIdGoogle());
        Amistad relacion = new Amistad();
        relacion.setId(idAmistad);
        relacion.setSolicitante(p1);
        relacion.setReceptor(p2);
        relacion.setEstado(Amistad.EstadoAmistad.aceptada);
        
        amistadRepository.save(relacion);
        System.out.println("✅ Relación de amistad guardada.");

        // 3. CONTENIDO DEL JUEGO (Tema y Palabra)
        Tema tema = new Tema();
        tema.setNombre("Ciberseguridad_" + System.currentTimeMillis());
        tema.setPrecioBalas(0);
        tema.setActivo(true);
        temaRepository.save(tema);

        PalabraTema palabra = new PalabraTema();
        palabra.setValor("Firewall");
        palabra.setTema(tema);
        palabra.setActivo(true);
        palabraTemaRepository.save(palabra);
        System.out.println("✅ Diccionario configurado.");

        // 4. PARTIDA
        Partida partida = new Partida();
        partida.setCodigoPartida("TEST-" + System.currentTimeMillis());
        partida.setEstado(Partida.EstadoPartida.esperando);
        partida.setCreador(p1);
        partida.setTema(tema);
        partida.setMaxJugadores(4);
        partida.setEsPublica(true);
        partidaRepository.save(partida);
        System.out.println("✅ Partida creada.");

        // 5. JUGADOR_PARTIDA (Unión a equipos)
        JugadorPartida jp1 = new JugadorPartida();
        jp1.setJugador(p1);
        jp1.setPartida(partida);
        jp1.setEquipo(JugadorPartida.Equipo.rojo);
        jp1.setRol(JugadorPartida.Rol.lider);
        jugadorPartidaRepository.save(jp1);
        System.out.println("✅ Jugador unido a partida.");

        // 6. GENERACIÓN DEL TABLERO (Uso de tu método exacto)
        TableroCarta cartaAsesino = new TableroCarta();
        cartaAsesino.setPartida(partida);
        cartaAsesino.setFila(0);
        cartaAsesino.setColumna(0);
        cartaAsesino.setPalabra(palabra);
        cartaAsesino.setTipo(TableroCarta.TipoCarta.asesino);
        cartaAsesino.setEstado(TableroCarta.EstadoCarta.oculta);
        tableroCartaRepository.save(cartaAsesino);

        // --- VALIDACIÓN USANDO TU MÉTODO countByPartida_IdPartidaAndTipoAndEstado ---
        long contador = tableroCartaRepository.countByPartida_IdPartidaAndTipoAndEstado(
            partida.getIdPartida(), 
            TableroCarta.TipoCarta.asesino, 
            TableroCarta.EstadoCarta.oculta
        );

        assertThat(contador).isEqualTo(1);
        System.out.println("✅ Validación con método countBy... exitosa.");

        System.out.println("\n🏆 ¡TEST DE INTEGRACIÓN COMPLETADO CON ÉXITO!");
    }
}