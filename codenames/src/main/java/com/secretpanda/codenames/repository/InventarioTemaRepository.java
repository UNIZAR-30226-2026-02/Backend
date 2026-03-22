package com.secretpanda.codenames.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.secretpanda.codenames.model.InventarioTema;
import com.secretpanda.codenames.model.InventarioTemaId;

@Repository
public interface InventarioTemaRepository extends JpaRepository<InventarioTema, InventarioTemaId> {

    // Obtener todos los temas adquiridos por un jugador
    List<InventarioTema> findById_IdJugador(String idJugador);

    // Comprobar si un jugador ya posee un tema concreto (para validar compra y acceso a partidas)
    boolean existsById_IdJugadorAndId_IdTema(String idJugador, Integer idTema);
}