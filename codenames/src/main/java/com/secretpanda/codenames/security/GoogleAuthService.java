package com.secretpanda.codenames.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import com.google.api.client.json.webtoken.JsonWebToken;
import com.google.auth.oauth2.TokenVerifier;
import com.google.auth.oauth2.TokenVerifier.VerificationException;

/**
 * Verifica el idToken que el cliente obtiene del SDK de Google
 * y extrae los datos del jugador (sub, email, name).
 *
 * Usa google-auth-library-oauth2-http (declarada en pom.xml).
 * TokenVerifier.verify() valida firma, expiración y audience en un solo paso.
 */
@Component
public class GoogleAuthService {

    @Value("${google.client-id}")
    private String clientId;

    public record DatosGoogle(String idGoogle, String email, String nombre) {}

    public DatosGoogle verificarToken(String idTokenString) {
        try {
            TokenVerifier verifier = TokenVerifier.newBuilder()
                    .setAudience(clientId)
                    .build();

            // verify() devuelve el JsonWebToken si es válido,
            // lanza VerificationException si no lo es
            JsonWebToken token = verifier.verify(idTokenString);

            JsonWebToken.Payload payload = token.getPayload();

            String idGoogle = (String) payload.get("sub");
            String email    = (String) payload.get("email");
            String nombre   = (String) payload.get("name");

            return new DatosGoogle(idGoogle, email, nombre);

        } catch (VerificationException e) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Token de Google inválido o expirado: " + e.getMessage()
            );
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error al verificar el token de Google: " + e.getMessage()
            );
        }
    }
}