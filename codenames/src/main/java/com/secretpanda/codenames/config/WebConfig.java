package com.secretpanda.codenames.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuración CORS para la API REST.
 *
 * Permite peticiones desde:
 *   - El cliente web React (localhost en desarrollo, dominio de producción)
 *   - El cliente móvil Android (no necesita CORS real, pero es buena práctica
 *     incluir el dominio de producción por si se usa una WebView)
 *
 * El origen concreto se inyecta desde application.properties mediante la
 * propiedad "cors.allowed-origins" para facilitar el cambio entre entornos
 * sin recompilar. Ejemplo en application.properties:
 *
 *   cors.allowed-origins=http://localhost:3000,https://tudominio.com
 */
@Configuration
public class WebConfig {

    /**
     * Orígenes permitidos, separados por comas.
     * Se puede sobreescribir con una variable de entorno: CORS_ALLOWED_ORIGINS
     */
    @Value("${cors.allowed-origins:http://localhost:3000,http://localhost:5173}")
    private String[] allowedOrigins;

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {

            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")          // Cubre todos los endpoints REST
                        .allowedOrigins(allowedOrigins)
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        // Necesario para que el cliente pueda leer el header Authorization
                        .exposedHeaders("Authorization")
                        .allowCredentials(true)
                        .maxAge(3600); // Cachea la preflight response 1 hora
            }
        };
    }
}