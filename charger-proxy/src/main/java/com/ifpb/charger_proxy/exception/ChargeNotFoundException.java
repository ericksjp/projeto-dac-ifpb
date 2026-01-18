package com.ifpb.charger_proxy.exception;

/**
 * Exceção lançada quando uma cobrança não é encontrada
 */
public class ChargeNotFoundException extends ChargerProxyException {
    
    public ChargeNotFoundException(String chargeId) {
        super("Charge not found: " + chargeId, "CHARGE_NOT_FOUND");
    }
}
