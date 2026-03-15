package com.secretpanda.codenames.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.secretpanda.codenames.dto.auth.AuthResponseDTO;
import com.secretpanda.codenames.dto.jugador.JugadorDTO;
import com.secretpanda.codenames.mapper.jugador.JugadorMapper;
import com.secretpanda.codenames.model.Jugador;
import com.secretpanda.codenames.repository.JugadorRepository;
import com.secretpanda.codenames.security.GoogleAuthService;
import com.secretpanda.codenames.security.JwtService;

@Service
public class AuthService {

    private final GoogleAuthService googleAuthService;
    private final JwtService jwtService;
    private final JugadorRepository jugadorRepository;

    public AuthService(GoogleAuthService googleAuthService, JwtService jwtService, JugadorRepository jugadorRepository) {
        this.googleAuthService = googleAuthService;
        this.jwtService = jwtService;
        this.jugadorRepository = jugadorRepository;
    }

    @Transactional
    public AuthResponseDTO login(String idTokenGoogle) {
        // 1. Verifica el token con la API de Google Auth 2.0 (RNF-7)
        GoogleAuthService.DatosGoogle datos = googleAuthService.verificarToken(idTokenGoogle);

        // 2. Busca en la tabla JUGADOR. Si no existe, hace un INSERT con estadísticas a 0 y balas iniciales.
        Jugador jugador = jugadorRepository.findById(datos.idGoogle()).orElseGet(() -> {
            Jugador nuevo = new Jugador();
            nuevo.setIdGoogle(datos.idGoogle());
            // Generamos un tag temporal basado en el nombre de Google (quitando espacios)
            nuevo.setTag(datos.nombre().replaceAll("\\s+", "") + "_" + (int)(Math.random() * 1000));
            // Nota: victorias, num_aciertos, num_fallos y partidas_jugadas ya se inicializan a 0
            // automáticamente por los valores DEFAULT de PostgreSQL.
            nuevo.setBalas(0); 
            return jugadorRepository.save(nuevo);
        });

        // 3. Ejecuta el método iniciarSesionGoogle()
        return iniciarSesionGoogle(jugador);
    }

    /**
     * Método especificado en el contrato de la API para completar el inicio de sesión.
     * Genera el JWT propio del sistema y devuelve el objeto JUGADOR completo.
     */
    private AuthResponseDTO iniciarSesionGoogle(Jugador jugador) {
        // Generamos el JWT de Secret Panda
        String token = jwtService.generarToken(jugador.getIdGoogle());

        // Devolvemos el objeto JUGADOR completo (pasando null al calculator para ir directo al MVP)
        JugadorDTO jugadorDTO = JugadorMapper.toDTO(jugador, null);
        return new AuthResponseDTO(token, jugadorDTO);
    }
}