package com.secretpanda.codenames.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Excepción para violaciones de reglas de juego.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class GameLogicException extends SecretPandaException {
    public GameLogicException(String message) {
        super(ErrorCode.BAD_REQUEST, message); // Default to 400 instead of 500 for logic errors
    }
    
    public GameLogicException(ErrorCode errorCode) {
        super(errorCode);
    }
    
    public GameLogicException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
