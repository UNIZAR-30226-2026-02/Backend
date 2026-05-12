package com.secretpanda.codenames.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Excepción para peticiones mal formadas.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BadRequestException extends SecretPandaException {
    public BadRequestException(String message) {
        super(ErrorCode.BAD_REQUEST, message);
    }
    
    public BadRequestException(ErrorCode errorCode) {
        super(errorCode);
    }
}
