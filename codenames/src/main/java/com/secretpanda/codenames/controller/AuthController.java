package com.secretpanda.codenames.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.secretpanda.codenames.dto.auth.AuthResponseDTO;
import com.secretpanda.codenames.dto.auth.LoginRequestDTO;
import com.secretpanda.codenames.service.AuthService;

import jakarta.servlet.http.HttpServletResponse;

/**
 * Controlador de autenticación — POST /api/auth/login
 *
 * Tras verificar el token de Google devuelve el JWT de dos formas:
 *
 *   1. Cookie HttpOnly "token_sesion" → React web.
 *      Set-Cookie: token_sesion=<jwt>; HttpOnly; Secure; SameSite=Strict
 *      El navegador la adjunta automáticamente con credentials:"include".
 *
 *   2. Campo "token" en el body JSON → Android.
 *      La app lee el token del body y lo guarda en su storage local.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Value("${jwt.expiration-ms:86400000}")
    private long jwtExpirationMs;

    /**
     * Login / Registro con Google OAuth 2.0.
     *
     * @param dto  { "id_google": "<token de Google>" }
     * @return 200 con el JUGADOR completo + token JWT
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(
            @RequestBody LoginRequestDTO dto,
            HttpServletResponse httpResponse) {

        AuthResponseDTO respuesta = authService.loginORegistrar(dto);

        // Emitir cookie HttpOnly para React
        // SameSite=Strict via header manual (Jakarta Servlet 6 no tiene setSameSite)
        httpResponse.setHeader("Set-Cookie",
                String.format("token_sesion=%s; HttpOnly; Secure; SameSite=Strict; Path=/; Max-Age=%d",
                        respuesta.getToken(),
                        jwtExpirationMs / 1000));

        // El token también va en el body para Android
        return ResponseEntity.ok(respuesta);
    }

    /**
     * Logout — invalida la cookie en el navegador.
     * Android simplemente descarta su token local.
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse httpResponse) {
        httpResponse.setHeader("Set-Cookie",
                "token_sesion=; HttpOnly; Secure; SameSite=Strict; Path=/; Max-Age=0");
        return ResponseEntity.noContent().build();
    }
}

