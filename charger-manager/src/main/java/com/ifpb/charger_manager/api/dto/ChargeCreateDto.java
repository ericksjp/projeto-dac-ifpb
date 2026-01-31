package com.ifpb.charger_manager.api.dto;

import com.ifpb.charger_manager.domain.enums.BillingType;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO para criação de cobrança
 */
public class ChargeCreateDto {
    private UUID customerId;
    private BillingType billingType;
    private BigDecimal value;
    private LocalDate dueDate;
    private String description;
    private Integer installmentCount;

    public UUID getCustomerId() {
        return customerId;
    }

    public void setCustomerId(UUID customerId) {
        this.customerId = customerId;
    }

    public BillingType getBillingType() {
        return billingType;
    }

    public void setBillingType(BillingType billingType) {
        this.billingType = billingType;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getInstallmentCount() {
        return installmentCount;
    }

    public void setInstallmentCount(Integer installmentCount) {
        this.installmentCount = installmentCount;
    }
}
