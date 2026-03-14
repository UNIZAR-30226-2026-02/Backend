package com.secretpanda.codenames.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Esta excepción captura situaciones donde un recurso específico solicitado 
 * por el cliente no ha sido encontrado en la base de datos.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}