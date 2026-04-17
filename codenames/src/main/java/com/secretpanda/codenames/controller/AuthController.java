package com.secretpanda.codenames.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.secretpanda.codenames.dto.auth.AuthResponseDTO;
import com.secretpanda.codenames.dto.auth.LoginRequestDTO;
import com.secretpanda.codenames.dto.auth.RegistroRequestDTO;
import com.secretpanda.codenames.service.AuthService;

import jakarta.servlet.http.HttpServletResponse;

/**
 * POST /api/auth/login   → Google idToken → esNuevo + (si no nuevo) JWT + Jugador
 * POST /api/auth/registro → id_google + tag → crea jugador + JWT + Jugador
 * POST /api/auth/logout  → invalida cookie
 * PUT  /api/auth/desactivar → borrado lógico
 *
 * COOKIE HttpOnly:  "token_sesion"   (solo cuando esNuevo = false o tras registro)
 * BODY JSON:        campo "token"     (siempre, para WebSocket sessionStorage)
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final String COOKIE_NAME = "token_sesion";
    private static final long   MAX_AGE_SEC = 86_400L; // 1 día

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // ─── Login ────────────────────────────────────────────────────────────────

    /**
     * Recibe el idToken de Google.
     * Si es nuevo: esNuevo=true, sin JWT ni Jugador → frontend va a /registro.
     * Si ya existe: esNuevo=false, devuelve JWT + Jugador + cookie HttpOnly.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(
            @RequestBody LoginRequestDTO dto,
            HttpServletResponse response) {

        AuthResponseDTO respuesta = authService.login(dto.getIdGoogle());

        // Solo emitimos la cookie cuando el jugador ya existe
        if (!respuesta.isEsNuevo()) {
            setCookie(response, respuesta.getToken());
        }

        return ResponseEntity.ok(respuesta);
    }

    // ─── Registro ─────────────────────────────────────────────────────────────

    /**
     * Crea el jugador nuevo (ya verificado con Google en el login previo).
     * Devuelve JWT + Jugador completo + cookie HttpOnly.
     */
    @PostMapping("/registro")
    public ResponseEntity<AuthResponseDTO> registro(
            @RequestBody RegistroRequestDTO dto,
            HttpServletResponse response) {

        AuthResponseDTO respuesta = authService.registro(dto.getIdGoogle(), dto.getTag());
        setCookie(response, respuesta.getToken());
        return ResponseEntity.ok(respuesta);
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private void setCookie(HttpServletResponse response, String token) {
        org.springframework.http.ResponseCookie cookie = org.springframework.http.ResponseCookie.from(COOKIE_NAME, token)
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")
                .maxAge(MAX_AGE_SEC)
                .build();
        response.addHeader(org.springframework.http.HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void invalidateCookie(HttpServletResponse response) {
        org.springframework.http.ResponseCookie cookie = org.springframework.http.ResponseCookie.from(COOKIE_NAME, "")
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")
                .maxAge(0)
                .build();
        response.addHeader(org.springframework.http.HttpHeaders.SET_COOKIE, cookie.toString());
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        invalidateCookie(response);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/desactivar")
    public ResponseEntity<Void> desactivar(
            jakarta.servlet.http.HttpServletRequest request,
            HttpServletResponse response) {

        String idGoogle = extraerIdGoogleDeCookie(request);
        authService.desactivarCuenta(idGoogle);
        invalidateCookie(response);
        return ResponseEntity.noContent().build();
    }

    /**
     * Extrae el idGoogle de la cookie HttpOnly a través del JwtFilter ya ejecutado.
     * El SecurityContext ya tiene el Principal tras pasar el JwtFilter.
     */
    private String extraerIdGoogleDeCookie(jakarta.servlet.http.HttpServletRequest request) {
        // El JwtFilter ya inyectó el idGoogle como principal
        java.security.Principal principal = request.getUserPrincipal();
        if (principal == null) {
            throw new com.secretpanda.codenames.exception.BadRequestException("No autenticado");
        }
        return principal.getName();
    }
}
