package com.secretpanda.codenames.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Excepción para recursos no encontrados.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class NotFoundException extends SecretPandaException {
    public NotFoundException(String message) {
        super(ErrorCode.NOT_FOUND, message);
    }
    
    public NotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }
}
