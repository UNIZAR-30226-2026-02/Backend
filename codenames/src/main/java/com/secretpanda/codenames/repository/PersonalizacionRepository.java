package com.secretpanda.codenames.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.secretpanda.codenames.model.Personalizacion;

@Repository
public interface PersonalizacionRepository extends JpaRepository<Personalizacion, Integer> {
    
    // Obtener todas las personalizaciones disponibles en la tienda
    List<Personalizacion> findByActivoTrue();
    
    // Filtrar la tienda por tipo (ej. ver solo los "tableros" o solo las "cartas")
    List<Personalizacion> findByTipoAndActivoTrue(String tipo);
    
    boolean existsByNombre(String nombre);
}