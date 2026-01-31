package com.ifpb.charger_manager.domain.enums;

/**
 * Enumeração dos possíveis status de uma cobrança
 */
public enum ChargeStatus {
    PENDING,      // Aguardando pagamento
    CONFIRMED,    // Pagamento confirmado (em processamento)
    RECEIVED,     // Pagamento recebido e confirmado
    OVERDUE,      // Cobrança vencida
    REFUNDED,     // Pagamento reembolsado
    CANCELLED     // Cobrança cancelada
}
