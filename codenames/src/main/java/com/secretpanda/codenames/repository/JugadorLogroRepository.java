package com.secretpanda.codenames.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.secretpanda.codenames.model.JugadorLogro;
import com.secretpanda.codenames.model.JugadorLogroId;

@Repository
public interface JugadorLogroRepository extends JpaRepository<JugadorLogro, JugadorLogroId> {
    
    // Obtener todos los logros de un jugador en específico
    List<JugadorLogro> findById_IdJugador(String idJugador);
    
    // Obtener solo los logros completados de un jugador
    List<JugadorLogro> findById_IdJugadorAndCompletadoTrue(String idJugador);
}