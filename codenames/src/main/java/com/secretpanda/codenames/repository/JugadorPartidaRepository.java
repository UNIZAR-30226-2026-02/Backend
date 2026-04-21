package com.secretpanda.codenames.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.secretpanda.codenames.model.JugadorPartida;
import com.secretpanda.codenames.model.JugadorPartida.Equipo;
import com.secretpanda.codenames.model.Partida.EstadoPartida;

@Repository
public interface JugadorPartidaRepository extends JpaRepository<JugadorPartida, Integer> {

    List<JugadorPartida> findByPartida_IdPartida(Integer idPartida);

    List<JugadorPartida> findByPartida_IdPartidaAndAbandonoFalse(Integer idPartida);

    Optional<JugadorPartida> findByJugador_IdGoogleAndPartida_IdPartida(String idGoogle, Integer idPartida);

    boolean existsByJugador_IdGoogleAndPartida_IdPartida(String idGoogle, Integer idPartida);

    long countByPartida_IdPartidaAndAbandonoFalse(Integer idPartida);

    List<JugadorPartida> findByPartida_IdPartidaAndEquipo(Integer idPartida, Equipo equipo);

    long countByPartida_IdPartidaAndEquipoAndAbandonoFalse(Integer idPartida, Equipo equipo);

    boolean existsByJugador_IdGoogleAndPartida_EstadoInAndAbandonoFalse(String idGoogle, List<EstadoPartida> estados);

    Optional<JugadorPartida> findFirstByJugador_IdGoogleAndPartida_EstadoInAndAbandonoFalse(String idGoogle, List<EstadoPartida> estados);

    Optional<JugadorPartida> findByJugador_IdGoogleAndAbandonoFalse(String idGoogle);

    // Consulta optimizada para el historial con JOIN FETCH
    @Query("SELECT jp FROM JugadorPartida jp JOIN FETCH jp.partida p JOIN FETCH p.tema t WHERE jp.jugador.idGoogle = :idGoogle ORDER BY p.fechaFin DESC")
    List<JugadorPartida> findHistoryByJugadorId(@Param("idGoogle") String idGoogle);
}