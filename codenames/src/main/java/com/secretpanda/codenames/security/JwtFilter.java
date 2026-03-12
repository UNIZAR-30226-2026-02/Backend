package com.secretpanda.codenames.security;

import java.io.IOException;
import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Filtro JWT que se ejecuta una sola vez por petición HTTP.
 *
 * Flujo:
 *   1. Extrae el token del header "Authorization: Bearer <token>"
 *   2. Lo valida con JwtService
 *   3. Si es válido, autentica la petición en el SecurityContext
 *      para que Spring Security la deje pasar al controlador.
 */
@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String token = extraerToken(request);

        // Si hay token y es válido, autenticamos la petición
        if (token != null && jwtService.esTokenValido(token)) {

            String idGoogle = jwtService.extraerIdGoogle(token);

            // Creamos el objeto de autenticación con el idGoogle como principal.
            // No necesitamos roles por ahora (lista vacía de authorities).
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(
                            idGoogle,
                            null,
                            Collections.emptyList()
                    );

            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // Registramos la autenticación en el contexto de Spring Security
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        // Continuamos con el siguiente filtro de la cadena
        filterChain.doFilter(request, response);
    }

    /**
     * Extrae el token JWT del header Authorization.
     * Devuelve null si el header no existe o no empieza por "Bearer ".
     */
    private String extraerToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}