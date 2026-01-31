package com.ifpb.charger_manager.api.controller;

import com.ifpb.charger_manager.api.dto.ChargeCreateDto;
import com.ifpb.charger_manager.api.dto.ChargeResponseDto;
import com.ifpb.charger_manager.domain.model.Charge;
import com.ifpb.charger_manager.service.ChargeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Controller REST para gerenciamento de cobranças
 */
@RestController
@RequestMapping("/api/v1/charges")
public class ChargeController {
    
    private static final Logger log = LoggerFactory.getLogger(ChargeController.class);
    
    private final ChargeService chargeService;

    public ChargeController(ChargeService chargeService) {
        this.chargeService = chargeService;
    }
    
    /**
     * Cria uma nova cobrança
     */
    @PostMapping
    public ResponseEntity<ChargeResponseDto> createCharge(@RequestBody ChargeCreateDto dto) {
        log.info("POST /api/v1/charges - Creating charge for customer: {}", dto.getCustomerId());
        
        Charge charge = chargeService.createCharge(
            dto.getCustomerId(),
            dto.getBillingType(),
            dto.getValue(),
            dto.getDueDate(),
            dto.getDescription(),
            dto.getInstallmentCount()
        );
        
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(charge));
    }
    
    /**
     * Busca uma cobrança por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ChargeResponseDto> getCharge(@PathVariable UUID id) {
        log.info("GET /api/v1/charges/{} - Getting charge", id);
        
        Charge charge = chargeService.getChargeById(id);
        return ResponseEntity.ok(toDto(charge));
    }
    
    /**
     * Lista todas as cobranças
     */
    @GetMapping
    public ResponseEntity<List<ChargeResponseDto>> getAllCharges() {
        log.info("GET /api/v1/charges - Listing all charges");
        
        List<ChargeResponseDto> charges = chargeService.getAllCharges()
            .stream()
            .map(this::toDto)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(charges);
    }
    
    /**
     * Lista cobranças de um cliente
     */
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<ChargeResponseDto>> getChargesByCustomer(@PathVariable UUID customerId) {
        log.info("GET /api/v1/charges/customer/{} - Listing charges for customer", customerId);
        
        List<ChargeResponseDto> charges = chargeService.getChargesByCustomer(customerId)
            .stream()
            .map(this::toDto)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(charges);
    }
    
    /**
     * Cancela uma cobrança
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ChargeResponseDto> cancelCharge(@PathVariable UUID id) {
        log.info("DELETE /api/v1/charges/{} - Cancelling charge", id);
        
        Charge charge = chargeService.cancelCharge(id);
        return ResponseEntity.ok(toDto(charge));
    }
    
    /**
     * Converte Charge para ChargeResponseDto
     */
    private ChargeResponseDto toDto(Charge charge) {
        return new ChargeResponseDto(
            charge.getId(),
            charge.getCustomerId(),
            charge.getExternalId(),
            charge.getInstallmentId(),
            charge.getBillingType(),
            charge.getValue(),
            charge.getDueDate(),
            charge.getDescription(),
            charge.getStatus(),
            charge.getInvoiceUrl(),
            charge.getBankSlipUrl(),
            charge.getPixQrCode(),
            charge.getInstallmentCount(),
            charge.getInstallmentNumber(),
            charge.getCreatedAt(),
            charge.getUpdatedAt(),
            charge.getCancelledAt()
        );
    }
}
