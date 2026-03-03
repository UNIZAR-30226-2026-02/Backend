package com.secretpanda.codenames.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.secretpanda.codenames.model.JugadorPartida;

@Repository
public interface JugadorPartidaRepository extends JpaRepository<JugadorPartida, Integer> {
    
    // Obtener todos los jugadores en una partida concreta
    List<JugadorPartida> findByPartida_IdPartida(Integer idPartida);
    
    // Obtener los jugadores de una partida filtrados por su equipo
    List<JugadorPartida> findByPartida_IdPartidaAndEquipo(Integer idPartida, String equipo);
    
    // Historial de partidas en las que ha estado un jugador
    List<JugadorPartida> findByJugador_IdGoogle(String idJugador);
    
    // Buscar el registro exacto de un jugador en una partida
    Optional<JugadorPartida> findByJugador_IdGoogleAndPartida_IdPartida(String idJugador, Integer idPartida);
    
    // Comprobar si ya está en la partida
    boolean existsByJugador_IdGoogleAndPartida_IdPartida(String idJugador, Integer idPartida);
}