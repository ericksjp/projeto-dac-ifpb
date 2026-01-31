package com.ifpb.charger_manager.domain.model;



import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidade que representa um cliente no sistema
 */
@Table("customers")
public class Customer implements Persistable<UUID> {
    
    @Id
    private UUID id;

    @Transient
    private boolean isNew = true;

    public Customer() {
        this.isNew = true;
    }

    @PersistenceCreator
    public Customer(UUID id, String externalId, String name, String email, String cpfCnpj, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.externalId = externalId;
        this.name = name;
        this.email = email;
        this.cpfCnpj = cpfCnpj;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.isNew = false;
    }
    
    private String externalId;  // ID do cliente no Asaas
    
    private String name;
    private String email;
    private String cpfCnpj;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Override
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCpfCnpj() {
        return cpfCnpj;
    }

    public void setCpfCnpj(String cpfCnpj) {
        this.cpfCnpj = cpfCnpj;
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

    @Override
    @Transient
    public boolean isNew() {
        return isNew;
    }

    public void setNew(boolean isNew) {
        this.isNew = isNew;
    }
}
