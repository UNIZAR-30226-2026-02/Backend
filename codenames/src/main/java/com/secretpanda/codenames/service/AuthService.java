package com.secretpanda.codenames.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.secretpanda.codenames.dto.auth.AuthResponseDTO;
import com.secretpanda.codenames.dto.jugador.JugadorDTO;
import com.secretpanda.codenames.exception.BadRequestException;
import com.secretpanda.codenames.exception.NotFoundException;
import com.secretpanda.codenames.mapper.jugador.JugadorMapper;
import com.secretpanda.codenames.model.Jugador;
import com.secretpanda.codenames.model.JugadorPartida;
import com.secretpanda.codenames.model.Partida;
import com.secretpanda.codenames.repository.JugadorPartidaRepository;
import com.secretpanda.codenames.repository.JugadorRepository;
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

    public AuthService(GoogleAuthService googleAuthService,
                       JwtService jwtService,
                       JugadorRepository jugadorRepository,
                       JugadorPartidaRepository jugadorPartidaRepository,
                       EstadisticasCalculator calculator) {
        this.googleAuthService = googleAuthService;
        this.jwtService = jwtService;
        this.jugadorRepository = jugadorRepository;
        this.jugadorPartidaRepository = jugadorPartidaRepository;
        this.calculator = calculator;
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
            throw new BadRequestException("Esta cuenta está desactivada.");
        }

        return construirRespuestaExistente(jugador);
    }

    // ─── Registro ─────────────────────────────────────────────────────────────

    /**
     * Crea al jugador con el tag elegido.
     * Valida unicidad del tag. Devuelve JWT + datos jugador.
     */
    @Transactional
    public AuthResponseDTO registro(String idGoogle, String tag) {
        // Validar que el idGoogle aún no tiene cuenta (doble seguro)
        if (jugadorRepository.existsById(idGoogle)) {
            throw new BadRequestException("Este usuario ya está registrado. Usa /login.");
        }

        // Validar unicidad del tag
        if (tag == null || tag.isBlank()) {
            throw new BadRequestException("El tag no puede estar vacío.");
        }
        if (jugadorRepository.existsByTagAndActivoTrue(tag.trim())) {
            throw new BadRequestException("Ese nombre de usuario ya está en uso.");
        }

        Jugador nuevo = new Jugador();
        nuevo.setIdGoogle(idGoogle);
        nuevo.setTag(tag.trim());
        nuevo.setBalas(0);
        jugadorRepository.save(nuevo);

        return construirRespuestaExistente(nuevo);
    }

    // ─── Desactivar cuenta ────────────────────────────────────────────────────

    @Transactional
    public void desactivarCuenta(String idGoogle) {
        Jugador jugador = jugadorRepository.findById(idGoogle)
                .orElseThrow(() -> new NotFoundException("Jugador no encontrado."));

        jugador.setActivo(false);
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
     * Devuelve el id de la primera partida en_curso en la que el jugador participa
     * y no ha abandonado. Null si no hay ninguna.
     */
    private Integer buscarPartidaActiva(String idGoogle) {
        List<JugadorPartida> participaciones =
                jugadorPartidaRepository.findByJugador_IdGoogle(idGoogle);

        return participaciones.stream()
                .filter(jp -> !jp.isAbandono())
                .filter(jp -> Partida.EstadoPartida.en_curso.equals(jp.getPartida().getEstado()))
                .map(jp -> jp.getPartida().getIdPartida())
                .findFirst()
                .orElse(null);
    }
}
