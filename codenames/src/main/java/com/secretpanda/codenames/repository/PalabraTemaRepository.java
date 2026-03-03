package com.secretpanda.codenames.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.secretpanda.codenames.model.PalabraTema;

@Repository
public interface PalabraTemaRepository extends JpaRepository<PalabraTema, Integer> {
    
    // Obtener todas las palabras de un tema específico
    List<PalabraTema> findByTema_IdTema(Integer idTema);
    
    // Obtener solo las palabras activas de un tema específico (para generar el tablero del juego)
    List<PalabraTema> findByTema_IdTemaAndActivoTrue(Integer idTema);
}