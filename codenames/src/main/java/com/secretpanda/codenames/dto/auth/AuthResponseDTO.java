package com.secretpanda.codenames.dto.auth;

import com.secretpanda.codenames.model.Jugador;

public class AuthResponseDTO {
    
    // El token JWT generado por JwtService
    private String token;
    
    // Los datos del perfil de usuario para la pantalla de inicio
    private PerfilBasicoDTO perfil;

    // Constructor vacío
    public AuthResponseDTO() {
    }

    // Constructor con parámetros
    public AuthResponseDTO(String token, Jugador jugador) {
        this.token = token;
        this.perfil = new PerfilBasicoDTO(jugador);
    }

    // Getters y Setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public PerfilBasicoDTO getPerfil() {
        return perfil;
    }

    public void setPerfil(PerfilBasicoDTO perfil) {
        this.perfil = perfil;
    }

    // CLASE ANIDADA PARA EL PERFIL DEL USUARIO
    // Solo enviamos lo que hace falta al iniciar sesión
    public static class PerfilBasicoDTO {
        //Parámetros para el perfil
        private String idGoogle;
        private String tag;
        private String fotoPerfil;
        private Integer balas;
        private Integer victorias;

        // Constructor que recibe el jugador para manejar los datos
        public PerfilBasicoDTO(Jugador jugador) {
            this.idGoogle = jugador.getIdGoogle();
            this.tag = jugador.getTag();
            this.fotoPerfil = jugador.getFotoPerfil();
            this.balas = jugador.getBalas();
            this.victorias = jugador.getVictorias();
        }

        // Getters
        public String getIdGoogle() { return idGoogle; }

        public String getTag() { return tag; }

        public String getFotoPerfil() { return fotoPerfil; }

        public Integer getBalas() { return balas; }
        
        public Integer getVictorias() { return victorias; }
    }
}
