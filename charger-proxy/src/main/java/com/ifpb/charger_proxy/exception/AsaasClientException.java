package com.ifpb.charger_proxy.exception;

/**
 * Exceção lançada quando ocorre erro na comunicação com a API do ASAAS
 */
public class AsaasClientException extends ChargerProxyException {
    
    public AsaasClientException(String message) {
        super(message, "ASAAS_API_ERROR");
    }
    
    public AsaasClientException(String message, Throwable cause) {
        super(message, "ASAAS_API_ERROR", cause);
    }
}
