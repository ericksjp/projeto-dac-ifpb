package com.ifpb.charger_proxy.web.endpoints;

import com.asaas.apisdk.models.PaymentGetResponseDto;
import com.ifpb.charger_proxy.schemas.*;
import com.ifpb.charger_proxy.service.LocalChargeService;
import com.ifpb.charger_proxy.validation.ValidationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Endpoint SOAP para operações de cobranças
 */
@Slf4j
@Endpoint
@RequiredArgsConstructor
public class ChargeEndpoint {

    private static final String NAMESPACE_URI = "http://ifpb.com/charger-proxy";
    
    private final LocalChargeService chargeService;
    private final ValidationUtil validationUtil;

    /**
     * Endpoint SOAP para criar uma nova cobrança
     */
    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "createChargeRequest")
    @ResponsePayload
    public CreateChargeResponse createCharge(@RequestPayload CreateChargeRequest request) {
        log.info("Received createCharge request for customer: {}", request.getCustomerId());

        try {
            // Validações
            validationUtil.validateRequired(request.getCustomerId(), "customerId");
            validationUtil.validateBillingType(request.getBillingType());
            validationUtil.validateAmount(request.getValue());
            LocalDate dueDate = validationUtil.validateAndParseDate(request.getDueDate(), "dueDate");
            validationUtil.validateRequired(request.getDescription(), "description");
            
            PaymentGetResponseDto asaasResponse;
            
            // Verifica se é cobrança parcelada
            if (request.getInstallmentCount() != null && request.getInstallmentCount() > 1) {
                validationUtil.validateInstallmentCount(request.getInstallmentCount());
                asaasResponse = chargeService.createInstallmentCharge(
                    request.getCustomerId(),
                    request.getBillingType(),
                    request.getValue(),
                    request.getInstallmentCount(),
                    dueDate,
                    request.getDescription()
                );
            } else {
                asaasResponse = chargeService.createCharge(
                    request.getCustomerId(),
                    request.getBillingType(),
                    request.getValue(),
                    dueDate,
                    request.getDescription()
                );
            }

            // Monta response
            CreateChargeResponse response = new CreateChargeResponse();
            response.setChargeId(asaasResponse.getId());
            response.setCustomerId(asaasResponse.getCustomer());
            response.setBillingType(asaasResponse.getBillingType().toString());
            response.setValue(BigDecimal.valueOf(asaasResponse.getValue()));
            response.setDueDate(asaasResponse.getDueDate());
            response.setStatus(asaasResponse.getStatus().toString());
            response.setInvoiceUrl(asaasResponse.getInvoiceUrl());
            response.setBankSlipUrl(asaasResponse.getBankSlipUrl());
            response.setPixQrCode(asaasResponse.getPixQrCodeId());
            response.setDateCreated(asaasResponse.getDateCreated());

            log.info("Charge created successfully with ID: {}", asaasResponse.getId());
            return response;
            
        } catch (Exception e) {
            log.error("Error creating charge: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Endpoint SOAP para buscar uma cobrança pelo ID
     */
    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "getChargeRequest")
    @ResponsePayload
    public GetChargeResponse getCharge(@RequestPayload GetChargeRequest request) {
        log.info("Received getCharge request for ID: {}", request.getChargeId());

        try {
            validationUtil.validateRequired(request.getChargeId(), "chargeId");
            
            PaymentGetResponseDto asaasResponse = chargeService.getCharge(request.getChargeId());

            GetChargeResponse response = new GetChargeResponse();
            response.setChargeId(asaasResponse.getId());
            response.setCustomerId(asaasResponse.getCustomer());
            response.setBillingType(asaasResponse.getBillingType().toString());
            response.setValue(BigDecimal.valueOf(asaasResponse.getValue()));
            response.setDueDate(asaasResponse.getDueDate());
            response.setStatus(asaasResponse.getStatus().toString());
            response.setDescription(asaasResponse.getDescription());
            response.setInvoiceUrl(asaasResponse.getInvoiceUrl());
            response.setBankSlipUrl(asaasResponse.getBankSlipUrl());
            response.setPixQrCode(asaasResponse.getPixQrCodeId());
            response.setDateCreated(asaasResponse.getDateCreated());

            return response;
            
        } catch (Exception e) {
            log.error("Error getting charge: {}", e.getMessage(), e);
            throw e;
        }
    }
}
