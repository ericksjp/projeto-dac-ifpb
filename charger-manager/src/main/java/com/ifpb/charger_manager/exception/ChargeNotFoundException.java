package com.ifpb.charger_manager.exception;

/**
 * Exceção lançada quando uma cobrança não é encontrada
 */
public class ChargeNotFoundException extends RuntimeException {
    public ChargeNotFoundException(String message) {
        super(message);
    }
}
