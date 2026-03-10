package com.secretpanda.codenames.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.secretpanda.codenames.model.JugadorLogro;
import com.secretpanda.codenames.model.JugadorLogroId;
import com.secretpanda.codenames.model.Logro;

@Repository
public interface JugadorLogroRepository extends JpaRepository<JugadorLogro, JugadorLogroId> {
    
    // Obtener todos los logros de un jugador en específico
    List<JugadorLogro> findById_IdJugador(String idJugador);
    
    // Obtener solo los logros completados de un jugador
    List<JugadorLogro> findById_IdJugadorAndCompletadoTrue(String idJugador);

    // Trae solo los que aún están a medias, para sumarles puntos.
    List<JugadorLogro> findById_IdJugadorAndCompletadoFalse(String idJugador);

    // Navega automáticamente a la tabla Logro y filtra por la columna 'tipo'
    List<JugadorLogro> findById_IdJugadorAndLogro_Tipo(String idJugador, Logro.TipoLogro tipo);
}