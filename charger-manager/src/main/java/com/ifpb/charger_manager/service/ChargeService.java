package com.ifpb.charger_manager.service;

import com.ifpb.charger_manager.domain.enums.BillingType;
import com.ifpb.charger_manager.domain.enums.ChargeStatus;
import com.ifpb.charger_manager.domain.model.Charge;
import com.ifpb.charger_manager.domain.model.Customer;
import com.ifpb.charger_manager.domain.repository.ChargeRepository;
import com.ifpb.charger_manager.exception.ChargeCreationException;
import com.ifpb.charger_manager.exception.ChargeNotFoundException;
import com.ifpb.charger_manager.exception.InvalidRequestException;
import com.ifpb.charger_manager.infra.soap.ChargeProxyClient;
import com.ifpb.charger_manager.ws.charge.v1.CreateChargeResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * Serviço para gerenciamento de cobranças
 */
@Service
public class ChargeService {
    
    private static final Logger log = LoggerFactory.getLogger(ChargeService.class);
    
    private final ChargeRepository chargeRepository;
    private final CustomerService customerService;
    private final ChargeProxyClient chargeProxyClient;
    private final EmailService emailService;

    public ChargeService(ChargeRepository chargeRepository, CustomerService customerService, ChargeProxyClient chargeProxyClient, EmailService emailService) {
        this.chargeRepository = chargeRepository;
        this.customerService = customerService;
        this.chargeProxyClient = chargeProxyClient;
        this.emailService = emailService;
    }
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    /**
     * Cria uma nova cobrança
     */
    @Transactional
    public Charge createCharge(
            UUID customerId,
            BillingType billingType,
            BigDecimal value,
            LocalDate dueDate,
            String description,
            Integer installmentCount) {
        
        log.info("Creating charge: customerId={}, type={}, value={}, dueDate={}", 
                 customerId, billingType, value, dueDate);
        
        // Validações
        validateChargeData(customerId, billingType, value, dueDate, description);
        
        // Busca o cliente para pegar o externalId
        Customer customer = customerService.getCustomerById(customerId);
        
        if (customer.getExternalId() == null) {
            throw new ChargeCreationException("Cliente não possui ID externo no Asaas");
        }
        
        try {
            // Cria a cobrança via charge-proxy
            CreateChargeResponse response = chargeProxyClient.createCharge(
                customer.getExternalId(),
                billingType.name(),
                value,
                dueDate.format(DATE_FORMATTER),
                description,
                installmentCount
            );
            
            // Cria a cobrança local
            Charge charge = new Charge();
            charge.setId(UUID.randomUUID());
            charge.setCustomerId(customerId);
            charge.setExternalId(response.getChargeId());
            charge.setInstallmentId(response.getInstallmentId());
            charge.setBillingType(billingType);
            charge.setValue(value);
            charge.setDueDate(dueDate);
            charge.setDescription(description);
            charge.setStatus(ChargeStatus.valueOf(response.getStatus()));
            charge.setInvoiceUrl(response.getInvoiceUrl());
            charge.setBankSlipUrl(response.getBankSlipUrl());
            charge.setPixQrCode(response.getPixQrCode());
            charge.setInstallmentCount(installmentCount);
            charge.setCreatedAt(LocalDateTime.now());
            charge.setUpdatedAt(LocalDateTime.now());
            
            charge = chargeRepository.save(charge);
            charge.setNew(false);
            
            // Envia notificação por e-mail
            emailService.sendChargeStatusUpdateEmail(charge);
            
            log.info("Charge created successfully: id={}, externalId={}", 
                     charge.getId(), charge.getExternalId());
            
            return charge;
            
        } catch (Exception e) {
            log.error("Error creating charge: {}", e.getMessage(), e);
            throw new ChargeCreationException("Erro ao criar cobrança: " + e.getMessage(), e);
        }
    }
    
    /**
     * Busca uma cobrança por ID
     */
    public Charge getChargeById(UUID id) {
        return chargeRepository.findById(id)
            .orElseThrow(() -> new ChargeNotFoundException("Cobrança não encontrada: " + id));
    }
    
    /**
     * Lista cobranças de um cliente
     */
    public List<Charge> getChargesByCustomer(UUID customerId) {
        // Valida se o cliente existe
        customerService.getCustomerById(customerId);
        return chargeRepository.findByCustomerId(customerId);
    }
    
    /**
     * Lista todas as cobranças
     */
    public List<Charge> getAllCharges() {
        return chargeRepository.findAllOrderByCreatedAtDesc();
    }
    
    /**
     * Lista cobranças por status
     */
    public List<Charge> getChargesByStatus(ChargeStatus status) {
        return chargeRepository.findByStatus(status.name());
    }
    
    /**
     * Cancela uma cobrança
     */
    @Transactional
    public Charge cancelCharge(UUID id) {
        log.info("Cancelling charge: id={}", id);
        
        Charge charge = getChargeById(id);
        
        if (charge.getStatus() == ChargeStatus.CANCELLED) {
            throw new InvalidRequestException("Cobrança já está cancelada");
        }
        
        if (charge.getStatus() == ChargeStatus.RECEIVED) {
            throw new InvalidRequestException("Não é possível cancelar cobrança já recebida");
        }
        
        try {
            // Cancela no Asaas via proxy
            chargeProxyClient.cancelCharge(charge.getExternalId());
            
            charge.setStatus(ChargeStatus.CANCELLED);
            charge.setCancelledAt(LocalDateTime.now());
            charge.setUpdatedAt(LocalDateTime.now());
            
            charge = chargeRepository.save(charge);
            charge.setNew(false);
            
            // Envia notificação por e-mail
            emailService.sendChargeStatusUpdateEmail(charge);
            
            log.info("Charge cancelled successfully: id={}", id);
            return charge;
            
        } catch (Exception e) {
            log.error("Error cancelling charge {}: {}", id, e.getMessage());
            throw new InvalidRequestException("Erro ao cancelar cobrança no provedor: " + e.getMessage());
        }
    }
    
    /**
     * Atualiza o status de uma cobrança (usado por notificações)
     */
    @Transactional
    public Charge updateChargeStatus(UUID id, ChargeStatus newStatus) {
        log.info("Updating charge status: id={}, newStatus={}", id, newStatus);
        
        Charge charge = getChargeById(id);
        charge.setStatus(newStatus);
        charge.setUpdatedAt(LocalDateTime.now());
        
        charge = chargeRepository.save(charge);
        charge.setNew(false);
        
        log.info("Charge status updated successfully: id={}, status={}", id, newStatus);
        return charge;
    }
    
    /**
     * Atualiza o status de uma cobrança pelo externalId
     */
    @Transactional
    public Charge updateChargeStatusByExternalId(String externalId, ChargeStatus newStatus) {
        log.info("Updating charge status by externalId: externalId={}, newStatus={}", externalId, newStatus);
        
        Charge charge = chargeRepository.findByExternalId(externalId)
            .orElseThrow(() -> new ChargeNotFoundException("Cobrança não encontrada com externalId: " + externalId));
        
        charge.setStatus(newStatus);
        charge.setUpdatedAt(LocalDateTime.now());
        
        charge = chargeRepository.save(charge);
        charge.setNew(false);
        
        log.info("Charge status updated successfully by externalId: externalId={}, status={}", externalId, newStatus);
        return charge;
    }

    public Charge getChargeByExternalId(String externalId) {
        return chargeRepository.findByExternalId(externalId)
            .orElseThrow(() -> new ChargeNotFoundException("Cobrança não encontrada com externalId: " + externalId));
    }

    /**
     * Valida os dados da cobrança
     */
    private void validateChargeData(
            UUID customerId,
            BillingType billingType,
            BigDecimal value,
            LocalDate dueDate,
            String description) {
        
        if (customerId == null) {
            throw new InvalidRequestException("ID do cliente é obrigatório");
        }
        
        if (billingType == null) {
            throw new InvalidRequestException("Tipo de cobrança é obrigatório");
        }
        
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidRequestException("Valor deve ser maior que zero");
        }
        
        if (dueDate == null) {
            throw new InvalidRequestException("Data de vencimento é obrigatória");
        }
        
        if (dueDate.isBefore(LocalDate.now())) {
            throw new InvalidRequestException("Data de vencimento não pode ser no passado");
        }
        
        if (description == null || description.isBlank()) {
            throw new InvalidRequestException("Descrição é obrigatória");
        }
    }
}
