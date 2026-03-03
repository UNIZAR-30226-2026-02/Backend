package com.secretpanda.codenames.repositories;

import com.secretpanda.codenames.models.JugadorLogro;
import com.secretpanda.codenames.models.JugadorLogroId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JugadorLogroRepository extends JpaRepository<JugadorLogro, JugadorLogroId> {
    
    // Obtener todos los logros de un jugador en específico
    List<JugadorLogro> findById_IdJugador(String idJugador);
    
    // Obtener solo los logros completados de un jugador
    List<JugadorLogro> findById_IdJugadorAndCompletadoTrue(String idJugador);
}