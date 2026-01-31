package com.ifpb.charger_manager.exception;

/**
 * Exceção lançada quando um cliente não é encontrado
 */
public class CustomerNotFoundException extends RuntimeException {
    public CustomerNotFoundException(String message) {
        super(message);
    }
}
