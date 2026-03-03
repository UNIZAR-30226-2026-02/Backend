package com.secretpanda.codenames.repositories;

import com.secretpanda.codenames.models.Partida;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PartidaRepository extends JpaRepository<Partida, Integer> {
    
    Optional<Partida> findByCodigoPartida(String codigoPartida);
    
    List<Partida> findByEsPublicaTrueAndEstado(Partida.EstadoPartida estado);

    boolean existsByCodigoPartida(String codigoPartida);
}