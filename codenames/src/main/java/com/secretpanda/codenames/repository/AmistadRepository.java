package com.secretpanda.codenames.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.secretpanda.codenames.model.Amistad;
import com.secretpanda.codenames.model.AmistadId;

@Repository
// Observa cómo el tipo de la clave primaria ahora es AmistadId en lugar de un Integer o String
public interface AmistadRepository extends JpaRepository<Amistad, AmistadId> {
    
    // Encuentra todas las solicitudes enviadas o recibidas por un jugador
    List<Amistad> findById_IdSolicitante(String idSolicitante);
    List<Amistad> findById_IdReceptor(String idReceptor);
}