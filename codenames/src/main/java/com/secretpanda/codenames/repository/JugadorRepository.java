package com.secretpanda.codenames.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.secretpanda.codenames.model.Jugador;

@Repository 
public interface JugadorRepository extends JpaRepository<Jugador, String> {
    
    // Comprobar si un tag ya está en uso (para validar en el registro)
    boolean existsByTag(String tag); 

    // Buscar a un jugador por su tag
    Optional<Jugador> findByTag(String tag);

    // Obtener el Ranking Global Dinámico
    List<Jugador> findByOrderByVictoriasDesc(Pageable pageable);
    
    // Ranking dinámico por partidas jugadas
    List<Jugador> findByOrderByPartidasJugadasDesc(Pageable pageable);
}