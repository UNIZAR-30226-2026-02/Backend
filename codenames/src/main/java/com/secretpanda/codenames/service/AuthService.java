package com.secretpanda.codenames.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.secretpanda.codenames.dto.auth.AuthResponseDTO;
import com.secretpanda.codenames.dto.jugador.JugadorDTO;
import com.secretpanda.codenames.exception.BadRequestException;
import com.secretpanda.codenames.exception.ErrorCode;
import com.secretpanda.codenames.exception.NotFoundException;
import com.secretpanda.codenames.exception.SecretPandaException;
import com.secretpanda.codenames.mapper.jugador.JugadorMapper;
import com.secretpanda.codenames.model.InventarioTema;
import com.secretpanda.codenames.model.InventarioTemaId;
import com.secretpanda.codenames.model.Jugador;
import com.secretpanda.codenames.model.JugadorPartida;
import com.secretpanda.codenames.model.Partida;
import com.secretpanda.codenames.repository.AmistadRepository;
import com.secretpanda.codenames.repository.InventarioTemaRepository;
import com.secretpanda.codenames.repository.JugadorPartidaRepository;
import com.secretpanda.codenames.repository.JugadorRepository;
import com.secretpanda.codenames.repository.TemaRepository;
import com.secretpanda.codenames.security.GoogleAuthService;
import com.secretpanda.codenames.security.JwtService;
import com.secretpanda.codenames.util.EstadisticasCalculator;

@Service
public class AuthService {

    private final GoogleAuthService googleAuthService;
    private final JwtService jwtService;
    private final JugadorRepository jugadorRepository;
    private final JugadorPartidaRepository jugadorPartidaRepository;
    private final EstadisticasCalculator calculator;
    private final TemaRepository temaRepository;
    private final InventarioTemaRepository inventarioTemaRepository;
    private final JugadorService jugadorService;
    private final AmistadRepository amistadRepository;
    private final PartidaService partidaService;
    private final ProfanityFilterService profanityFilterService;

    @Value("${game.tema-basico-id:1}")
    private Integer temaBasicoId;

    public AuthService(GoogleAuthService googleAuthService,
                    JwtService jwtService,
                    JugadorRepository jugadorRepository,
                    JugadorPartidaRepository jugadorPartidaRepository,
                    EstadisticasCalculator calculator,
                    TemaRepository temaRepository,
                    InventarioTemaRepository inventarioTemaRepository,
                    JugadorService jugadorService,
                    AmistadRepository amistadRepository,
                    PartidaService partidaService,
                    ProfanityFilterService profanityFilterService) {
        this.googleAuthService = googleAuthService;
        this.jwtService = jwtService;
        this.jugadorRepository = jugadorRepository;
        this.jugadorPartidaRepository = jugadorPartidaRepository;
        this.calculator = calculator;
        this.temaRepository = temaRepository;
        this.inventarioTemaRepository = inventarioTemaRepository;
        this.jugadorService = jugadorService;
        this.amistadRepository = amistadRepository;
        this.partidaService = partidaService;
        this.profanityFilterService = profanityFilterService;
    }

    // ─── Login ────────────────────────────────────────────────────────────────

    /**
     * Verifica el idToken con Google.
     * Si el jugador NO existe → devuelve esNuevo=true (sin JWT).
     * Si ya existe → devuelve esNuevo=false + JWT + datos jugador + partidaActivaId.
     */
    @Transactional
    public AuthResponseDTO login(String idTokenGoogle) {
        GoogleAuthService.DatosGoogle datos = googleAuthService.verificarToken(idTokenGoogle);

        Optional<Jugador> opt = jugadorRepository.findById(datos.idGoogle());

        if (opt.isEmpty()) {
            // La primera vez el frontend redirige al formulario de elección de tag
            return AuthResponseDTO.nuevo();
        }

        Jugador jugador = opt.get();
        if (!jugador.isActivo()) {
            return AuthResponseDTO.nuevo();        
        }

        return construirRespuestaExistente(jugador);
    }

    // ─── Registro ─────────────────────────────────────────────────────────────

    /**
     * Crea al jugador con el tag elegido.
     * Valida unicidad del tag. Devuelve JWT + datos jugador.
     */
    @Transactional
    public AuthResponseDTO registro(String idTokenGoogle, String tag) {
        // EXTRAER EL ID REAL DESDE GOOGLE
        GoogleAuthService.DatosGoogle datos = googleAuthService.verificarToken(idTokenGoogle);
        String idGoogleReal = datos.idGoogle();

        // BUSCAR SI YA EXISTE (ACTIVO O INACTIVO)
        Optional<Jugador> jugadorOpt = jugadorRepository.findById(idGoogleReal);

        // VALIDAR: Si existe y YA ESTÁ ACTIVO, no puede registrarse otra vez
        if (jugadorOpt.isPresent() && jugadorOpt.get().isActivo()) {
            throw new BadRequestException("Este usuario ya está registrado y activo. Usa /login.");
        }

        // VALIDAR UNICIDAD DEL TAG (Solo entre usuarios activos)
        if (tag == null || tag.isBlank()) {
            throw new BadRequestException("El tag no puede estar vacío.");
        }
        
        String tagLimpio = tag.trim();
        if (profanityFilterService.filter(tagLimpio).wasCensored()) {
            throw new SecretPandaException(ErrorCode.PROFANITY_DETECTED);
        }
        
        if (jugadorRepository.existsByTagAndActivoTrue(tagLimpio)) {
            throw new SecretPandaException(ErrorCode.TAG_TAKEN);
        }

        Jugador jugador;
        if (jugadorOpt.isPresent()) {
            // REACTIVACIÓN (El usuario existía pero estaba desactivado)
            jugador = jugadorOpt.get();
            jugador.setActivo(true);
            jugador.setTag(tag.trim());
            jugador.setBalas(0); // Empezar de cero
            // Reset de estadísticas por si no se hizo al desactivar
            jugador.setPartidasJugadas(0);
            jugador.setVictorias(0);
            jugador.setNumAciertos(0);
            jugador.setNumFallos(0);
        } else {
            // REGISTRO TOTALMENTE NUEVO
            jugador = new Jugador();
            jugador.setIdGoogle(idGoogleReal);
            jugador.setTag(tag.trim());
            jugador.setFotoPerfil("1");
            jugador.setBalas(0);
        }

        jugadorRepository.save(jugador);

        // ASIGNAR TEMA POR DEFECTO (Si no lo tiene ya)
        boolean yaTieneTema = inventarioTemaRepository.existsById_IdJugadorAndId_IdTema(jugador.getIdGoogle(), temaBasicoId); 
        if (!yaTieneTema) {
            temaRepository.findById(temaBasicoId).ifPresent(tema -> {
                InventarioTema inv = new InventarioTema();
                InventarioTemaId invId = new InventarioTemaId();
                invId.setIdJugador(jugador.getIdGoogle());
                invId.setIdTema(tema.getIdTema());
                inv.setId(invId);
                inv.setJugador(jugador);
                inv.setTema(tema);
                inventarioTemaRepository.save(inv);
            });
        }

        // Inicializar logros con progreso 0 para el nuevo jugador
        jugadorService.inicializarLogros(jugador);

        return construirRespuestaExistente(jugador);
    }

    // ─── Desactivar cuenta ────────────────────────────────────────────────────

    @Transactional
    public void desactivarCuenta(String idGoogle) {
        Jugador jugador = jugadorRepository.findById(idGoogle)
                .orElseThrow(() -> new NotFoundException("Jugador no encontrado."));

        // Abandonar partida activa si existe para evitar que se quede fantasma ESTO NO HACE FALTA (comentado por si acaso)
        /*Integer partidaId = buscarPartidaActiva(idGoogle);
        if (partidaId != null) {
            partidaService.abandonar(partidaId, idGoogle);
        }*/

        // Borrar todas las relaciones de amistad
        amistadRepository.deleteAllByJugador(idGoogle);

        // Desactivamos y reseteamos estadísticas visibles + liberar TAG
        jugador.setActivo(false);
        jugador.getInventario().clear();
        jugador.getInventarioTemas().clear();
        // jugador.setTag(null); No gestionamos tags a null
        jugador.setFotoPerfil(null);
        jugador.setPartidasJugadas(0);
        jugador.setVictorias(0);
        jugador.setNumAciertos(0);
        jugador.setNumFallos(0);
        jugador.setBalas(0);
        jugadorRepository.save(jugador);
    }

    // ─── Helper ───────────────────────────────────────────────────────────────

    @Transactional
    public AuthResponseDTO construirRespuestaExistente(Jugador jugador) {
        String token = jwtService.generarToken(jugador.getIdGoogle());
        
        // Control de Sesión Única
        jugador.setTokenActual(token);
        jugadorRepository.saveAndFlush(jugador);

        JugadorDTO jugadorDTO = JugadorMapper.toDTO(jugador, calculator);

        // Buscamos si tiene alguna partida EN CURSO
        Integer partidaActivaId = buscarPartidaActiva(jugador.getIdGoogle());

        return AuthResponseDTO.existente(token, jugadorDTO, partidaActivaId);
    }

    /**
     * Devuelve el id de la primera partida en_curso o esperando en la que el jugador participa
     * y no ha abandonado. Null si no hay ninguna.
     */
    private Integer buscarPartidaActiva(String idGoogle) {
        return jugadorPartidaRepository
                .findFirstByJugador_IdGoogleAndPartida_EstadoInAndAbandonoFalse(
                        idGoogle, List.of(Partida.EstadoPartida.esperando, Partida.EstadoPartida.en_curso))
                .map(jp -> jp.getPartida().getIdPartida())
                .orElse(null);
    }
}
