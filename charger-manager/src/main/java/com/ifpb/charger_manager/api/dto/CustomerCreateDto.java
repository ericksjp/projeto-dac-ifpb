package com.ifpb.charger_manager.api.dto;

import com.ifpb.charger_manager.api.validation.ValidCpfCnpj;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;

/**
 * DTO para criação de cliente
 */
public class CustomerCreateDto {
    private String name;
    
    @ValidCpfCnpj
    private String cpfCnpj;
    private String email;

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
}
