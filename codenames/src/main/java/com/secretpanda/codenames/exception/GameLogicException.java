package com.secretpanda.codenames.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Esta excepción captura acciones que violan las reglas de negocio y las 
 * mecánicas internas del juego, independientemente de que la petición sea técnicamente válida.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class GameLogicException extends RuntimeException {
    public GameLogicException(String message) {
        super(message);
    }
}