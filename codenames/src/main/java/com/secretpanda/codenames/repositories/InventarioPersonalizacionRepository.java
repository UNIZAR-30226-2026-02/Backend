package com.secretpanda.codenames.repositories;

import com.secretpanda.codenames.models.InventarioPersonalizacion;
import com.secretpanda.codenames.models.InventarioPersonalizacionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventarioPersonalizacionRepository extends JpaRepository<InventarioPersonalizacion, InventarioPersonalizacionId> {
    
    // Obtener todo el inventario de un jugador
    List<InventarioPersonalizacion> findById_IdJugador(String idJugador);
    
    // Obtener solo lo que el jugador tiene equipado actualmente
    List<InventarioPersonalizacion> findById_IdJugadorAndEquipadoTrue(String idJugador);
}