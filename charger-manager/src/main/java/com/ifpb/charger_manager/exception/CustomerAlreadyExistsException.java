package com.ifpb.charger_manager.exception;

/**
 * Exceção lançada quando um cliente já está registrado
 */
public class CustomerAlreadyExistsException extends RuntimeException {
    public CustomerAlreadyExistsException(String message) {
        super(message);
    }
}
