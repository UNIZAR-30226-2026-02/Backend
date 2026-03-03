package com.secretpanda.codenames.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.secretpanda.codenames.model.Chat;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Integer> {
    List<Chat> findByPartida_IdPartidaOrderByFechaAsc(Integer idPartida);
}