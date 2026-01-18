package com.ifpb.charger_proxy.service;

import com.asaas.apisdk.models.PaymentGetResponseDto;
import com.asaas.apisdk.models.PaymentSaveRequestDto;
import com.asaas.apisdk.models.PaymentSaveRequestBillingType;
import com.asaas.apisdk.services.PaymentService;
import com.ifpb.charger_proxy.exception.AsaasClientException;
import com.ifpb.charger_proxy.exception.ChargeCreationException;
import com.ifpb.charger_proxy.exception.ChargeNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Serviço para gerenciar cobranças (payments) no ASAAS
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LocalChargeService {

    private final PaymentService paymentService;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Cria uma nova cobrança no ASAAS
     * 
     * @param customerId ID do cliente no ASAAS
     * @param billingType tipo de cobrança (PIX, BOLETO, CREDIT_CARD, etc)
     * @param value valor da cobrança
     * @param dueDate data de vencimento
     * @param description descrição da cobrança
     * @return resposta do ASAAS com dados da cobrança criada
     */
    public PaymentGetResponseDto createCharge(
            String customerId,
            String billingType,
            BigDecimal value,
            LocalDate dueDate,
            String description) {
        
        log.info("Creating charge for customer {}: {} - R$ {}", customerId, billingType, value);

        try {
            PaymentSaveRequestDto request = PaymentSaveRequestDto.builder()
                    .customer(customerId)
                    .billingType(convertBillingType(billingType))
                    .value(value.doubleValue())
                    .dueDate(dueDate.format(DATE_FORMATTER))
                    .description(description)
                    .build();

            PaymentGetResponseDto response = paymentService.createNewPayment(request);
            log.info("Charge created successfully with ID: {}", response.getId());
            return response;
            
        } catch (Exception e) {
            log.error("Error creating charge for customer {}: {}", customerId, e.getMessage(), e);
            throw new ChargeCreationException("Erro ao criar cobrança: " + e.getMessage(), e);
        }
    }

    /**
     * Cria uma cobrança parcelada no ASAAS
     * 
     * @param customerId ID do cliente no ASAAS
     * @param billingType tipo de cobrança (geralmente CREDIT_CARD para parcelamento)
     * @param totalValue valor total
     * @param installmentCount número de parcelas
     * @param dueDate data de vencimento da primeira parcela
     * @param description descrição da cobrança
     * @return resposta do ASAAS com dados da cobrança criada
     */
    public PaymentGetResponseDto createInstallmentCharge(
            String customerId,
            String billingType,
            BigDecimal totalValue,
            Integer installmentCount,
            LocalDate dueDate,
            String description) {
        
        log.info("Creating installment charge for customer {}: {} - R$ {} in {} installments", 
                 customerId, billingType, totalValue, installmentCount);

        try {
            PaymentSaveRequestDto request = PaymentSaveRequestDto.builder()
                    .customer(customerId)
                    .billingType(convertBillingType(billingType))
                    .value(totalValue.doubleValue())
                    .dueDate(dueDate.format(DATE_FORMATTER))
                    .description(description)
                    .installmentCount(installmentCount.longValue())
                    .build();

            PaymentGetResponseDto response = paymentService.createNewPayment(request);
            log.info("Installment charge created successfully with ID: {}", response.getId());
            return response;
            
        } catch (Exception e) {
            log.error("Error creating installment charge for customer {}: {}", customerId, e.getMessage(), e);
            throw new ChargeCreationException("Erro ao criar cobrança parcelada: " + e.getMessage(), e);
        }
    }

    /**
     * Busca uma cobrança pelo ID no ASAAS
     * 
     * @param chargeId ID da cobrança no ASAAS
     * @return dados da cobrança
     */
    public PaymentGetResponseDto getCharge(String chargeId) {
        log.info("Getting charge: {}", chargeId);
        
        try {
            PaymentGetResponseDto response = paymentService.retrieveASinglePayment(chargeId);
            
            if (response == null) {
                throw new ChargeNotFoundException(chargeId);
            }
            
            return response;
            
        } catch (ChargeNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error getting charge {}: {}", chargeId, e.getMessage(), e);
            throw new AsaasClientException("Erro ao buscar cobrança: " + e.getMessage(), e);
        }
    }

    /**
     * Converte string de billing type para o enum do SDK
     */
    private PaymentSaveRequestBillingType convertBillingType(String billingType) {
        return switch (billingType.toUpperCase()) {
            case "PIX" -> PaymentSaveRequestBillingType.PIX;
            case "BOLETO" -> PaymentSaveRequestBillingType.BOLETO;
            case "CREDIT_CARD" -> PaymentSaveRequestBillingType.CREDIT_CARD;
            case "UNDEFINED" -> PaymentSaveRequestBillingType.UNDEFINED;
            default -> throw new IllegalArgumentException("Billing type inválido: " + billingType);
        };
    }
}
