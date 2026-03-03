package com.secretpanda.codenames.repositories;

import com.secretpanda.codenames.models.Logro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LogroRepository extends JpaRepository<Logro, Integer> {
    
    // Método extra para obtener solo los logros que están activos
    List<Logro> findByActivoTrue();
    
    boolean existsByNombre(String nombre);
}