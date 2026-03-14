package com.secretpanda.codenames.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.secretpanda.codenames.model.TableroCarta;
import com.secretpanda.codenames.model.TableroCarta.EstadoCarta;
import com.secretpanda.codenames.model.TableroCarta.TipoCarta;

@Repository
public interface TableroCartaRepository extends JpaRepository<TableroCarta, Integer> {
    
    // Obtener todo el tablero de una partida específica
    List<TableroCarta> findByPartida_IdPartida(Integer idPartida);
    
    // Obtener una carta específica por sus coordenadas
    Optional<TableroCarta> findByPartida_IdPartidaAndFilaAndColumna(Integer idPartida, Integer fila, Integer columna);

    // Obtener cartas filtradas por estado (Útil para traer solo las REVELADAS o solo las OCULTAS)
    List<TableroCarta> findByPartida_IdPartidaAndEstado(Integer idPartida, EstadoCarta estado);

    // Numero de cartas de un tipo y estado concreto para validar victoria o derrota
    long countByPartida_IdPartidaAndTipoAndEstado(Integer idPartida, TipoCarta tipo, EstadoCarta estado);

    // Validar si una carta está en un estado específico
    boolean existsByIdCartaTableroAndEstado(Integer idCartaTablero, EstadoCarta estado);

    // Obtener cartas de un tipo concreto en una partida
    List<TableroCarta> findByPartida_IdPartidaAndTipo(Integer idPartida, TipoCarta tipo);
}