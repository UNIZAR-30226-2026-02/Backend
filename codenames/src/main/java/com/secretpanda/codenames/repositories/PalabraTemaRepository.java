package com.secretpanda.codenames.repositories;

import com.secretpanda.codenames.models.PalabraTema;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PalabraTemaRepository extends JpaRepository<PalabraTema, Integer> {
    
    // Obtener todas las palabras de un tema específico
    List<PalabraTema> findByTema_IdTema(Integer idTema);
    
    // Obtener solo las palabras activas de un tema específico (para generar el tablero del juego)
    List<PalabraTema> findByTema_IdTemaAndActivoTrue(Integer idTema);
}