package com.ifpb.charger_proxy.exception;

/**
 * Exceção lançada quando ocorre erro na criação de uma cobrança
 */
public class ChargeCreationException extends ChargerProxyException {
    
    public ChargeCreationException(String message) {
        super(message, "CHARGE_CREATION_ERROR");
    }
    
    public ChargeCreationException(String message, Throwable cause) {
        super(message, "CHARGE_CREATION_ERROR", cause);
    }
}
