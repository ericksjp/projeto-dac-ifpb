package com.ifpb.charger_manager.domain.model;

import com.ifpb.charger_manager.domain.enums.BillingType;
import com.ifpb.charger_manager.domain.enums.ChargeStatus;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidade que representa uma cobrança no sistema
 */
@Table("charges")
public class Charge implements Persistable<UUID> {
    
    @Id
    private UUID id;

    @Transient
    private boolean isNew = true;

    public Charge() {
        this.isNew = true;
    }

    @PersistenceCreator
    public Charge(UUID id, UUID customerId, String externalId, String installmentId, BillingType billingType, BigDecimal value, LocalDate dueDate, String description, ChargeStatus status, String invoiceUrl, String bankSlipUrl, String pixQrCode, Integer installmentCount, Integer installmentNumber, LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime cancelledAt) {
        this.id = id;
        this.customerId = customerId;
        this.externalId = externalId;
        this.installmentId = installmentId;
        this.billingType = billingType;
        this.value = value;
        this.dueDate = dueDate;
        this.description = description;
        this.status = status;
        this.invoiceUrl = invoiceUrl;
        this.bankSlipUrl = bankSlipUrl;
        this.pixQrCode = pixQrCode;
        this.installmentCount = installmentCount;
        this.installmentNumber = installmentNumber;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.cancelledAt = cancelledAt;
        this.isNew = false;
    }
    
    private UUID customerId;
    
    // IDs externos do Asaas
    private String externalId;
    private String installmentId;
    
    // Dados da cobrança
    private BillingType billingType;
    private BigDecimal value;
    private LocalDate dueDate;
    private String description;
    
    // Status
    private ChargeStatus status;
    
    // URLs de pagamento
    private String invoiceUrl;
    private String bankSlipUrl;
    private String pixQrCode;
    
    // Parcelamento
    private Integer installmentCount;
    private Integer installmentNumber;
    
    // Auditoria
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime cancelledAt;

    @Override
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public void setCustomerId(UUID customerId) {
        this.customerId = customerId;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public String getInstallmentId() {
        return installmentId;
    }

    public void setInstallmentId(String installmentId) {
        this.installmentId = installmentId;
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

    public ChargeStatus getStatus() {
        return status;
    }

    public void setStatus(ChargeStatus status) {
        this.status = status;
    }

    public String getInvoiceUrl() {
        return invoiceUrl;
    }

    public void setInvoiceUrl(String invoiceUrl) {
        this.invoiceUrl = invoiceUrl;
    }

    public String getBankSlipUrl() {
        return bankSlipUrl;
    }

    public void setBankSlipUrl(String bankSlipUrl) {
        this.bankSlipUrl = bankSlipUrl;
    }

    public String getPixQrCode() {
        return pixQrCode;
    }

    public void setPixQrCode(String pixQrCode) {
        this.pixQrCode = pixQrCode;
    }

    public Integer getInstallmentCount() {
        return installmentCount;
    }

    public void setInstallmentCount(Integer installmentCount) {
        this.installmentCount = installmentCount;
    }

    public Integer getInstallmentNumber() {
        return installmentNumber;
    }

    public void setInstallmentNumber(Integer installmentNumber) {
        this.installmentNumber = installmentNumber;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getCancelledAt() {
        return cancelledAt;
    }

    public void setCancelledAt(LocalDateTime cancelledAt) {
        this.cancelledAt = cancelledAt;
    }

    @Override
    @Transient
    public boolean isNew() {
        return isNew;
    }

    public void setNew(boolean isNew) {
        this.isNew = isNew;
    }
}
