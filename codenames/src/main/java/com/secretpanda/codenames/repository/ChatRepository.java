package com.secretpanda.codenames.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.secretpanda.codenames.model.Chat;
import com.secretpanda.codenames.model.JugadorPartida;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Integer> {
    
    // Cargar todo el historial inicial filtrado por equipo
    List<Chat> findByPartida_IdPartidaAndJugadorPartida_EquipoOrderByFechaAsc(Integer idPartida, JugadorPartida.Equipo equipo);

    // PARA RECONEXIONES 
    // Si a un jugador se le va el internet y vuelve, no le mandes los 500 mensajes otra vez.
    // Le mandas solo los que se escribieron DESPUÉS de su última conexión.
    // Solo los de su equipo
    List<Chat> findByPartida_IdPartidaAndJugadorPartida_EquipoAndFechaAfterOrderByFechaAsc(Integer idPartida, JugadorPartida.Equipo equipo, LocalDateTime fechaUltimoMensaje);
    
    // OPTIMIZACIÓN
    // Si quieres cargar solo los (EJ. Últimos 50 mensajes) para no saturar la RAM del móvil.
    // Usamos OrderByFechaDesc para coger los más recientes, y un Pageable para limitar la cantidad.
    // Solo los de su equipo
    List<Chat> findByPartida_IdPartidaAndJugadorPartida_EquipoOrderByFechaDesc(Integer idPartida, JugadorPartida.Equipo equipo, Pageable pageable);

    // Cargar todo el historial de la partida (sin importar el equipo)
    List<Chat> findByPartida_IdPartidaOrderByFechaAsc(Integer idPartida);

    // MANTENIMIENTO (Limpieza de Base de Datos)
    // Código Secreto genera muchos mensajes. Si la partida se cancela o termina, 
    // interesante borrar el chat entero para ahorrar espacio en el servidor.
    @Transactional
    void deleteByPartida_IdPartida(Integer idPartida);
}