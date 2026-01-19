package com.ifpb.charger_proxy.domain.event;

/**
 * Tipos de eventos que o ASAAS pode enviar via webhook
 */
public enum WebhookEventType {
    
    // eventos de Pagamento
    PAYMENT_CREATED("Cobrança criada"),
    PAYMENT_UPDATED("Cobrança atualizada"),
    PAYMENT_CONFIRMED("Pagamento confirmado"),
    PAYMENT_RECEIVED("Pagamento recebido"),
    PAYMENT_ANTICIPATED("Pagamento antecipado"),
    PAYMENT_OVERDUE("Cobrança vencida"),
    PAYMENT_DELETED("Cobrança deletada"),
    PAYMENT_RESTORED("Cobrança restaurada"),
    PAYMENT_REFUNDED("Pagamento estornado"),
    PAYMENT_RECEIVED_IN_CASH_UNDONE("Recebimento em dinheiro desfeito"),
    PAYMENT_CHARGEBACK_REQUESTED("Chargeback solicitado"),
    PAYMENT_CHARGEBACK_DISPUTE("Chargeback em disputa"),
    PAYMENT_AWAITING_CHARGEBACK_REVERSAL("Aguardando reversão de chargeback"),
    PAYMENT_DUNNING_RECEIVED("Negativação recebida"),
    PAYMENT_DUNNING_REQUESTED("Negativação solicitada"),
    PAYMENT_BANK_SLIP_VIEWED("Boleto visualizado"),
    PAYMENT_CHECKOUT_VIEWED("Checkout visualizado"),
    
    // Outros
    UNKNOWN("Evento desconhecido");
    
    private final String description;
    
    WebhookEventType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * Converte string para enum, retornando UNKNOWN se não encontrar
     */
    public static WebhookEventType fromString(String event) {
        if (event == null) {
            return UNKNOWN;
        }
        
        try {
            return WebhookEventType.valueOf(event.toUpperCase());
        } catch (IllegalArgumentException e) {
            return UNKNOWN;
        }
    }
}
