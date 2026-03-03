package com.secretpanda.codenames.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.secretpanda.codenames.model.Partida;

@Repository
public interface PartidaRepository extends JpaRepository<Partida, Integer> {
    
    Optional<Partida> findByCodigoPartida(String codigoPartida);
    
    List<Partida> findByEsPublicaTrueAndEstado(Partida.EstadoPartida estado);

    boolean existsByCodigoPartida(String codigoPartida);
}