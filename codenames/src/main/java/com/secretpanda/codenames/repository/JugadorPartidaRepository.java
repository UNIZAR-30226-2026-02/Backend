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
    
    // Obtener el historial de partidas de un jugador ordenado por fecha de finalización (más reciente primero)
    List<JugadorPartida> findByJugador_IdGoogleOrderByPartida_FechaFinDesc(String idGoogle);
    
    // Buscar el registro exacto de un jugador en una partida
    Optional<JugadorPartida> findByJugador_IdGoogleAndPartida_IdPartida(String idGoogle, Integer idPartida);
    
    // Comprobar si ya está en la partida (Para validaciones antes de unirse)
    boolean existsByJugador_IdGoogleAndPartida_IdPartida(String idGoogle, Integer idPartida);

    // Buscar jugador con un rol específico en un equipo específico dentro de una partida
    Optional<JugadorPartida> findByPartida_IdPartidaAndEquipoAndRol(Integer idPartida, Equipo equipo, Rol rol);

    // Contar cuántos jugadores NO han abandonado la partida (Para verificar si se puede jugar)
    long countByPartida_IdPartidaAndAbandonoFalse(Integer idPartida);

    // Obtener la lista de jugadores que siguen activos en la partida (Los que no han abandonado)
    List<JugadorPartida> findByPartida_IdPartidaAndAbandonoFalse(Integer idPartida);

    // Comprobar si un jugador tiene alguna partida activa (esperando o en curso) y no ha abandonado
    boolean existsByJugador_IdGoogleAndPartida_EstadoInAndAbandonoFalse(String idGoogle, List<com.secretpanda.codenames.model.Partida.EstadoPartida> estados);

    // Buscar la primera participación activa de un jugador para el login/reconexión (soporta múltiples estados)
    Optional<JugadorPartida> findFirstByJugador_IdGoogleAndPartida_EstadoInAndAbandonoFalse(String idGoogle, List<com.secretpanda.codenames.model.Partida.EstadoPartida> estados);

    // Buscar todas las participaciones activas (sin abandono) de un jugador
    List<JugadorPartida> findByJugador_IdGoogleAndAbandonoFalse(String idGoogle);

    // Buscar la primera participación activa de un jugador para el login/reconexión
    Optional<JugadorPartida> findFirstByJugador_IdGoogleAndPartida_EstadoAndAbandonoFalse(String idGoogle, com.secretpanda.codenames.model.Partida.EstadoPartida estado);
}