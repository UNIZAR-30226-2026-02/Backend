package com.secretpanda.codenames.repositories;

import com.secretpanda.codenames.models.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Integer> {
    List<Chat> findByPartida_IdPartidaOrderByFechaAsc(Integer idPartida);
}