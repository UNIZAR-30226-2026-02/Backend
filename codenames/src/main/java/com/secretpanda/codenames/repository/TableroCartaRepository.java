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
    
    // Obtener una carta específica por sus coordenadas
    Optional<TableroCarta> findByPartida_IdPartidaAndFilaAndColumna(Integer idPartida, Integer fila, Integer columna);

    // Numero de cartas de un tipo concreto en un estado concreto (ej. para validar si se ha revelado el ASESINO o cuántas cartas de un equipo quedan)
    long countByPartida_IdPartidaAndTipoAndEstado(Integer idPartida, String tipo, String estado);

    // Busca si una carta específica ya ha sido revelada antes de permitir un voto.
    // Evita que alguien vote dos veces a la misma carta o a una ya destapada.
    boolean existsByIdCartaTableroAndEstado(Integer idCartaTablero, String estado);

    // Obtener solo las cartas de un tipo concreto
    List<TableroCarta> findByPartida_IdPartidaAndTipo(Integer idPartida, String tipo);
}