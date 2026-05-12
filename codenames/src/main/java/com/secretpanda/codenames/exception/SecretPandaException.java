package com.secretpanda.codenames.exception;

import java.util.Map;

/**
 * Excepción base para errores de negocio controlados, vinculada al catálogo oficial.
 */
public class SecretPandaException extends RuntimeException {
    private final ErrorCode errorCode;
    private final String customMessage;
    private final Map<String, Object> data;

    public SecretPandaException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.customMessage = null;
        this.data = null;
    }

    public SecretPandaException(ErrorCode errorCode, String customMessage) {
        super(customMessage);
        this.errorCode = errorCode;
        this.customMessage = customMessage;
        this.data = null;
    }

    public SecretPandaException(ErrorCode errorCode, Map<String, Object> data) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.customMessage = null;
        this.data = data;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public String getMessage() {
        return customMessage != null ? customMessage : errorCode.getMessage();
    }

    public Map<String, Object> getData() {
        return data;
    }
}
