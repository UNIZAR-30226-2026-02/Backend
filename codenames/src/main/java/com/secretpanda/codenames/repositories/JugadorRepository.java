package com.secretpanda.codenames.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.secretpanda.codenames.models.Jugador;

@Repository // Indica que es un componente de acceso a datos
// JpaRepository recibe dos tipos: <La Entidad, El tipo de dato de su Clave Primaria (Id)>
public interface JugadorRepository extends JpaRepository<Jugador, String> {
    
    // Spring Boot genera la consulta automáticamente solo con nombrar bien el método
    boolean existsByTag(String tag); 
}