package com.secretpanda.codenames.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.secretpanda.codenames.model.Amistad;
import com.secretpanda.codenames.model.AmistadId;

@Repository
public interface AmistadRepository extends JpaRepository<Amistad, AmistadId> {
    
    // OBTENER TODAS
    List<Amistad> findById_IdSolicitante(String idSolicitante);
    List<Amistad> findById_IdReceptor(String idReceptor);

    // Lista de solicitudes pendientes que me han enviado y tengo que aceptar/rechazar
    List<Amistad> findById_IdReceptorAndEstado(String idReceptor, Amistad.EstadoAmistad estado);

    // Lista de solicitudes pendientes que yo he enviado (por si se quiere mostrar "Solicitudes enviadas")
    List<Amistad> findById_IdSolicitanteAndEstado(String idSolicitante, Amistad.EstadoAmistad estado);

    // Consulta óptima para obtener la lista real de amigos de un jugador
    @Query("SELECT a FROM Amistad a WHERE (a.id.idSolicitante = :idJugador OR a.id.idReceptor = :idJugador) AND a.estado = :estado")
    List<Amistad> findAmistadesPorJugadorYEstado(
        @Param("idJugador") String idJugador, 
        @Param("estado") Amistad.EstadoAmistad estado
    );

    // Obtener la relación entre dos jugadores concretos (sin importar quién la envió)
    @Query("SELECT a FROM Amistad a WHERE (a.id.idSolicitante = :jugador1 AND a.id.idReceptor = :jugador2) OR (a.id.idSolicitante = :jugador2 AND a.id.idReceptor = :jugador1)")
    Optional<Amistad> findAmistadEntreJugadores(
        @Param("jugador1") String jugador1, 
        @Param("jugador2") String jugador2
    );
}