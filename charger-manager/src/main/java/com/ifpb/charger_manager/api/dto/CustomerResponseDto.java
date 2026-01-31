package com.ifpb.charger_manager.api.dto;



import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO para resposta de cliente
 */
public class CustomerResponseDto {
    private UUID id;
    private String externalId;
    private String name;
    private String cpfCnpj;
    private String email;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public CustomerResponseDto() {
    }

    public CustomerResponseDto(UUID id, String externalId, String name, String cpfCnpj, String email, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.externalId = externalId;
        this.name = name;
        this.cpfCnpj = cpfCnpj;
        this.email = email;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCpfCnpj() {
        return cpfCnpj;
    }

    public void setCpfCnpj(String cpfCnpj) {
        this.cpfCnpj = cpfCnpj;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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
}
