package com.secretpanda.codenames.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.secretpanda.codenames.model.VotoCarta;

@Repository
public interface VotoCartaRepository extends JpaRepository<VotoCarta, Integer> {
    List<VotoCarta> findByTurno_IdTurno(Integer idTurno);
    List<VotoCarta> findByCartaTablero_IdCartaTablero(Integer idCartaTablero);
}