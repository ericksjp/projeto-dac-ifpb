package com.ifpb.charger_proxy.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.asaas.apisdk.models.CustomerGetResponseDto;
import com.asaas.apisdk.models.CustomerSaveRequestDto;
import com.asaas.apisdk.services.CustomerService;

@Slf4j
@Service
@RequiredArgsConstructor
public class LocalCustomerService {

    private final CustomerService customerService; 

    /**
     * Cadastra um novo cliente no ASAAS
     * 
     * @param name        nome do cliente
     * @param email       email do cliente
     * @param cpfCnpj     CPF ou CNPJ do cliente
     * @param phone       telefone fixo
     * @param mobilePhone telefone celular
     * @return resposta do ASAAS com ID do cliente criado
     */
    public CustomerGetResponseDto createCustomer(
            String name,
            String email,
            String cpfCnpj,
            String phone,
            String mobilePhone) {

        log.info("Creating customer: {}", email);

        CustomerSaveRequestDto customerSaveRequestDto = CustomerSaveRequestDto.builder()
                .name(name)
                .email(email)
                .cpfCnpj(cpfCnpj)
                .phone(phone)
                .mobilePhone(mobilePhone)
                .notificationDisabled(false)
                .build();

        return customerService.createNewCustomer(customerSaveRequestDto);
    }

    /**
     * Cadastra um novo cliente no ASAAS com endereço completo
     */
    public CustomerGetResponseDto createCustomerWithAddress(
            String name,
            String email,
            String cpfCnpj,
            String phone,
            String mobilePhone,
            String address,
            String addressNumber,
            String complement,
            String province,
            String postalCode) {

        log.info("Creating customer with address: {}", email);

        CustomerSaveRequestDto request = CustomerSaveRequestDto.builder()
                .name(name)
                .email(email)
                .cpfCnpj(cpfCnpj)
                .phone(phone)
                .mobilePhone(mobilePhone)
                .address(address)
                .addressNumber(addressNumber)
                .complement(complement)
                .province(province)
                .postalCode(postalCode)
                .notificationDisabled(false)
                .build();

        return customerService.createNewCustomer(request);
    }

    /**
     * Busca um cliente pelo ID no ASAAS
     * 
     * @param customerId ID do cliente no ASAAS
     * @return dados do cliente
     */
    public CustomerGetResponseDto getCustomer(String customerId) {
        return customerService.retrieveASingleCustomer(customerId);
    }
}
