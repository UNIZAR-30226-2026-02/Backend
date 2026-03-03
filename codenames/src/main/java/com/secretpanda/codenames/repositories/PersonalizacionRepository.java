package com.secretpanda.codenames.repositories;

import com.secretpanda.codenames.models.Personalizacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PersonalizacionRepository extends JpaRepository<Personalizacion, Integer> {
    
    // Obtener todas las personalizaciones disponibles en la tienda
    List<Personalizacion> findByActivoTrue();
    
    // Filtrar la tienda por tipo (ej. ver solo los "tableros" o solo las "cartas")
    List<Personalizacion> findByTipoAndActivoTrue(String tipo);
    
    boolean existsByNombre(String nombre);
}