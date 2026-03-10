package com.secretpanda.codenames.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.secretpanda.codenames.model.Tema;

@Repository
public interface TemaRepository extends JpaRepository<Tema, Integer> {
    
    // Catálogo de temas activos para creación de partidas
    List<Tema> findByActivoTrue();
    
    // Validar duplicados por nombre
    boolean existsByNombre(String nombre);

    // Búsqueda de temas activos por coincidencia de nombre
    List<Tema> findByNombreContainingIgnoreCaseAndActivoTrue(String nombre);
}