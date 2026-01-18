package com.ifpb.charger_proxy.webhook.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Payload recebido do webhook do ASAAS
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AsaasWebhookPayload {

    /**
     * tipo do evento
     * Ex: PAYMENT_CREATED, PAYMENT_CONFIRMED, PAYMENT_RECEIVED, etc.
     */
    @JsonProperty("event")
    private String event;

    /**
     * Dados do pagamento/cobrança
     */
    @JsonProperty("payment")
    private PaymentWebhookData payment;
}
