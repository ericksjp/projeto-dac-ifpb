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

            CreateChargeResponse response = new CreateChargeResponse();
            response.setChargeId(asaasResponse.getId());
            response.setInstallmentId(asaasResponse.getInstallment());
            response.setStatus(asaasResponse.getStatus().toString());
            response.setInvoiceUrl(asaasResponse.getInvoiceUrl());
            response.setBankSlipUrl(asaasResponse.getBankSlipUrl());
            response.setPixQrCode(asaasResponse.getPixQrCodeId());
            response.setCreatedAt(asaasResponse.getDateCreated());

            log.info("Charge created successfully with ID: {}", asaasResponse.getId());
            return response;
            
        } catch (Exception e) {
            log.error("Error creating charge: {}", e.getMessage(), e);
            throw e;
        }
    }
}
