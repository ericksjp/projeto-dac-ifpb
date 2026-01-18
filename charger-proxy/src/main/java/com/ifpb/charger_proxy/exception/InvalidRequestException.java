package com.ifpb.charger_proxy.exception;

/**
 * Exceção lançada quando os dados da requisição são inválidos
 */
public class InvalidRequestException extends ChargerProxyException {
    
    public InvalidRequestException(String message) {
        super(message, "INVALID_REQUEST");
    }
    
    public InvalidRequestException(String field, String reason) {
        super(String.format("Invalid field '%s': %s", field, reason), "INVALID_REQUEST");
    }
}
