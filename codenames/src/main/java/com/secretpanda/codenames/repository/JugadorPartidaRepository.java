package com.secretpanda.codenames.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.secretpanda.codenames.model.JugadorPartida;
import com.secretpanda.codenames.model.JugadorPartida.Equipo;
import com.secretpanda.codenames.model.JugadorPartida.Rol;

@Repository
public interface JugadorPartidaRepository extends JpaRepository<JugadorPartida, Integer> {
    
    // Obtener todas las participaciones de un jugador concreto
    List<JugadorPartida> findByJugador_IdGoogle(String idGoogle);

    // Obtener todos los jugadores en una partida concreta
    List<JugadorPartida> findByPartida_IdPartida(Integer idPartida);
    
    // Obtener todos los jugadores de un equipo concreto en una partida concreta
    List<JugadorPartida> findByPartida_IdPartidaAndEquipo(Integer idPartida, Equipo equipo);
    
    // Historial de partidas en las que ha estado un jugador (Ordenadas de más reciente a más antigua)
    List<JugadorPartida> findByJugador_IdGoogleOrderByPartida_FechaCreacionDesc(String idGoogle);
    
    // Buscar el registro exacto de un jugador en una partida
    Optional<JugadorPartida> findByJugador_IdGoogleAndPartida_IdPartida(String idGoogle, Integer idPartida);
    
    // Comprobar si ya está en la partida (Para validaciones antes de unirse)
    boolean existsByJugador_IdGoogleAndPartida_IdPartida(String idGoogle, Integer idPartida);

    // Buscar jugador con un rol específico en un equipo específico dentro de una partida
    Optional<JugadorPartida> findByPartida_IdPartidaAndEquipoAndRol(Integer idPartida, Equipo equipo, Rol rol);
}