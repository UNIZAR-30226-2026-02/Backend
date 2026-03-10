package com.secretpanda.codenames.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.secretpanda.codenames.model.Partida;

@Repository
public interface PartidaRepository extends JpaRepository<Partida, Integer> {
    
    // Buscar una partida por su código de invitación (Para unirse a partidas privadas)
    Optional<Partida> findByCodigoPartida(String codigoPartida);
    
    // Comprobar si un código ya existe (Para el generador de códigos aleatorios)
    boolean existsByCodigoPartida(String codigoPartida);

    // Mostrar salas públicas esperando jugadores, ordenadas de más nuevas a más antiguas
    List<Partida> findByEsPublicaTrueAndEstadoOrderByFechaCreacionDesc(Partida.EstadoPartida estado);

    // Mostrar salas públicas esperando jugadores de una temática concreta, ordenadas de más nuevas a más antiguas
    List<Partida> findByEsPublicaTrueAndEstado(Partida.EstadoPartida estado);

    // Mostrar salas públicas esperando jugadores de una temática concreta, ordenadas de más nuevas a más antiguas
    List<Partida> findByEsPublicaTrueAndEstadoAndTema_IdTemaOrderByFechaCreacionDesc(Partida.EstadoPartida estado, Integer idTema);

    // Buscar partidas antiguas que sigan en estado "ESPERANDO_JUGADORES" para cerrarlas automáticamente
    List<Partida> findByEstadoAndFechaCreacionBefore(Partida.EstadoPartida estado, LocalDateTime fechaLimite);
}