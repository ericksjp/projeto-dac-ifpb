package com.ifpb.charger_manager.exception;

/**
 * Exceção lançada quando há dados inválidos na requisição
 */
public class InvalidRequestException extends RuntimeException {
    public InvalidRequestException(String message) {
        super(message);
    }
}
