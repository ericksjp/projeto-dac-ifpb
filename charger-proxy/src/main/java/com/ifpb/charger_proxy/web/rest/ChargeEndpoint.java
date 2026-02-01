package com.ifpb.charger_proxy.web.rest;

import com.asaas.apisdk.models.PaymentGetResponseDto;
import com.ifpb.charger_proxy.schemas.*;
import com.ifpb.charger_proxy.application.charger.LocalChargeService;
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
    
    private final ValidationUtil validationUtil;
    private final LocalChargeService chargeService;

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

    /**
     * Endpoint SOAP para cancelar uma cobrança
     */
    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "cancelChargeRequest")
    @ResponsePayload
    public CancelChargeResponse cancelCharge(@RequestPayload CancelChargeRequest request) {
        log.info("Received cancelCharge request for charge: {}", request.getChargeId());

        try {
            validationUtil.validateRequired(request.getChargeId(), "chargeId");
            
            boolean cancelled = chargeService.cancelCharge(request.getChargeId());

            CancelChargeResponse response = new CancelChargeResponse();
            response.setChargeId(request.getChargeId());
            response.setCancelled(cancelled);
            response.setStatus(cancelled ? "CANCELLED" : "FAILED");
            response.setMessage(cancelled ? "Cobrança cancelada com sucesso" : "Falha ao cancelar cobrança");

            log.info("Charge {} cancellation status: {}", request.getChargeId(), cancelled ? "SUCCESS" : "FAILED");
            return response;
            
        } catch (Exception e) {
            log.error("Error cancelling charge {}: {}", request.getChargeId(), e.getMessage(), e);
            
            CancelChargeResponse response = new CancelChargeResponse();
            response.setChargeId(request.getChargeId());
            response.setCancelled(false);
            response.setStatus("ERROR");
            response.setMessage("Erro ao cancelar cobrança: " + e.getMessage());
            
            return response;
        }
    }
}
