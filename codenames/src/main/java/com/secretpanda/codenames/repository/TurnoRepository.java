package com.secretpanda.codenames.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.secretpanda.codenames.model.JugadorPartida.Equipo;
import com.secretpanda.codenames.model.Turno;

@Repository
public interface TurnoRepository extends JpaRepository<Turno, Integer> {
    
    // Obtener todos los turnos de una partida ordenados cronológicamente
    List<Turno> findByPartida_IdPartidaOrderByNumTurnoAsc(Integer idPartida);
    
    // Buscar un turno específico de una partida
    Optional<Turno> findByPartida_IdPartidaAndNumTurno(Integer idPartida, Integer numTurno);

    // Obtener el turno actual de la partida
    Optional<Turno> findFirstByPartida_IdPartidaOrderByNumTurnoDesc(Integer idPartida);

    // Historial de turnos filtrado por equipo del jugador que dio la pista
    List<Turno> findByPartida_IdPartidaAndJugadorPartida_Equipo(Integer idPartida, Equipo equipo);

    // Historial de turnos filtrado por equipo del jugador que dio la pista
    List<Turno> findByPartida_IdPartidaAndEquipo(Integer idPartida, Equipo equipo);
}