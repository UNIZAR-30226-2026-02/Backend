package com.secretpanda.codenames.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.secretpanda.codenames.dto.auth.AuthResponseDTO;
import com.secretpanda.codenames.dto.auth.LoginRequestDTO;
import com.secretpanda.codenames.service.AuthService;

/**
 * Controlador de autenticación.
 *
 * Expone un único endpoint público que actúa como puerta de entrada
 * para cualquier usuario, tanto si es la primera vez (registro)
 * como si ya tiene cuenta (login). La distinción la gestiona AuthService.
 *
 * Este endpoint está marcado como permitAll() en SecurityConfig,
 * por lo que no requiere JWT para ser invocado.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * Login / Registro con Google OAuth 2.0.
     *
     * El cliente (React o Android) obtiene previamente un idToken del SDK de
     * Google y lo envía aquí junto con el tag deseado.
     *
     * Casos:
     *   - Usuario nuevo  → se registra automáticamente y recibe JWT
     *   - Usuario existente → recibe JWT directamente
     *
     * @param dto { idToken: "<Google idToken>", tag: "NombreJugador" }
     * @return 200 con { token, perfil } si todo va bien
     *         400 si el tag está vacío o el idToken es inválido
     *         409 si el tag ya está en uso (solo en registro)
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody LoginRequestDTO dto) {
        AuthResponseDTO respuesta = authService.loginORegistrar(dto);
        return ResponseEntity.ok(respuesta);
    }
}