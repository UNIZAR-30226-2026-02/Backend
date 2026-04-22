package com.secretpanda.codenames.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.secretpanda.codenames.dto.auth.AuthResponseDTO;
import com.secretpanda.codenames.dto.jugador.JugadorDTO;
import com.secretpanda.codenames.exception.BadRequestException;
import com.secretpanda.codenames.exception.NotFoundException;
import com.secretpanda.codenames.mapper.jugador.JugadorMapper;
import com.secretpanda.codenames.model.InventarioTema;
import com.secretpanda.codenames.model.InventarioTemaId;
import com.secretpanda.codenames.model.Jugador;
import com.secretpanda.codenames.model.JugadorPartida;
import com.secretpanda.codenames.model.Partida;
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

    @Value("${game.tema-basico-id:1}")
    private Integer temaBasicoId;

    public AuthService(GoogleAuthService googleAuthService,
                    JwtService jwtService,
                    JugadorRepository jugadorRepository,
                    JugadorPartidaRepository jugadorPartidaRepository,
                    EstadisticasCalculator calculator,
                    TemaRepository temaRepository,
                    InventarioTemaRepository inventarioTemaRepository,
                    JugadorService jugadorService) {
        this.googleAuthService = googleAuthService;
        this.jwtService = jwtService;
        this.jugadorRepository = jugadorRepository;
        this.jugadorPartidaRepository = jugadorPartidaRepository;
        this.calculator = calculator;
        this.temaRepository = temaRepository;
        this.inventarioTemaRepository = inventarioTemaRepository;
        this.jugadorService = jugadorService;
    }

    // ─── Login ────────────────────────────────────────────────────────────────

    /**
     * Verifica el idToken con Google.
     * Si el jugador NO existe → devuelve esNuevo=true (sin JWT).
     * Si ya existe → devuelve esNuevo=false + JWT + datos jugador + partidaActivaId.
     */
    @Transactional(readOnly = true)
    public AuthResponseDTO login(String idTokenGoogle) {
        GoogleAuthService.DatosGoogle datos = googleAuthService.verificarToken(idTokenGoogle);

        Optional<Jugador> opt = jugadorRepository.findById(datos.idGoogle());

        if (opt.isEmpty()) {
            // Primera vez: el frontend redirige al formulario de elección de tag
            return AuthResponseDTO.nuevo();
        }

        Jugador jugador = opt.get();
        if (!jugador.isActivo()) {
            // Cuenta desactivada como si no existiera
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
        // 1. EXTRAER EL ID REAL DESDE GOOGLE
        GoogleAuthService.DatosGoogle datos = googleAuthService.verificarToken(idTokenGoogle);
        String idGoogleReal = datos.idGoogle();

        // 2. BUSCAR SI YA EXISTE (ACTIVO O INACTIVO)
        Optional<Jugador> jugadorOpt = jugadorRepository.findById(idGoogleReal);

        // 3. VALIDAR: Si existe y YA ESTÁ ACTIVO, no puede registrarse otra vez
        if (jugadorOpt.isPresent() && jugadorOpt.get().isActivo()) {
            throw new BadRequestException("Este usuario ya está registrado y activo. Usa /login.");
        }

        // 4. VALIDAR UNICIDAD DEL TAG (Solo entre usuarios activos)
        if (tag == null || tag.isBlank()) {
            throw new BadRequestException("El tag no puede estar vacío.");
        }
        if (jugadorRepository.existsByTagAndActivoTrue(tag.trim())) {
            throw new BadRequestException("Ese nombre de usuario ya está en uso.");
        }

        Jugador jugador;
        if (jugadorOpt.isPresent()) {
            // CASO A: REACTIVACIÓN (El usuario existía pero estaba desactivado)
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
            // CASO B: REGISTRO TOTALMENTE NUEVO
            jugador = new Jugador();
            jugador.setIdGoogle(idGoogleReal);
            jugador.setTag(tag.trim());
            jugador.setBalas(0);
        }

        jugadorRepository.save(jugador);

        // 5. ASIGNAR TEMA POR DEFECTO (Si no lo tiene ya)
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

        // RF: Inicializar logros con progreso 0 para el nuevo jugador
        jugadorService.inicializarLogros(jugador);

        return construirRespuestaExistente(jugador);
    }

    // ─── Desactivar cuenta ────────────────────────────────────────────────────

    @Transactional
    public void desactivarCuenta(String idGoogle) {
        Jugador jugador = jugadorRepository.findById(idGoogle)
                .orElseThrow(() -> new NotFoundException("Jugador no encontrado."));

        jugador.setActivo(false);
        jugador.setFotoPerfil("1");
        // Resetear estadísticas visibles (leaderboard, amigos)
        jugador.setPartidasJugadas(0);
        jugador.setVictorias(0);
        jugador.setNumAciertos(0);
        jugador.setNumFallos(0);
        jugadorRepository.save(jugador);
    }

    // ─── Helper ───────────────────────────────────────────────────────────────

    private AuthResponseDTO construirRespuestaExistente(Jugador jugador) {
        String token = jwtService.generarToken(jugador.getIdGoogle());
        JugadorDTO jugadorDTO = JugadorMapper.toDTO(jugador, calculator);

        // Buscar si tiene alguna partida EN CURSO
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
