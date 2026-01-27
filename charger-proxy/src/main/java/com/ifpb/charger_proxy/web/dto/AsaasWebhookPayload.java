package com.ifpb.charger_proxy.web.dto;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

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
    private Map<String, Object> payload;
}
