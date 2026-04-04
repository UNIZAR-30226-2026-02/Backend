package com.secretpanda.codenames.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.secretpanda.codenames.model.InventarioPersonalizacion;
import com.secretpanda.codenames.model.InventarioPersonalizacionId;
import com.secretpanda.codenames.model.Personalizacion;

@Repository
public interface InventarioPersonalizacionRepository extends JpaRepository<InventarioPersonalizacion, InventarioPersonalizacionId> {
    
    // Obtener todo el inventario de un jugador
    List<InventarioPersonalizacion> findById_IdJugador(String idJugador);
    
    // Obtener todo lo que el jugador tiene equipado actualmente (Tableros, cartas, etc.)
    List<InventarioPersonalizacion> findById_IdJugadorAndEquipadoTrue(String idJugador);

    // Obtener un ítem específico del inventario de un jugador, usando el ID de la personalización (no el ID del inventario)
    Optional<InventarioPersonalizacion> findById_IdJugadorAndId_IdPersonalizacion(String idJugador, Integer idPersonalizacion);

    // Obtener el elemento equipado de un TIPO específico
    Optional<InventarioPersonalizacion> findById_IdJugadorAndPersonalizacion_TipoAndEquipadoTrue(String idJugador, Personalizacion.TipoPersonalizacion tipo);
}