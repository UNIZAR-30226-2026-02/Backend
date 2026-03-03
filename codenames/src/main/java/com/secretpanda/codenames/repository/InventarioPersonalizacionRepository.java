package com.secretpanda.codenames.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.secretpanda.codenames.model.InventarioPersonalizacion;
import com.secretpanda.codenames.model.InventarioPersonalizacionId;

@Repository
public interface InventarioPersonalizacionRepository extends JpaRepository<InventarioPersonalizacion, InventarioPersonalizacionId> {
    
    // Obtener todo el inventario de un jugador
    List<InventarioPersonalizacion> findById_IdJugador(String idJugador);
    
    // Obtener solo lo que el jugador tiene equipado actualmente
    List<InventarioPersonalizacion> findById_IdJugadorAndEquipadoTrue(String idJugador);
}