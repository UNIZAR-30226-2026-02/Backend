package com.secretpanda.codenames.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.secretpanda.codenames.model.Partida;

@Repository
public interface PartidaRepository extends JpaRepository<Partida, Integer> {
    
    // Buscar una partida por su código de invitación
    Optional<Partida> findByCodigoPartida(String codigoPartida);

    // Buscar una partida por código asegurando que esté en un estado concreto (Ej. ESPERANDO)
    Optional<Partida> findByCodigoPartidaAndEstado(String codigoPartida, Partida.EstadoPartida estado);
    
    // Comprobar si un código ya existe
    boolean existsByCodigoPartida(String codigoPartida);

    // Contar cuántas partidas hay en un estado determinado (Para métricas de carga del servidor)
    long countByEstado(Partida.EstadoPartida estado);

    // Mostrar salas públicas esperando jugadores ordenadas por fecha
    List<Partida> findByEsPublicaTrueAndEstadoOrderByFechaCreacionDesc(Partida.EstadoPartida estado);

    // Mostrar salas públicas esperando jugadores de una temática concreta
    List<Partida> findByEsPublicaTrueAndEstadoAndTema_IdTemaOrderByFechaCreacionDesc(Partida.EstadoPartida estado, Integer idTema);

    // Buscar partidas públicas con huecos libres para matchmaking
    @Query("SELECT p FROM Partida p WHERE p.esPublica = true AND p.estado = :estado " +
           "AND (SELECT COUNT(jp) FROM JugadorPartida jp WHERE jp.partida = p) < p.maxJugadores " +
           "ORDER BY p.fechaCreacion DESC")
    List<Partida> findPartidasPublicasDisponibles(@Param("estado") Partida.EstadoPartida estado);

    // Buscar partidas antiguas para cierre automático
    List<Partida> findByEstadoAndFechaCreacionBefore(Partida.EstadoPartida estado, LocalDateTime fechaLimite);
    
    // Listar partidas creadas por un jugador
    List<Partida> findByCreador_IdGoogle(String idGoogle);
}