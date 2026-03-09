package com.secretpanda.codenames.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.secretpanda.codenames.model.Chat;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Integer> {
    
    // Cargar todo el hitorial inicial
    List<Chat> findByPartida_IdPartidaOrderByFechaAsc(Integer idPartida);

    // PARA RECONEXIONES 
    // Si a un jugador se le va el internet y vuelve, no le mandes los 500 mensajes otra vez.
    // Le mandas solo los que se escribieron DESPUÉS de su última conexión.
    List<Chat> findByPartida_IdPartidaAndFechaAfterOrderByFechaAsc(Integer idPartida, LocalDateTime fechaUltimoMensaje);

    // OPTIMIZACIÓN
    // Si quieres cargar solo los (EJ. Últimos 50 mensajes) para no saturar la RAM del móvil.
    // Usamos OrderByFechaDesc para coger los más recientes, y un Pageable para limitar la cantidad.
    List<Chat> findByPartida_IdPartidaOrderByFechaDesc(Integer idPartida, Pageable pageable);

    // MANTENIMIENTO (Limpieza de Base de Datos)
    // Código Secreto genera muchos mensajes. Si la partida se cancela o termina, 
    // interesante borrar el chat entero para ahorrar espacio en el servidor.
    void deleteByPartida_IdPartida(Integer idPartida);
}