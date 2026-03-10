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
    @Rollback(true) // Cambia a false si quieres ver los datos en pgAdmin después
    void testFlujoIntegralSistema() {
        System.out.println("\n🚀 INICIANDO TEST DE INTEGRACIÓN COMPLETO - SECRET PANDA");
        System.out.println("===========================================================");

        // --- 1. CREACIÓN DE USUARIOS ---
        System.out.print(" -> Creando jugadores... ");
        Jugador j1 = crearJugador("google_01", "panda_master@gmail.com", "PandaMaster");
        Jugador j2 = crearJugador("google_02", "ninja_coder@gmail.com", "NinjaCoder");
        jugadorRepository.saveAll(List.of(j1, j2));
        System.out.println("✅ [" + jugadorRepository.count() + " jugadores en BD]");

        // --- 2. SISTEMA SOCIAL (AMISTAD) ---
        System.out.print(" -> Estableciendo amistad... ");
        AmistadId amistadId = new AmistadId(j1.getIdGoogle(), j2.getIdGoogle());
        Amistad amistad = new Amistad();
        amistad.setId(amistadId);
        // Suponiendo que tienes estos campos en tu modelo
        // amistad.setEstado(Amistad.EstadoAmistad.ACEPTADA); 
        // amistad.setFechaAmistad(LocalDateTime.now());
        amistadRepository.save(amistad);
        System.out.println("✅ [Relación creada]");

        // --- 3. CONFIGURACIÓN DEL TEMA Y PALABRAS ---
        System.out.print(" -> Configurando tema 'Tecnología'... ");
        Tema temaTech = new Tema();
        temaTech.setNombre("Tecnología");
        temaTech.setPrecioBalas(0);
        temaTech.setActivo(true);
        temaRepository.save(temaTech);

        String[] palabras = {"Java", "Python", "Docker", "Spring", "Database", "Cloud", "Git", "Linux"};
        for (String p : palabras) {
            PalabraTema pt = new PalabraTema();
            pt.setNombrePalabra(p);
            pt.setTema(temaTech);
            pt.setActivo(true);
            palabraTemaRepository.save(pt);
        }
        System.out.println("✅ [" + palabras.length + " palabras insertadas]");

        // --- 4. CREACIÓN DE PARTIDA ---
        System.out.print(" -> Creando sala de juego... ");
        Partida partida = new Partida();
        partida.setCodigoPartida("PANDA-2026");
        partida.setEstado(Partida.EstadoPartida.ESPERANDO);
        partida.setEsPublica(true);
        partida.setTema(temaTech);
        partida.setFechaCreacion(LocalDateTime.now());
        partidaRepository.save(partida);
        System.out.println("✅ [Código: PANDA-2026]");

        // --- 5. GENERACIÓN DE TABLERO (Muestra de 4 cartas) ---
        System.out.print(" -> Generando cartas del tablero... ");
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                TableroCarta carta = new TableroCarta();
                carta.setPartida(partida);
                carta.setFila(i);
                carta.setColumna(j);
                carta.setPalabra("Palabra_" + i + "_" + j);
                carta.setTipo("AZUL"); // Rojo, Azul, Civil, Asesino
                carta.setEstado("OCULTA");
                tableroCartaRepository.save(carta);
            }
        }
        System.out.println("✅ [Mini-tablero 2x2 creado]");

        // --- 6. VERIFICACIÓN FINAL (QUERIES COMBINADAS) ---
        System.out.println("-----------------------------------------------------------");
        System.out.println("🔍 VERIFICANDO INTEGRIDAD DE DATOS...");
        
        // Test: Buscar partida por código y ver si trae el tema
        Partida pCheck = partidaRepository.findByCodigoPartida("PANDA-2026").orElseThrow();
        assertThat(pCheck.getTema().getNombre()).isEqualTo("Tecnología");
        System.out.println(" OK: Relación Partida -> Tema verificada.");

        // Test: Contar cartas de la partida
        long cartasCount = tableroCartaRepository.findByPartida_IdPartida(pCheck.getIdPartida()).size();
        assertThat(cartasCount).isEqualTo(4);
        System.out.println(" OK: Conteo de cartas en tablero correcto.");

        // Test: Buscar amigos de J1
        List<Amistad> amigos = amistadRepository.findById_IdSolicitante(j1.getIdGoogle());
        assertThat(amigos).isNotEmpty();
        System.out.println(" OK: Sistema de amigos funcional.");

        System.out.println("===========================================================");
        System.out.println("🎉 ¡TEST FINALIZADO CON ÉXITO! El esquema es sólido.");
    }

    private Jugador crearJugador(String id, String email, String tag) {
        Jugador j = new Jugador();
        j.setIdGoogle(id);
        j.setEmail(email);
        j.setTag(tag);
        j.setBalas(100);
        j.setVictorias(0);
        j.setPartidasJugadas(0);
        return j;
    }
}