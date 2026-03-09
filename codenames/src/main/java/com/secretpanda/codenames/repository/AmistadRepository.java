package com.secretpanda.codenames.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.secretpanda.codenames.model.Amistad;
import com.secretpanda.codenames.model.AmistadId;

@Repository
public interface AmistadRepository extends JpaRepository<Amistad, AmistadId> {
    
    // OBTENER TODAS
    List<Amistad> findById_IdSolicitante(String idSolicitante);
    List<Amistad> findById_IdReceptor(String idReceptor);

    // OBTENER FILTRADAS POR ESTADOç
    // Lista de amigos confirmados (Para mostrar en la sección de amigos)
    List<Amistad> findById_IdSolicitanteAndEstado(String idSolicitante, Amistad.EstadoAmistad estado);
    
    // Lista de solicitudes pendientes que tengo que aceptar o rechazar
    List<Amistad> findById_IdReceptorAndEstado(String idReceptor, Amistad.EstadoAmistad estado);
}