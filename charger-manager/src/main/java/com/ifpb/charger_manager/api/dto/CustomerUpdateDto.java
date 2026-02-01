package com.ifpb.charger_manager.api.dto;

import jakarta.validation.constraints.Email;

/**
 * DTO para atualização de cliente
 */
public class CustomerUpdateDto {
    private String name;
    
    @Email(message = "Email deve ser válido")
    private String email;

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
}
