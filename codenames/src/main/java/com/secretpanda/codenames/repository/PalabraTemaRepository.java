package com.secretpanda.codenames.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.secretpanda.codenames.model.PalabraTema;

@Repository
public interface PalabraTemaRepository extends JpaRepository<PalabraTema, Integer> {
    
    // Obtener todas las palabras de un tema específico
    List<PalabraTema> findByTema_IdTema(Integer idTema);
    
    // Obtener solo las palabras activas de un tema específico (para generar el tablero del juego)
    List<PalabraTema> findByTema_IdTemaAndActivoTrue(Integer idTema);

    // Validar que no exista ya la misma palabra dentro del mismo tema
    boolean existsByPalabraAndTema_IdTema(String palabra, Integer idTema);

    // MEJORAS DE RENDIMIENTO
    // Extrae un número X de palabras aleatorias directamente desde PostgreSQL.
    // Parametrizamos el LIMIT para que el Service decida cuántas cartas quiere
    @Query(value = "SELECT * FROM palabra_tema WHERE id_tema = :idTema AND activo = true ORDER BY RANDOM() LIMIT :cantidad", nativeQuery = true)
    List<PalabraTema> findPalabrasAleatoriasPorTema(@Param("idTema") Integer idTema, @Param("cantidad") Integer cantidad);
}