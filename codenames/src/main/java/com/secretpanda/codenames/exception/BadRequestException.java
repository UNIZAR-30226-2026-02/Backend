package com.secretpanda.codenames.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Esta excepción captura peticiones del cliente que son sintácticamente 
 * incorrectas o que contienen parámetros que no cumplen con los requisitos técnicos.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}