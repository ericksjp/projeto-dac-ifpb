package com.ifpb.charger_proxy.exception;

/**
 * Exceção lançada quando um cliente não é encontrado
 */
public class CustomerNotFoundException extends ChargerProxyException {
    
    public CustomerNotFoundException(String customerId) {
        super("Customer not found: " + customerId, "CUSTOMER_NOT_FOUND");
    }
}
