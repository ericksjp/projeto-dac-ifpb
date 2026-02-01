package com.ifpb.charger_manager.api.controller;

import com.ifpb.charger_manager.api.dto.PaymentEventDto;
import com.ifpb.charger_manager.service.PaymentNotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller REST para receber notificações de eventos de pagamento
 */
@RestController
@RequestMapping("/api/v1/notifications")
public class PaymentNotificationController {
    
    private static final Logger log = LoggerFactory.getLogger(PaymentNotificationController.class);
    
    private final PaymentNotificationService notificationService;

    public PaymentNotificationController(PaymentNotificationService notificationService) {
        this.notificationService = notificationService;
    }
    
    /**
     * Recebe notificação de evento de pagamento do charge-proxy
     */
    @PostMapping("/payment-events")
    public ResponseEntity<Void> receivePaymentEvent(@RequestBody PaymentEventDto dto) {
        log.info("POST /api/v1/notifications/payment-events - Received event: type={}, chargeId={}", 
                 dto.getEventType(), dto.getChargeId());
        
        try {
            notificationService.processPaymentEvent(dto);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error processing payment event: {}", e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }
}
