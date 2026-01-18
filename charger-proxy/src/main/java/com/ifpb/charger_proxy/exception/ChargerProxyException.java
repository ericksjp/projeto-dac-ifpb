package com.ifpb.charger_proxy.exception;

/**
 * Exceção base para erros relacionados ao Charger Proxy
 */
public class ChargerProxyException extends RuntimeException {
    
    private final String errorCode;
    
    public ChargerProxyException(String message) {
        super(message);
        this.errorCode = "CHARGER_PROXY_ERROR";
    }
    
    public ChargerProxyException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public ChargerProxyException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "CHARGER_PROXY_ERROR";
    }
    
    public ChargerProxyException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
}
