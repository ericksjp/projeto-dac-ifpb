package com.ifpb.charger_proxy.service;

import com.asaas.apisdk.models.CustomerGetResponseDto;
import com.asaas.apisdk.models.CustomerSaveRequestDto;
import com.asaas.apisdk.services.CustomerService;
import com.ifpb.charger_proxy.exception.AsaasClientException;
import com.ifpb.charger_proxy.exception.CustomerNotFoundException;
import com.ifpb.charger_proxy.exception.InvalidRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Serviço para gerenciar clientes no ASAAS
 */
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
     * @return resposta do ASAAS com ID do cliente criado
     * @throws InvalidRequestException se os dados forem inválidos
     * @throws AsaasClientException    se houver erro na comunicação com ASAAS
     */
    public CustomerGetResponseDto createCustomer(
            String name,
            String email,
            String cpfCnpj) {

        log.info("Creating customer: {} - {}", email, cpfCnpj);

        try {
            CustomerSaveRequestDto customerSaveRequestDto = CustomerSaveRequestDto.builder()
                    .name(name)
                    .email(email)
                    .cpfCnpj(cpfCnpj)
                    .notificationDisabled(false)
                    .build();

            CustomerGetResponseDto response = customerService.createNewCustomer(customerSaveRequestDto);

            if (response == null) {
                throw new AsaasClientException("ASAAS retornou resposta nula ao criar cliente");
            }

            log.info("Customer created successfully with ID: {}", response.getId());
            return response;

        } catch (IllegalArgumentException e) {
            log.error("Invalid data for customer creation: {}", e.getMessage(), e);
            throw new InvalidRequestException("Dados inválidos para criação de cliente: " + e.getMessage());
        } catch (AsaasClientException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error creating customer {}: {}", email, e.getMessage(), e);
            throw new AsaasClientException("Erro ao criar cliente no ASAAS: " + e.getMessage(), e);
        }
    }

    /**
     * Busca um cliente pelo ID no ASAAS
     * 
     * @param customerId ID do cliente no ASAAS
     * @return dados do cliente
     * @throws CustomerNotFoundException se o cliente não for encontrado
     * @throws AsaasClientException      se houver erro na comunicação com ASAAS
     */
    public CustomerGetResponseDto getCustomer(String customerId) {
        log.info("Getting customer: {}", customerId);

        try {
            CustomerGetResponseDto response = customerService.retrieveASingleCustomer(customerId);

            if (response == null) {
                throw new CustomerNotFoundException(customerId);
            }

            log.debug("Customer retrieved successfully: {}", customerId);
            return response;

        } catch (CustomerNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error retrieving customer {}: {}", customerId, e.getMessage(), e);
            throw new AsaasClientException("Erro ao buscar cliente no ASAAS: " + e.getMessage(), e);
        }
    }
}
