package com.secretpanda.codenames.Unitarios.security;

import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.secretpanda.codenames.config.SecurityConfig;
import com.secretpanda.codenames.controller.JugadorController;
import com.secretpanda.codenames.controller.TiendaController;
import com.secretpanda.codenames.dto.jugador.JugadorDTO;
import com.secretpanda.codenames.repository.JugadorRepository;
import com.secretpanda.codenames.security.JwtService;
import com.secretpanda.codenames.service.JugadorService;
import com.secretpanda.codenames.service.TiendaService;

import jakarta.servlet.http.Cookie;

/**
 * Tests de seguridad que validan el JwtFilter y la SecurityFilterChain.
 *
 *
 * BEANS CARGADOS AUTOMÁTICAMENTE POR @WebMvcTest:
 *   - JugadorController, TiendaController  (especificados explícitamente)
 *   - SecurityConfig                       (@Configuration + @EnableWebSecurity)
 *   - JwtFilter                            (@Component + OncePerRequestFilter)
 *
 * BEANS QUE REQUIEREN MOCK:
 *   - JwtService, JugadorRepository   → dependencias de JwtFilter
 *   - JugadorService, TiendaService   → dependencias de los controllers
 */
@WebMvcTest(controllers = {JugadorController.class, TiendaController.class})
@Import(SecurityConfig.class)
public class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    // ── Dependencias de JwtFilter ─────────────────────────────────────────────

    @MockitoBean
    private JwtService jwtService;

    // @MockitoBean sobre JugadorRepository es correcto porque
    // @WebMvcTest NO carga JPA, así que no hay proxy factory que conflicte.
    @MockitoBean
    private JugadorRepository jugadorRepository;

    // ── Dependencias de los controllers ──────────────────────────────────────

    @MockitoBean
    private JugadorService jugadorService;

    // El endpoint /api/temas/activos está mapeado en TiendaController.
    @MockitoBean
    private TiendaService tiendaService;

    // ── Tests ─────────────────────────────────────────────────────────────────

    /**
     * Verifica que /api/temas/activos es accesible sin autenticación.
     * SecurityConfig lo declara como .requestMatchers(GET, "/api/temas/activos").permitAll()
     */
    @Test
    public void shouldAllowAccessToPublicEndpointsWithoutAuth() throws Exception {
        // El endpoint es público y llama a tiendaService.getTemasTienda(null)
        // (principal es null cuando no hay autenticación)
        when(tiendaService.getTemasTienda(null)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/temas/activos"))
                .andExpect(status().isOk());
    }

    /**
     * Verifica que un endpoint protegido devuelve 403 sin token.
     *
     * SecurityConfig no configura un authenticationEntryPoint explícito.
     * Sin formLogin() ni httpBasic(), Spring Security usa por defecto
     * Http403ForbiddenEntryPoint → 403 (no 401).
     */
    @Test
    public void shouldReturn403WhenAccessingProtectedEndpointWithoutToken() throws Exception {
        mockMvc.perform(get("/api/jugadores"))
                .andExpect(status().isForbidden());
    }

    /**
     * Verifica que un token con firma inválida resulta en 403.
     *
     * JwtFilter llama a jwtService.esTokenValido() → false
     * → no autentica → Spring Security rechaza con 403.
     *
     *  No hace falta stubear extraerIdGoogle() porque
     * JwtFilter hace un cortocircuito si esTokenValido() devuelve false
     * y nunca llama a extraerIdGoogle(). Stubear un método que no se llama
     * no rompe nada, pero es código muerto confuso.
     */
    @Test
    public void shouldReturn403WhenTokenIsInvalid() throws Exception {
        when(jwtService.esTokenValido("token_invalido")).thenReturn(false);

        Cookie invalidCookie = new Cookie("token_sesion", "token_invalido");

        mockMvc.perform(get("/api/jugadores").cookie(invalidCookie))
                .andExpect(status().isForbidden());
    }

    /**
     * Verifica que un token válido permite el acceso a un endpoint protegido.
     *
     * Flujo del JwtFilter con token válido:
     *   1. esTokenValido(token)        → true
     *   2. extraerIdGoogle(token)      → userId
     *   3. findTokenActualById(userId) → Optional.of(token)  ← control de sesión única
     *   4. token.equals(tokenEnBD)     → true → autentica el Principal
     *
     * Tras pasar el filtro, JugadorController.getPerfil() se ejecuta
     * y llama a jugadorService.getPerfil(userId), que mockeamos para
     * que devuelva un DTO válido y el controller pueda responder 200.
     */
    @Test
    public void shouldAllowAccessWithValidToken() throws Exception {
        final String validToken = "token_valido";
        final String userId     = "user123";

        // Mock del filtro JWT — los 3 pasos que verifica JwtFilter
        when(jwtService.esTokenValido(validToken)).thenReturn(true);
        when(jwtService.extraerIdGoogle(validToken)).thenReturn(userId);
        when(jugadorRepository.findTokenActualById(userId)).thenReturn(Optional.of(validToken));

        // Mock del service del controller — necesario para que el endpoint
        // responda 200 y no 500 por NullPointerException en el controller.
        //  sin este stub, jugadorService.getPerfil() devuelve null,
        // ResponseEntity.ok(null) serializa a body vacío pero puede lanzar NPE
        // si JugadorDTO tiene campos @NotNull marcados por Jackson.
        JugadorDTO jugadorDTO = new JugadorDTO();
        when(jugadorService.getPerfil(userId)).thenReturn(jugadorDTO);

        Cookie validCookie = new Cookie("token_sesion", validToken);

        mockMvc.perform(get("/api/jugadores").cookie(validCookie))
                .andExpect(status().isOk());
    }
}