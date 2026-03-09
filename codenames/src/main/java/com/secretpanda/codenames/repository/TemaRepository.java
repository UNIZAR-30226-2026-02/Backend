package com.secretpanda.codenames.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.secretpanda.codenames.model.Tema;

@Repository
public interface TemaRepository extends JpaRepository<Tema, Integer> {
    
    // Para mostrar a los jugadores a la hora de crear partida (Catálogo completo activo)
    List<Tema> findByActivoTrue();
    
    // Validar que no haya duplicados
    boolean existsByNombre(String nombre);

    // Permite buscar temas que contengan una palabra
    // El "Containing" genera un LIKE '%nombre%' en SQL automáticamente.
    List<Tema> findByNombreContainingIgnoreCaseAndActivoTrue(String nombre);
}