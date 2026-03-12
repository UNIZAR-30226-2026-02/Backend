package com.secretpanda.codenames.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.secretpanda.codenames.security.JwtFilter;

/**
 * Configuración de Spring Security.
 *
 * Estrategia: stateless (sin sesiones HTTP). Toda autenticación se
 * basa en el JWT que el cliente incluye en cada petición.
 *
 * Rutas públicas (sin token):
 *   - POST /api/auth/**          → login / registro via Google OAuth
 *   - GET  /api/temas/activos    → catálogo de temas (pantalla de inicio)
 *   - GET  /ws/**                → handshake inicial del WebSocket (STOMP)
 *
 * El resto de rutas requieren un JWT válido.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            // Desactivamos CSRF: usamos JWT, no cookies de sesión
            .csrf(csrf -> csrf.disable())

            // CORS lo gestiona WebConfig, aquí solo lo habilitamos
            .cors(cors -> cors.configure(http))

            // Sin estado: Spring Security no crea ni usa sesiones HTTP
            .sessionManagement(session ->
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // Reglas de autorización
            .authorizeHttpRequests(auth -> auth

                // ── Autenticación ──────────────────────────────────────────
                // El endpoint de login/registro Google OAuth es público
                .requestMatchers(HttpMethod.POST, "/api/auth/**").permitAll()

                // ── Catálogo público ───────────────────────────────────────
                // Permite ver los temas disponibles sin estar logueado
                .requestMatchers(HttpMethod.GET, "/api/temas/activos").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/logros/activos").permitAll()

                // ── WebSocket (STOMP) ──────────────────────────────────────
                // El handshake HTTP del WebSocket debe ser accesible;
                // la autenticación real se realiza vía JwtChannelInterceptor
                .requestMatchers("/ws/**").permitAll()

                // ── Todo lo demás requiere JWT válido ──────────────────────
                .anyRequest().authenticated()
            )

            // Añadimos nuestro filtro JWT ANTES del filtro estándar de
            // usuario/contraseña de Spring Security
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}