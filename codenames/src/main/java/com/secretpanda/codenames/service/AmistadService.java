package com.secretpanda.codenames.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.secretpanda.codenames.dto.social.AmistadDTO;
import com.secretpanda.codenames.dto.social.NotificacionDTO;
import com.secretpanda.codenames.dto.social.RankingDTO;
import com.secretpanda.codenames.exception.BadRequestException;
import com.secretpanda.codenames.exception.NotFoundException;
import com.secretpanda.codenames.mapper.jugador.JugadorMapper;
import com.secretpanda.codenames.mapper.social.AmistadMapper;
import com.secretpanda.codenames.model.Amistad;
import com.secretpanda.codenames.model.AmistadId;
import com.secretpanda.codenames.model.Jugador;
import com.secretpanda.codenames.repository.AmistadRepository;
import com.secretpanda.codenames.repository.JugadorRepository;

@Service
public class AmistadService {

    private final AmistadRepository amistadRepository;
    private final JugadorRepository jugadorRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public AmistadService(AmistadRepository amistadRepository, JugadorRepository jugadorRepository, SimpMessagingTemplate messagingTemplate) {
        this.amistadRepository = amistadRepository;
        this.jugadorRepository = jugadorRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @Transactional(readOnly = true)
    public List<RankingDTO> getAmigosAceptados(String idGoogle) {
        List<Amistad> amistades = amistadRepository.findAmistadesPorJugadorYEstado(idGoogle, Amistad.EstadoAmistad.aceptada);
        
        List<Jugador> amigosActivos = amistades.stream()
                .map(a -> a.getSolicitante().getIdGoogle().equals(idGoogle) ? a.getReceptor() : a.getSolicitante())
                .filter(Jugador::isActivo)
                .toList();

        return JugadorMapper.toRankingDTOList(amigosActivos);
    }

    @Transactional(readOnly = true)
    public List<AmistadDTO> getSolicitudesPendientes(String idGoogle) {
        List<Amistad> pendientes = amistadRepository.findById_IdReceptorAndEstado(idGoogle, Amistad.EstadoAmistad.pendiente);
        return AmistadMapper.toDTOList(pendientes);
    }

    @Transactional
    public void enviarSolicitud(String idSolicitante, String tagReceptor) {
        Jugador solicitante = jugadorRepository.findById(idSolicitante).orElseThrow();
        Jugador receptor = jugadorRepository.findByTagAndActivoTrue(tagReceptor.trim())
                .orElseThrow(() -> new NotFoundException("No existe un jugador activo con el tag: " + tagReceptor));

        if (idSolicitante.equals(receptor.getIdGoogle())) 
            throw new BadRequestException("No puedes enviarte solicitud a ti mismo.");

        // Comprobar si ya existe una relación previa en cualquier sentido
        Optional<Amistad> relacionExistente = amistadRepository.findAmistadEntreJugadores(idSolicitante, receptor.getIdGoogle());

        if (relacionExistente.isPresent()) {
            Amistad am = relacionExistente.get();
            
            if (am.getEstado() == Amistad.EstadoAmistad.aceptada) {
                throw new BadRequestException("Ya sois amigos.");
            }

            // Si la solicitud existente la envió el OTRO y está pendiente, asutoacepta
            if (am.getSolicitante().getIdGoogle().equals(receptor.getIdGoogle())) {
                am.setEstado(Amistad.EstadoAmistad.aceptada);
                amistadRepository.save(am);
                
                // Notificar a ambos que la amistad se ha sellado
                actualizarListaAmigosWS(idSolicitante);
                actualizarListaAmigosWS(receptor.getIdGoogle());
                enviarNotificacionGeneralWS(idSolicitante, "solicitud_amistad", "¡Interés mutuo! Ahora eres amigo de " + receptor.getTag());
                enviarNotificacionGeneralWS(receptor.getIdGoogle(), "solicitud_amistad", solicitante.getTag() + " también te ha agregado.");
                
                // Limpiar la lista de pendientes del que ya la tenía
                actualizarListaSolicitudesWS(idSolicitante);
                return;
            } else {
                // Si la envié YO mismo antes y sigue pendiente
                throw new BadRequestException("Ya has enviado una solicitud a este jugador.");
            }
        }

        // Si no existía nada, crear nueva solicitud normal
        Amistad nueva = new Amistad();
        AmistadId aid = new AmistadId();
        aid.setIdSolicitante(idSolicitante);
        aid.setIdReceptor(receptor.getIdGoogle());
        nueva.setId(aid);
        nueva.setSolicitante(solicitante);
        nueva.setReceptor(receptor);
        nueva.setEstado(Amistad.EstadoAmistad.pendiente);
        amistadRepository.save(nueva);

        actualizarListaSolicitudesWS(receptor.getIdGoogle());
        enviarNotificacionGeneralWS(receptor.getIdGoogle(), "solicitud_amistad", "Nueva solicitud de " + solicitante.getTag());
    }

    @Transactional
    public void gestionarSolicitud(String idReceptor, String idSolicitante, String estado) {
        AmistadId aid = new AmistadId();
        aid.setIdSolicitante(idSolicitante);
        aid.setIdReceptor(idReceptor);
        Amistad amistad = amistadRepository.findById(aid)
                .orElseThrow(() -> new NotFoundException("Solicitud no encontrada."));

        if ("aceptada".equalsIgnoreCase(estado)) {
            amistad.setEstado(Amistad.EstadoAmistad.aceptada);
            amistadRepository.save(amistad);

            // Notificar a AMBOS para actualizar sus listas de amigos 
            actualizarListaAmigosWS(idReceptor);
            actualizarListaAmigosWS(idSolicitante);

            // Notificar al solicitante que ha sido aceptado
            enviarNotificacionGeneralWS(idSolicitante, "solicitud_amistad", "Ahora eres amigo de " + amistad.getReceptor().getTag());
        } else {
            amistadRepository.delete(amistad);
        }

        // En cualquier caso, actualizar la lista de solicitudes del receptor para que desaparezca la fila
        actualizarListaSolicitudesWS(idReceptor);
    }

    @Transactional(readOnly = true)
    public List<RankingDTO> buscarJugadores(String tag, String miId) {
        return JugadorMapper.toRankingDTOList(
                jugadorRepository.findByTagContainingIgnoreCaseAndActivoTrue(tag, PageRequest.of(0, 10))
                        .stream().filter(j -> !j.getIdGoogle().equals(miId)).toList()
        );
    }

    // --- MÉTODOS PRIVADOS PARA CUMPLIR LOS CANALES WS DEL CONTRATO ---

    private void actualizarListaAmigosWS(String idGoogle) {
        List<RankingDTO> amigos = getAmigosAceptados(idGoogle);
        messagingTemplate.convertAndSendToUser(idGoogle, "/queue/amigos", amigos);
    }

    private void actualizarListaSolicitudesWS(String idGoogle) {
        List<AmistadDTO> solicitudes = getSolicitudesPendientes(idGoogle);
        messagingTemplate.convertAndSendToUser(idGoogle, "/queue/solicitudes", solicitudes);
    }

    private void enviarNotificacionGeneralWS(String idGoogle, String tipo, String mensaje) {
        NotificacionDTO notif = new NotificacionDTO(tipo, Map.of("mensaje", mensaje));
        messagingTemplate.convertAndSendToUser(idGoogle, "/queue/jugadores/notificaciones", notif);
    }
}