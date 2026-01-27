package com.ifpb.charger_proxy.domain.model;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Representa um evento de pagamento recebido via webhook do asaas
 */
@Table("payment_events")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentEvent {

    @Id
    private UUID id;

    @Column("provider_event_id")
    private String providerEventId;

    @Column("event_type")
    private String eventType;

    /**
     * Payload JSON do webhook
     */
    @Column("payload")
    private Map<String, Object> payload;

    @Column("processed")
    private boolean processed;

    @Column("received_at")
    private LocalDateTime receivedAt;

    @Column("processed_at")
    private LocalDateTime processedAt;

    /**
     * controle de versão para optimistic locking
     */
    @Version
    private Long version;
}
