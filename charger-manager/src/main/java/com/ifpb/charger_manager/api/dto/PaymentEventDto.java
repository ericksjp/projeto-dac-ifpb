package com.ifpb.charger_manager.api.dto;



import java.util.Map;

/**
 * DTO para notificação de evento de pagamento
 */
public class PaymentEventDto {
    private String eventType;
    private String externalEventId;
    private String chargeExternalId;
    private Map<String, Object> payload;

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

    public String getChargeExternalId() {
        return chargeExternalId;
    }

    public void setChargeExternalId(String chargeExternalId) {
        this.chargeExternalId = chargeExternalId;
    }

    public Map<String, Object> getPayload() {
        return payload;
    }

    public void setPayload(Map<String, Object> payload) {
        this.payload = payload;
    }
}
