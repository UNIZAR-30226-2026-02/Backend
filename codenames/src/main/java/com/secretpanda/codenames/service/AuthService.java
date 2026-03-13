package com.secretpanda.codenames.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import com.secretpanda.codenames.dto.auth.AuthResponseDTO;
import com.secretpanda.codenames.dto.auth.LoginRequestDTO;
import com.secretpanda.codenames.model.Jugador;
import com.secretpanda.codenames.repository.JugadorRepository;
import com.secretpanda.codenames.security.GoogleAuthService;
import com.secretpanda.codenames.security.GoogleAuthService.DatosGoogle;
import com.secretpanda.codenames.security.JwtService;

/**
 * Gestiona el flujo de autenticación con Google OAuth 2.0.
 *
 * Contrato API (POST /auth/login):
 *   - Verifica el token con Google.
 *   - Si el jugador no existe → INSERT con estadísticas a 0.
 *   - Si el jugador existe    → login directo.
 *   - Devuelve el JUGADOR completo + token JWT.
 *
 * El tag inicial es el nombre de Google. El usuario puede cambiarlo
 * después con PUT /jugadores/{id_google} (RF-6).
 */
@Service
public class AuthService {

    @Autowired
    private GoogleAuthService googleAuthService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private JugadorRepository jugadorRepository;

    @Transactional
    public AuthResponseDTO loginORegistrar(LoginRequestDTO dto) {

        // 1. Verificar el token con Google
        DatosGoogle datosGoogle = googleAuthService.verificarToken(dto.getId_google());

        // 2. Buscar si el jugador ya existe
        Jugador jugador = jugadorRepository.findByIdGoogle(datosGoogle.idGoogle())
                .orElse(null);

        if (jugador == null) {
            // Primera vez → registrar con estadísticas a 0
            jugador = registrarNuevoJugador(datosGoogle);
        }

        // 3. Generar JWT interno
        String token = jwtService.generarToken(jugador.getIdGoogle());

        // 4. Devolver JUGADOR completo + token
        return new AuthResponseDTO(token, jugador);
    }

    /**
     * Crea y persiste un nuevo Jugador.
     * Tag inicial = nombre de Google. Si ya está en uso, añade sufijo _1, _2...
     */
    private Jugador registrarNuevoJugador(DatosGoogle datosGoogle) {

        String tagBase = StringUtils.hasText(datosGoogle.nombre())
                ? datosGoogle.nombre().trim()
                : "user_" + datosGoogle.idGoogle().substring(0, 8);

        // Garantizar unicidad del tag
        String tag = tagBase;
        int intento = 1;
        while (jugadorRepository.existsByTag(tag)) {
            tag = tagBase + "_" + intento;
            intento++;
        }

        Jugador nuevo = new Jugador();
        nuevo.setIdGoogle(datosGoogle.idGoogle());
        nuevo.setTag(tag);
        // balas, victorias, aciertos, fallos → se inicializan a 0 en el modelo

        try {
            return jugadorRepository.save(nuevo);
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error al registrar el jugador: " + e.getMessage()
            );
        }
    }
}