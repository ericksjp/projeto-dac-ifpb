package com.ifpb.charger_proxy.web.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import tools.jackson.databind.node.ObjectNode;

/**
 * Payload recebido do webhook do ASAAS
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AsaasWebhookPayload {

    @JsonProperty("id")
    private String id;

    @JsonProperty("event")
    private String event;

    @JsonProperty("dateCreated")
    private String dateCreated;

    @JsonProperty("payment")
    private ObjectNode payment;
}
