package com.secretpanda.codenames.exception;

import org.springframework.http.HttpStatus;

/**
 * Catálogo oficial de errores del sistema Código Secreto.
 * Mapea códigos de error con su estado HTTP y mensaje predeterminado.
 */
public enum ErrorCode {
    // 9.1. Sesión y Seguridad
    SESSION_INVALIDATED(HttpStatus.FORBIDDEN, "Se ha iniciado sesión en otro dispositivo."),
    GOOGLE_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "El token de Google ha caducado."),
    INACTIVE_ACCOUNT(HttpStatus.FORBIDDEN, "La cuenta ha sido desactivada/borrada."),

    // 9.2. Gestión de Partidas
    PLAYER_ALREADY_IN_GAME(HttpStatus.CONFLICT, "El usuario ya tiene una partida activa."),
    LOBBY_FULL(HttpStatus.CONFLICT, "La sala ha alcanzado el límite de jugadores."),
    GAME_ALREADY_STARTED(HttpStatus.CONFLICT, "La partida ya ha comenzado."),
    INVALID_ROOM_CODE(HttpStatus.NOT_FOUND, "El código de sala privada no existe."),
    MISSING_THEME_PACK(HttpStatus.FORBIDDEN, "No tienes el pack de cartas necesario."),
    TEAM_UNBALANCED(HttpStatus.BAD_REQUEST, "No hay suficientes jugadores en un equipo."),

    // 9.3. Gameplay
    NOT_YOUR_TURN(HttpStatus.CONFLICT, "Intento de acción fuera de turno."),
    INVALID_ROLE_ACTION(HttpStatus.FORBIDDEN, "Acción no permitida para tu rol (ej: Líder votando)."),
    INVALID_PHASE_ACTION(HttpStatus.CONFLICT, "Votar en fase de pista o viceversa."),
    WORD_ALREADY_REVEALED(HttpStatus.CONFLICT, "La carta seleccionada ya ha sido descubierta."),

    // 9.4. Social y Perfil
    TAG_TAKEN(HttpStatus.BAD_REQUEST, "El nombre de usuario ya existe."),
    PROFANITY_DETECTED(HttpStatus.BAD_REQUEST, "Uso de lenguaje no permitido."),
    ALREADY_FRIENDS(HttpStatus.BAD_REQUEST, "Ya existe una relación de amistad."),

    // 9.5. Tienda e Inventario
    INSUFFICIENT_FUNDS(HttpStatus.BAD_REQUEST, "No tienes suficientes balas."),
    ITEM_NOT_OWNED(HttpStatus.FORBIDDEN, "Intento de equipar algo no comprado."),
    ALREADY_OWNED(HttpStatus.BAD_REQUEST, "El objeto ya está en el inventario."),

    // 9.6. Errores de Sistema
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Fallo inesperado en el servidor."),
    
    // Genéricos (Fallback)
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "Petición incorrecta."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "Recurso no encontrado.");

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
