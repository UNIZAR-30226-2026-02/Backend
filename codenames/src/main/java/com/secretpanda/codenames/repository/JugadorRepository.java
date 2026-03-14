package com.secretpanda.codenames.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.secretpanda.codenames.model.Jugador;

@Repository 
public interface JugadorRepository extends JpaRepository<Jugador, String> {
    
    // Comprobar si un tag ya está en uso
    boolean existsByTag(String tag); 

    // Buscar a un jugador por su tag
    Optional<Jugador> findByTag(String tag);

    // Buscador de jugadores por tag (Parcial) para encontrar amigos
    List<Jugador> findByTagContainingIgnoreCase(String tag, Pageable pageable);

    // Obtener el Ranking Global Dinámico por Victorias
    List<Jugador> findByOrderByVictoriasDesc(Pageable pageable);
    
    // Ranking dinámico por partidas jugadas
    List<Jugador> findByOrderByPartidasJugadasDesc(Pageable pageable);

    // Ranking dinámico por ratio de victorias
    List<Jugador> findByIdGoogleInOrderByVictoriasDesc(Collection<String> ids, Pageable pageable);

    // Búsqueda explícita por ID de Google
    Optional<Jugador> findByIdGoogle(String idGoogle);
}