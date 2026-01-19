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
     * Endpoint SOAP para registar um cliente em um provedor externo
     */
    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "registerCustomerRequest")
    @ResponsePayload
    public RegisterCustomerResponse registerCustomer(@RequestPayload RegisterCustomerRequest request) {
        log.info("Received registerCustomer request for id: {}", request.getId());

        try {
            // Validações de entrada
            validationUtil.validateRequired(request.getId(), "id");
            validationUtil.validateRequired(request.getName(), "name");
            validationUtil.validateEmail(request.getEmail());
            validationUtil.validateCpfCnpj(request.getCpfCnpj());

            CustomerGetResponseDto asaasResponse = customerService.register(
                    request.getId(),
                    request.getName(),
                    request.getEmail(),
                    request.getCpfCnpj());

            RegisterCustomerResponse response = new RegisterCustomerResponse();
            response.setCustomerExternalId(asaasResponse.getId());
            response.setCustomerExternalId(asaasResponse.getDateCreated());

            log.info("Customer registered successfully with ID: {}", asaasResponse.getId());
            return response;
        } catch (Exception e) {
            log.error("Error creating customer: {}", e.getMessage(), e);
            throw e;
        }
    }
}
