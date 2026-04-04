package com.secretpanda.codenames.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.secretpanda.codenames.dto.jugador.ActualizarPerfilDTO;
import com.secretpanda.codenames.dto.jugador.HistorialDTO;
import com.secretpanda.codenames.dto.jugador.JugadorDTO;
import com.secretpanda.codenames.dto.jugador.PersonalizacionInventarioDTO;
import com.secretpanda.codenames.dto.jugador.TemaInventarioDTO;
import com.secretpanda.codenames.dto.partida.PartidaResumenDTO;
import com.secretpanda.codenames.dto.tienda.LogroDTO;
import com.secretpanda.codenames.exception.BadRequestException;
import com.secretpanda.codenames.exception.NotFoundException;
import com.secretpanda.codenames.mapper.jugador.JugadorLogroMapper;
import com.secretpanda.codenames.mapper.jugador.JugadorMapper;
import com.secretpanda.codenames.mapper.jugador.PersonalizacionInventarioMapper;
import com.secretpanda.codenames.mapper.jugador.TemaInventarioMapper;
import com.secretpanda.codenames.mapper.partida.PartidaMapper;
import com.secretpanda.codenames.model.InventarioPersonalizacion;
import com.secretpanda.codenames.model.Jugador;
import com.secretpanda.codenames.model.JugadorLogro;
import com.secretpanda.codenames.model.JugadorPartida;
import com.secretpanda.codenames.model.Personalizacion;
import com.secretpanda.codenames.repository.InventarioPersonalizacionRepository;
import com.secretpanda.codenames.repository.InventarioTemaRepository;
import com.secretpanda.codenames.repository.JugadorLogroRepository;
import com.secretpanda.codenames.repository.JugadorPartidaRepository;
import com.secretpanda.codenames.repository.JugadorRepository;
import com.secretpanda.codenames.util.EstadisticasCalculator;

@Service
public class JugadorService {

    private static final int MAX_HISTORIAL = 30;

    private final JugadorRepository jugadorRepository;
    private final InventarioTemaRepository inventarioTemaRepository;
    private final InventarioPersonalizacionRepository inventarioPersonalizacionRepository;
    private final JugadorPartidaRepository jugadorPartidaRepository;
    private final JugadorLogroRepository jugadorLogroRepository;
    private final EstadisticasCalculator calculator;

    public JugadorService(JugadorRepository jugadorRepository,
                          InventarioTemaRepository inventarioTemaRepository,
                          InventarioPersonalizacionRepository inventarioPersonalizacionRepository,
                          JugadorPartidaRepository jugadorPartidaRepository,
                          JugadorLogroRepository jugadorLogroRepository,
                          EstadisticasCalculator calculator) {
        this.jugadorRepository = jugadorRepository;
        this.inventarioTemaRepository = inventarioTemaRepository;
        this.inventarioPersonalizacionRepository = inventarioPersonalizacionRepository;
        this.jugadorPartidaRepository = jugadorPartidaRepository;
        this.jugadorLogroRepository = jugadorLogroRepository;
        this.calculator = calculator;
    }

    // ─── Perfil ───────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public JugadorDTO getPerfil(String idGoogle) {
        Jugador jugador = findJugador(idGoogle);
        return JugadorMapper.toDTO(jugador, calculator);
    }

    @Transactional
    public JugadorDTO actualizarPerfil(ActualizarPerfilDTO dto, String idGoogle) {
        Jugador jugador = findJugador(idGoogle);

        // Validar unicidad del nuevo tag (si lo está cambiando)
        if (dto.getTag() != null && !dto.getTag().equals(jugador.getTag())) {
            if (jugadorRepository.existsByTagAndActivoTrue(dto.getTag().trim())) {
                throw new BadRequestException("Ese nombre de usuario ya está en uso.");
            }
        }

        JugadorMapper.applyUpdateDTO(dto, jugador);
        jugadorRepository.save(jugador);
        return JugadorMapper.toDTO(jugador, calculator);
    }

    // ─── Personalización e Inventario ─────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<PersonalizacionInventarioDTO> getPersonalizacionesAdquiridas(String idGoogle) {
        return PersonalizacionInventarioMapper.toDTOList(
                inventarioPersonalizacionRepository.findById_IdJugador(idGoogle));
    }

    /**
     * Gestiona qué aspecto estético tiene activo el jugador.
     * Si se equipa un nuevo tablero o carta, se desequipa automáticamente el anterior del mismo tipo.
     */
    @Transactional
    public void equiparItem(Integer idPersonalizacion, boolean equipado, String idGoogle) {
        InventarioPersonalizacion itemAEquipar = inventarioPersonalizacionRepository
                .findById_IdJugadorAndId_IdPersonalizacion(idGoogle, idPersonalizacion)
                .orElseThrow(() -> new NotFoundException("No posees este artículo en tu inventario (ID: " + idPersonalizacion + ")."));

        if (equipado) {
            // Obtenemos el TIPO (TABLERO o CARTA) del ítem que el usuario ha elegido
            Personalizacion.TipoPersonalizacion tipoActual = itemAEquipar.getPersonalizacion().getTipo();

            inventarioPersonalizacionRepository
                .findById_IdJugadorAndPersonalizacion_TipoAndEquipadoTrue(idGoogle, tipoActual)
                .ifPresent(itemAntiguo -> {
                    // Si encontramos uno, lo desequipamos antes de activar el nuevo
                    itemAntiguo.setEquipado(false);
                    inventarioPersonalizacionRepository.save(itemAntiguo);
                });
        }

        itemAEquipar.setEquipado(equipado);
        inventarioPersonalizacionRepository.save(itemAEquipar);
    }

    // ─── Temas ────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<TemaInventarioDTO> getTemasAdquiridos(String idGoogle) {
        return TemaInventarioMapper.toDTOList(
                inventarioTemaRepository.findById_IdJugador(idGoogle));
    }

    // ─── Historial ────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public HistorialDTO getHistorial(String idGoogle) {
        // Las últimas MAX_HISTORIAL partidas del jugador
        List<JugadorPartida> participaciones =
                jugadorPartidaRepository.findByJugador_IdGoogleOrderByPartida_FechaCreacionDesc(idGoogle);

        // Limitamos a 30
        List<JugadorPartida> limitadas = participaciones.stream()
                .limit(MAX_HISTORIAL)
                .collect(Collectors.toList());

        List<PartidaResumenDTO> resumen = PartidaMapper.toResumenDTOList(limitadas);

        HistorialDTO dto = new HistorialDTO();
        dto.setPartidas(resumen);
        dto.setPaginaActual(0);
        dto.setTotalPaginas(1);
        dto.setTotalPartidas(resumen.size());
        return dto;
    }

    // ─── Logros ───────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<LogroDTO> getLogros(String idGoogle) {
        List<JugadorLogro> jugadorLogros = jugadorLogroRepository.findById_IdJugador(idGoogle);
        return JugadorLogroMapper.toEnrichedDTOList(jugadorLogros);
    }

    // ─── Helper ───────────────────────────────────────────────────────────────

    private Jugador findJugador(String idGoogle) {
        return jugadorRepository.findById(idGoogle)
                .orElseThrow(() -> new NotFoundException("Jugador no encontrado."));
    }
}
