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
 * Usa exclusivamente google-auth-library-oauth2-http (ya declarada en pom.xml).
 *
 * Flujo correcto con TokenVerifier:
 *   1. Construir el verifier con nuestro client-id como audience
 *   2. Llamar a verifier.verify(tokenString) → devuelve JsonWebToken si es válido
 *                                             → lanza VerificationException si no
 *   3. Extraer sub, email y name del payload
 */
@Component
public class GoogleAuthService {

    @Value("${google.client-id}")
    private String clientId;

    // Datos del jugador que extraemos del token de Google
    public record DatosGoogle(String idGoogle, String email, String nombre) {}

    /**
     * Verifica el idToken con los servidores de Google y devuelve los datos del jugador.
     *
     * @param idTokenString el token JWT que devuelve el SDK de Google al cliente
     * @return DatosGoogle con idGoogle (sub), email y nombre
     * @throws ResponseStatusException 401 si el token es inválido o ha expirado
     */
    public DatosGoogle verificarToken(String idTokenString) {

        try {
            // Construimos el verificador con nuestro client-id como audience.
            // TokenVerifier valida firma, expiración y audience en un solo paso.
            TokenVerifier verifier = TokenVerifier.newBuilder()
                    .setAudience(clientId)
                    .build();

            // verify() devuelve el JsonWebToken si todo es correcto,
            // o lanza VerificationException si el token es inválido o ha expirado.
            JsonWebToken token = verifier.verify(idTokenString);

            // Extraemos los datos del jugador del payload
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
                    "Error inesperado al verificar el token de Google: " + e.getMessage()
            );
        }
    }
}