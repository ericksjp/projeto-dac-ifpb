package com.ifpb.charger_manager.domain.model;


import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Entidade que representa uma notificação de evento de pagamento
 */
@Table("payment_notifications")
public class PaymentNotification {
    
    @Id
    private UUID id;
    
    private UUID chargeId;
    
    private String eventType;
    private String externalEventId;
    
    // Payload JSON do evento
    private Map<String, Object> payload;
    
    private LocalDateTime receivedAt;
    private Boolean processed;
    private LocalDateTime processedAt;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getChargeId() {
        return chargeId;
    }

    public void setChargeId(UUID chargeId) {
        this.chargeId = chargeId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getExternalEventId() {
        return externalEventId;
    }

    public void setExternalEventId(String externalEventId) {
        this.externalEventId = externalEventId;
    }

    public Map<String, Object> getPayload() {
        return payload;
    }

    public void setPayload(Map<String, Object> payload) {
        this.payload = payload;
    }

    public LocalDateTime getReceivedAt() {
        return receivedAt;
    }

    public void setReceivedAt(LocalDateTime receivedAt) {
        this.receivedAt = receivedAt;
    }

    public Boolean getProcessed() {
        return processed;
    }

    public void setProcessed(Boolean processed) {
        this.processed = processed;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }
}
