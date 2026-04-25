package com.secretpanda.codenames.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.secretpanda.codenames.model.VotoCarta;

@Repository
public interface VotoCartaRepository extends JpaRepository<VotoCarta, Integer> {
    
    // Obtener todos los votos del turno actual
    List<VotoCarta> findByTurno_IdTurno(Integer idTurno);
    
    // Obtener solo los votos activos (que aún no han resultado en una carta revelada) del turno actual
    List<VotoCarta> findByTurno_IdTurnoAndCartaReveladaIsNull(Integer idTurno);

    // Historial de votos sobre una carta específica
    List<VotoCarta> findByCartaTablero_IdCartaTablero(Integer idCartaTablero);

    // Buscar si el jugador ya ha votado en este turno (Para permitir cambiar el voto)
    Optional<VotoCarta> findByTurno_IdTurnoAndJugadorPartida_IdJugadorPartida(Integer idTurno, Integer idJugadorPartida);

    // Buscar el voto de un jugador específico en un turno y sobre una carta
    Optional<VotoCarta> findByTurno_IdTurnoAndJugadorPartida_IdJugadorPartidaAndCartaTablero_IdCartaTablero(Integer idTurno, Integer idJugadorPartida, Integer idCartaTablero);

    // Numero de votos que ha recibido una carta en un turno concreto
    long countByTurno_IdTurnoAndCartaTablero_IdCartaTablero(Integer idTurno, Integer idCartaTablero);
}