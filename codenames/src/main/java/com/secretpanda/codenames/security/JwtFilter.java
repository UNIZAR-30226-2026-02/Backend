package com.secretpanda.codenames.security;

import java.io.IOException;
import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.secretpanda.codenames.exception.ErrorCode;
import com.secretpanda.codenames.repository.JugadorRepository;

/**
 * Filtro JWT — se ejecuta una sola vez por petición HTTP.
 *
 * Busca el JWT en dos sitios por orden de prioridad:
 *
 * 1. Cookie "token_sesion" (HttpOnly) → React web.
 * El navegador la adjunta automáticamente con credentials:"include".
 *
 * 2. Header "Authorization: Bearer <token>" → Android.
 * La app lo añade manualmente en cada petición.
 */
@Component
public class JwtFilter extends OncePerRequestFilter {

    private static final String COOKIE_NAME = "token_sesion";

    @Autowired
    private JwtService jwtService;

    @Autowired
    private JugadorRepository jugadorRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // --- NUEVO: Dejar pasar la ruta pública de prueba directamente ---
        if ("/api/hello".equals(request.getServletPath())) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = extraerToken(request);

        if (token != null && jwtService.esTokenValido(token)) {

            String idGoogle = jwtService.extraerIdGoogle(token);

            // RNF-1: Control de Sesión Única
            // Consultamos directamente el token en BD para evitar la caché de primer nivel de JPA
            String tokenEnBD = jugadorRepository.findTokenActualById(idGoogle).orElse("");
            boolean sesionValida = token.equals(tokenEnBD);

            if (sesionValida) {
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(
                                idGoogle,
                                null,
                                Collections.emptyList()
                        );

                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);
            } else {
                // Si el token es válido pero NO es el actual en BD, es una sesión invalidada
                escribirErrorSesion(response);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Escribe la respuesta de error 403 SESSION_INVALIDATED directamente en el stream.
     */
    private void escribirErrorSesion(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String json = String.format(
            "{\"status\": 403, \"error_code\": \"SESSION_INVALIDATED\", \"message\": \"%s\"}",
            ErrorCode.SESSION_INVALIDATED.getMessage()
        );

        response.getWriter().write(json);
    }

    /**
     * Busca el JWT primero en la cookie HttpOnly (React),
     * y si no existe en el header Authorization (Android).
     */
    private String extraerToken(HttpServletRequest request) {

        // 1. Cookie token_sesion (React web)
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (COOKIE_NAME.equals(cookie.getName())) {
                    String valor = cookie.getValue();
                    if (valor != null && !valor.isBlank()) {
                        return valor;
                    }
                }
            }
        }

        // 2. Header Authorization: Bearer <token> (Android)
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        return null;
    }
}
