package com.ifpb.charger_proxy.web.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO para transferência de dados de eventos de pagamento
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentEventDto {
    private UUID id;
    private String providerEventId;
    private String eventType;
    private String paymentRawPayload;
    private LocalDateTime receivedAt;
}

