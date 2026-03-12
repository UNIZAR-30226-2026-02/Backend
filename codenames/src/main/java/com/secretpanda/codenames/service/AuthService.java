package com.secretpanda.codenames.service;

import com.secretpanda.codenames.dto.auth.AuthResponseDTO;
import com.secretpanda.codenames.dto.auth.LoginRequestDTO;
import com.secretpanda.codenames.model.Jugador;
import com.secretpanda.codenames.repository.JugadorRepository;
import com.secretpanda.codenames.security.GoogleAuthService;
import com.secretpanda.codenames.security.GoogleAuthService.DatosGoogle;
import com.secretpanda.codenames.security.JwtService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

/**
 * Gestiona el flujo completo de autenticación mediante Google OAuth 2.0.
 *
 * Un único endpoint cubre tanto el registro (primera vez) como el login
 * (visitas siguientes). La decisión se toma comprobando si el idGoogle
 * ya existe en la base de datos (patrón upsert).
 */
@Service
public class AuthService {

    @Autowired
    private GoogleAuthService googleAuthService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private JugadorRepository jugadorRepository;

    /**
     * Punto de entrada único para login y registro.
     *
     * @param dto contiene el idToken de Google y el tag deseado por el usuario
     * @return AuthResponseDTO con nuestro JWT interno y el perfil básico del jugador
     */
    @Transactional
    public AuthResponseDTO loginORegistrar(LoginRequestDTO dto) {

        // 1. Verificar el idToken con los servidores de Google
        DatosGoogle datosGoogle = googleAuthService.verificarToken(dto.getIdToken());

        // 2. Buscar si el jugador ya existe por su idGoogle
        Jugador jugador = jugadorRepository.findByIdGoogle(datosGoogle.idGoogle())
                .orElse(null);

        if (jugador == null) {
            // ── REGISTRO: primera vez que este usuario accede ──────────────
            jugador = registrarNuevoJugador(datosGoogle, dto.getTag());
        }
        // Si ya existe simplemente seguimos con el jugador recuperado de BD
        // (login: no modificamos sus datos)

        // 3. Generar nuestro propio JWT interno
        String token = jwtService.generarToken(jugador.getIdGoogle());

        // 4. Construir y devolver la respuesta
        return new AuthResponseDTO(token, jugador);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Métodos privados
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Crea y persiste un nuevo Jugador a partir de los datos de Google y el
     * tag elegido por el usuario en el cliente.
     *
     * Reglas:
     *  - El tag no puede estar vacío.
     *  - El tag debe ser único en la base de datos.
     *  - Si no se proporciona tag se usa el nombre de Google como fallback.
     */
    private Jugador registrarNuevoJugador(DatosGoogle datosGoogle, String tagSolicitado) {

        // Determinar el tag definitivo
        String tag = StringUtils.hasText(tagSolicitado)
                ? tagSolicitado.trim()
                : datosGoogle.nombre();   // fallback al nombre de Google

        if (!StringUtils.hasText(tag)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El tag (nombre de usuario) es obligatorio para el registro"
            );
        }

        // Comprobar unicidad del tag
        if (jugadorRepository.existsByTag(tag)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El tag '" + tag + "' ya está en uso. Elige otro nombre de usuario."
            );
        }

        Jugador nuevo = new Jugador();
        nuevo.setIdGoogle(datosGoogle.idGoogle());
        nuevo.setTag(tag);
        // fotoPerfil: null por defecto, el usuario la elige después (RF-6)
        // balas, victorias, etc.: se inicializan a 0 en el propio modelo

        return jugadorRepository.save(nuevo);
    }
}