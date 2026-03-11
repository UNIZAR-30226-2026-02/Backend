package com.secretpanda.codenames.security;

import com.google.auth.oauth2.TokenVerifier;
import com.google.auth.oauth2.IdToken;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class GoogleAuthService {

    @Value("${google.client-id}")
    private String clientId;

    // Datos del jugador que extraemos del token de Google
    public record DatosGoogle(String idGoogle, String email, String nombre) {}

    // Verifica el token con los servidores de Google y devuelve los datos del jugador
    public DatosGoogle verificarToken(String idTokenString) {
        try {
            TokenVerifier verifier = TokenVerifier.newBuilder()
                    .setAudience(clientId)
                    .build();

            IdToken idToken = IdToken.parse(
                    com.google.auth.http.HttpTransportFactory.create(),
                    idTokenString
            );

            verifier.verify(idToken);

            // Extraemos los datos del jugador del token verificado
            String idGoogle = (String) idToken.getPayload().get("sub");
            String email    = (String) idToken.getPayload().get("email");
            String nombre   = (String) idToken.getPayload().get("name");

            return new DatosGoogle(idGoogle, email, nombre);

        } catch (Exception e) {
            throw new RuntimeException("Token de Google inválido o expirado: " + e.getMessage());
        }
    }
}