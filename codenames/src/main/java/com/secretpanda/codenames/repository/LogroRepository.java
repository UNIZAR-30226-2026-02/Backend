package com.secretpanda.codenames.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.secretpanda.codenames.model.Logro;

@Repository
public interface LogroRepository extends JpaRepository<Logro, Integer> {
    
    // Método extra para obtener solo los logros que están activos
    List<Logro> findByActivoTrue();
    
    // Método para validar que no haya dos logros con el mismo nombre
    boolean existsByNombre(String nombre);

    // Devuelve el catálogo filtrando por tipo y que estén activos.
    List<Logro> findByTipoAndActivoTrue(String tipo);

    // Devuelve el catálogo filtrando por la estadística clave que suma puntos y que estén activos
    List<Logro> findByEstadisticaClaveAndActivoTrue(String estadisticaClave);
}