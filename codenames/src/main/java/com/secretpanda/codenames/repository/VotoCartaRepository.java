package com.secretpanda.codenames.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.secretpanda.codenames.model.VotoCarta;

@Repository
public interface VotoCartaRepository extends JpaRepository<VotoCarta, Integer> {
    
    // Obtener todos los votos del turno actual (Para que todos vean las fichas de los demás)
    List<VotoCarta> findByTurno_IdTurno(Integer idTurno);
    
    // Historial de votos sobre una carta específica
    List<VotoCarta> findByCartaTablero_IdCartaTablero(Integer idCartaTablero);

    // ¿El jugador "X" ha votado ya por una carta en este turno? (Para evitar votos múltiples)
    Optional<VotoCarta> findByTurno_IdTurnoAndJugador_IdGoogle(Integer idTurno, String idJugador);

    // Devuelve el número de votos que ha recibido una carta en un turno concreto
    long countByTurno_IdTurnoAndCartaTablero_IdCartaTablero(Integer idTurno, Integer idCartaTablero);
}