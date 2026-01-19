package com.ifpb.charger_proxy.web.endpoints;

import com.asaas.apisdk.models.CustomerGetResponseDto;
import com.ifpb.charger_proxy.schemas.*;
import com.ifpb.charger_proxy.service.LocalCustomerService;
import com.ifpb.charger_proxy.validation.ValidationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

/**
 * Endpoint SOAP para operações de clientes
 */
@Slf4j
@Endpoint
@RequiredArgsConstructor
public class CustomerEndpoint {

    private static final String NAMESPACE_URI = "http://ifpb.com/charger-proxy";

    private final LocalCustomerService customerService;
    private final ValidationUtil validationUtil;

    /**
     * Endpoint SOAP para criar um novo cliente
     */
    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "createCustomerRequest")
    @ResponsePayload
    public CreateCustomerResponse createCustomer(@RequestPayload CreateCustomerRequest request) {
        log.info("Received createCustomer request for email: {}", request.getEmail());

        try {
            // Validações de entrada
            validationUtil.validateRequired(request.getName(), "name");
            validationUtil.validateEmail(request.getEmail());
            validationUtil.validateCpfCnpj(request.getCpfCnpj());

            CustomerGetResponseDto asaasResponse = customerService.createCustomer(
                    request.getName(),
                    request.getEmail(),
                    request.getCpfCnpj());

            CreateCustomerResponse response = new CreateCustomerResponse();
            response.setCustomerId(asaasResponse.getId());
            response.setName(asaasResponse.getName());
            response.setEmail(asaasResponse.getEmail());
            response.setCpfCnpj(asaasResponse.getCpfCnpj());
            response.setDateCreated(asaasResponse.getDateCreated());

            log.info("Customer created successfully with ID: {}", asaasResponse.getId());
            return response;
            
        } catch (Exception e) {
            log.error("Error creating customer: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Endpoint SOAP para buscar um cliente pelo ID
     */
    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "getCustomerRequest")
    @ResponsePayload
    public GetCustomerResponse getCustomer(@RequestPayload GetCustomerRequest request) {
        log.info("Received getCustomer request for ID: {}", request.getCustomerId());

        try {
            validationUtil.validateRequired(request.getCustomerId(), "customerId");
            
            CustomerGetResponseDto asaasResponse = customerService.getCustomer(request.getCustomerId());

            GetCustomerResponse response = new GetCustomerResponse();
            response.setCustomerId(asaasResponse.getId());
            response.setName(asaasResponse.getName());
            response.setEmail(asaasResponse.getEmail());
            response.setCpfCnpj(asaasResponse.getCpfCnpj());
            response.setDateCreated(asaasResponse.getDateCreated());

            return response;
            
        } catch (Exception e) {
            log.error("Error getting customer: {}", e.getMessage(), e);
            throw e;
        }
    }
}
