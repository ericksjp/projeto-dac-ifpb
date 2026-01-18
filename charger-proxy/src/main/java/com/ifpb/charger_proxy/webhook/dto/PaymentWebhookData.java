package com.ifpb.charger_proxy.webhook.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Dados do pagamento recebidos via webhook
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentWebhookData {

    /**
     * ID da cobrança no ASAAS
     */
    @JsonProperty("id")
    private String id;

    /**
     * ID do cliente no ASAAS
     */
    @JsonProperty("customer")
    private String customer;

    /**
     * tipo de cobrança (PIX, BOLETO, CREDIT_CARD, etc.)
     */
    @JsonProperty("billingType")
    private String billingType;

    /**
     * valor da cobrança
     */
    @JsonProperty("value")
    private BigDecimal value;

    /**
     * valor líquido
     */
    @JsonProperty("netValue")
    private BigDecimal netValue;

    /**
     * Status da cobrança
     * PENDING, CONFIRMED, RECEIVED, OVERDUE, REFUNDED, etc.
     */
    @JsonProperty("status")
    private String status;

    /**
     * data de vencimento (formato: yyyy-MM-dd)
     */
    @JsonProperty("dueDate")
    private String dueDate;

    /**
     * data de confirmação do pagamento (formato: yyyy-MM-dd)
     */
    @JsonProperty("paymentDate")
    private String paymentDate;

    /**
     * data de confirmação
     */
    @JsonProperty("confirmedDate")
    private String confirmedDate;

    /**
     * descrição da cobrança
     */
    @JsonProperty("description")
    private String description;

    /**
     * url do fatura
     */
    @JsonProperty("invoiceUrl")
    private String invoiceUrl;

    /**
     * número de parcelas (se aplicável)
     */
    @JsonProperty("installmentCount")
    private Integer installmentCount;

    /**
     * dados do PIX (se billingType = PIX)
     */
    @JsonProperty("pixTransaction")
    private String pixTransaction;

    /**
     * código de barras do boleto (se billingType = BOLETO)
     */
    @JsonProperty("bankSlipUrl")
    private String bankSlipUrl;
}
