package com.secretpanda.codenames.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.secretpanda.codenames.model.TableroCarta;

@Repository
public interface TableroCartaRepository extends JpaRepository<TableroCarta, Integer> {
    
    // Obtener todo el tablero de una partida específica
    List<TableroCarta> findByPartida_IdPartida(Integer idPartida);
    
    // Obtener una carta específica por sus coordenadas en la partida
    Optional<TableroCarta> findByPartida_IdPartidaAndFilaAndColumna(Integer idPartida, Integer fila, Integer columna);
}