package com.secretpanda.codenames.repositories;

import com.secretpanda.codenames.models.Amistad;
import com.secretpanda.codenames.models.AmistadId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
// Observa cómo el tipo de la clave primaria ahora es AmistadId en lugar de un Integer o String
public interface AmistadRepository extends JpaRepository<Amistad, AmistadId> {
    
    // Encuentra todas las solicitudes enviadas o recibidas por un jugador
    List<Amistad> findById_IdSolicitante(String idSolicitante);
    List<Amistad> findById_IdReceptor(String idReceptor);
}