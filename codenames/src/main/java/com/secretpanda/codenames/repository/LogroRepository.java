package com.secretpanda.codenames.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.secretpanda.codenames.model.Logro;

@Repository
public interface LogroRepository extends JpaRepository<Logro, Integer> {
    
    // Método extra para obtener solo los logros que están activos
    List<Logro> findByActivoTrue();
    
    boolean existsByNombre(String nombre);
}