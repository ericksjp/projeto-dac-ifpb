package com.ifpb.charger_proxy.web.endpoints;

import com.asaas.apisdk.models.CustomerGetResponseDto;
import com.ifpb.charger_proxy.schemas.*;
import com.ifpb.charger_proxy.service.LocalCustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

@Slf4j
@Endpoint
@RequiredArgsConstructor
public class CustomerEndpoint {

    private static final String NAMESPACE_URI = "http://ifpb.com/charger-proxy";

    private final LocalCustomerService customerService;

    /**
     * Endpoint SOAP para criar um novo cliente
     */
    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "createCustomerRequest")
    @ResponsePayload
    public CreateCustomerResponse createCustomer(@RequestPayload CreateCustomerRequest request) {
        log.info("Received createCustomer request for email: {}", request.getEmail());

        CustomerGetResponseDto asaasResponse = customerService.createCustomer(
                request.getName(),
                request.getEmail(),
                request.getCpfCnpj(),
                request.getPhone(),
                request.getMobilePhone());

        CreateCustomerResponse response = new CreateCustomerResponse();
        response.setCustomerId(asaasResponse.getId());
        response.setName(asaasResponse.getName());
        response.setEmail(asaasResponse.getEmail());
        response.setCpfCnpj(asaasResponse.getCpfCnpj());
        response.setDateCreated(asaasResponse.getDateCreated());

        log.info("Customer created successfully with ID: {}", asaasResponse.getId());
        return response;
    }

    /**
     * Endpoint SOAP para buscar um cliente pelo ID
     */
    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "getCustomerRequest")
    @ResponsePayload
    public GetCustomerResponse getCustomer(@RequestPayload GetCustomerRequest request) {
        log.info("Received getCustomer request for ID: {}", request.getCustomerId());

        CustomerGetResponseDto asaasResponse = customerService.getCustomer(request.getCustomerId());

        GetCustomerResponse response = new GetCustomerResponse();
        response.setCustomerId(asaasResponse.getId());
        response.setName(asaasResponse.getName());
        response.setEmail(asaasResponse.getEmail());
        response.setCpfCnpj(asaasResponse.getCpfCnpj());
        response.setPhone(asaasResponse.getPhone());
        response.setMobilePhone(asaasResponse.getMobilePhone());
        response.setDateCreated(asaasResponse.getDateCreated());

        return response;
    }
}
