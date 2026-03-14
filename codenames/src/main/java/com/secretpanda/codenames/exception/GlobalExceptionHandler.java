package com.secretpanda.codenames.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Clase centralizada para interceptar y procesar todas las excepciones lanzadas por la aplicación,
 * transformándolas en respuestas HTTP estandarizadas con formato JSON.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Gestiona las excepciones de recursos no encontrados para devolver un error 404.
     */
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Object> handleNotFound(NotFoundException ex) {
        return buildResponse(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    /**
     * Gestiona las excepciones de peticiones mal formadas para devolver un error 400.
     */
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Object> handleBadRequest(BadRequestException ex) {
        return buildResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    /**
     * Gestiona las violaciones de las reglas de juego para devolver un error 409 (Conflicto).
     */
    @ExceptionHandler(GameLogicException.class)
    public ResponseEntity<Object> handleGameLogic(GameLogicException ex) {
        return buildResponse(ex.getMessage(), HttpStatus.CONFLICT);
    }

    /**
     * Intercepta y desglosa los fallos de validación de campos para informar detalladamente al cliente.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> 
            errors.put(error.getField(), error.getDefaultMessage())
        );

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Error de validación");
        body.put("details", errors);
        
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    /**
     * Captura cualquier otra excepción no prevista para evitar fugas de información y devolver un error 500.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGlobalException(Exception ex) {
        return buildResponse("Error interno en los sistemas de Secret Panda: " + ex.getMessage(), 
                             HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Método interno para unificar la estructura de respuesta de error en toda la API.
     */
    private ResponseEntity<Object> buildResponse(String message, HttpStatus status) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        return new ResponseEntity<>(body, status);
    }
}