package com.ifpb.charger_manager.exception;

/**
 * Exceção lançada quando há erro ao criar uma cobrança
 */
public class ChargeCreationException extends RuntimeException {
    public ChargeCreationException(String message) {
        super(message);
    }
    
    public ChargeCreationException(String message, Throwable cause) {
        super(message, cause);
    }
}
