package com.secretpanda.codenames.security;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

class JwtServiceTest {

    private JwtService jwtService;
    private final String secretKeyString = "EstaEsUnaClaveSecretaMuyLargaYSeguraParaPoderFirmarElTokenJWT123456";

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", secretKeyString);
        ReflectionTestUtils.setField(jwtService, "expirationMs", 86400000L);
    }

    @Test
    void testGenerarYExtraerToken_Valido() {
        String idGoogle = "usuario_seguro_123";
        String token = jwtService.generarToken(idGoogle);
        assertNotNull(token);

        String extractedId = jwtService.extraerIdGoogle(token);
        assertEquals(idGoogle, extractedId);
        
        assertTrue(jwtService.esTokenValido(token));
    }

    @Test
    void testEsTokenValido_InvalidoSiEstaCorrupto() {
        String tokenMalo = "ey...tokenFalso";
        assertFalse(jwtService.esTokenValido(tokenMalo));
    }
}
