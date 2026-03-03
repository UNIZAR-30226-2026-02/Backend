package com.secretpanda.codenames.repositories;

import com.secretpanda.codenames.models.VotoCarta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VotoCartaRepository extends JpaRepository<VotoCarta, Integer> {
    List<VotoCarta> findByTurno_IdTurno(Integer idTurno);
    List<VotoCarta> findByCartaTablero_IdCartaTablero(Integer idCartaTablero);
}