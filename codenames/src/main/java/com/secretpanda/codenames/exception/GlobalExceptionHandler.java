package com.secretpanda.codenames.exception;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Clase centralizada para interceptar y procesar todas las excepciones lanzadas por la aplicación,
 * transformándolas en respuestas HTTP estandarizadas con formato JSON según el contrato API.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Gestiona la excepción base del sistema para devolver el formato exacto del contrato.
     */
    @ExceptionHandler(SecretPandaException.class)
    public ResponseEntity<Object> handleSecretPandaException(SecretPandaException ex) {
        return buildResponse(ex.getMessage(), 
                             ex.getErrorCode().name(), 
                             ex.getErrorCode().getStatus(), 
                             ex.getData());
    }

    /**
     * Intercepta y desglosa los fallos de validación de campos.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> 
            errors.put(error.getField(), error.getDefaultMessage())
        );

        return buildResponse("Error de validación en los campos enviados.", 
                             "VALIDATION_ERROR", 
                             HttpStatus.BAD_REQUEST, 
                             errors);
    }

    /**
     * Captura cualquier otra excepción no prevista.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGlobalException(Exception ex) {
        return buildResponse("Error interno: " + ex.getMessage(), 
                             ErrorCode.INTERNAL_SERVER_ERROR.name(), 
                             HttpStatus.INTERNAL_SERVER_ERROR, 
                             null);
    }

    /**
     * Método interno para unificar la estructura de respuesta de error.
     * Sigue estrictamente el formato del apartado 9 del contrato API.
     */
    private ResponseEntity<Object> buildResponse(String message, String errorCode, HttpStatus status, Object details) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", status.value());
        body.put("error_code", errorCode);
        body.put("message", message);
        if (details != null && (!(details instanceof Map) || !((Map<?, ?>) details).isEmpty())) {
            body.put("details", details);
        }
        return new ResponseEntity<>(body, status);
    }
}
