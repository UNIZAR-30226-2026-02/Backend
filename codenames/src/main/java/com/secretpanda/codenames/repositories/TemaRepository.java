package com.secretpanda.codenames.repositories;

import com.secretpanda.codenames.models.Tema;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TemaRepository extends JpaRepository<Tema, Integer> {
    
    // Para mostrar a los jugadores a la hora de crear partida, solo los temas activos
    List<Tema> findByActivoTrue();
    
    boolean existsByNombre(String nombre);
}