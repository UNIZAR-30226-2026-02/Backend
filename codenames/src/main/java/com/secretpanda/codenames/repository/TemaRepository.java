package com.secretpanda.codenames.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.secretpanda.codenames.model.Tema;

@Repository
public interface TemaRepository extends JpaRepository<Tema, Integer> {
    
    // Catálogo de temas activos para creación de partidas
    List<Tema> findByActivoTrue();
    
    // Validar duplicados por nombre
    boolean existsByNombre(String nombre);

    // Buscar un tema concreto por su nombre exacto
    Optional<Tema> findByNombre(String nombre);

    // Búsqueda de temas activos por coincidencia de nombre (parcial)
    List<Tema> findByNombreContainingIgnoreCaseAndActivoTrue(String nombre);
}