package com.secretpanda.codenames.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.secretpanda.codenames.model.Turno;

@Repository
public interface TurnoRepository extends JpaRepository<Turno, Integer> {
    
    // Obtener todos los turnos de una partida, ordenados cronológicamente
    List<Turno> findByPartida_IdPartidaOrderByNumTurnoAsc(Integer idPartida);
    
    // Buscar un turno específico de una partida
    Optional<Turno> findByPartida_IdPartidaAndNumTurno(Integer idPartida, Integer numTurno);
}