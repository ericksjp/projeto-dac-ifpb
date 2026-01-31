package com.ifpb.charger_manager.service;

import com.ifpb.charger_manager.domain.enums.ChargeStatus;
import com.ifpb.charger_manager.domain.model.PaymentNotification;
import com.ifpb.charger_manager.domain.repository.PaymentNotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Serviço para processar notificações de eventos de pagamento
 */
@Service
public class PaymentNotificationService {
    
    private static final Logger log = LoggerFactory.getLogger(PaymentNotificationService.class);
    
    private final PaymentNotificationRepository notificationRepository;
    private final ChargeService chargeService;
    private final EmailService emailService;

    public PaymentNotificationService(PaymentNotificationRepository notificationRepository, ChargeService chargeService, EmailService emailService) {
        this.notificationRepository = notificationRepository;
        this.chargeService = chargeService;
        this.emailService = emailService;
    }
    
    /**
     * Processa uma notificação de evento de pagamento
     */
    @Transactional
    public PaymentNotification processPaymentEvent(
            String eventType,
            String externalEventId,
            String chargeExternalId,
            Map<String, Object> payload) {
        
        log.info("Processing payment event: type={}, chargeId={}", eventType, chargeExternalId);
        
        // Verifica se já processou este evento
        if (externalEventId != null) {
            var existing = notificationRepository.findByExternalEventId(externalEventId);
            if (existing.isPresent()) {
                log.warn("Event already processed: {}", externalEventId);
                return existing.get();
            }
        }
        
        // Busca a cobrança pelo externalId
        UUID chargeId = null;
        try {
            var charge = chargeService.updateChargeStatusByExternalId(
                chargeExternalId,
                mapEventTypeToStatus(eventType)
            );
            chargeId = charge.getId();
            
            // Envia notificação por e-mail após atualizar o status
            emailService.sendChargeStatusUpdateEmail(charge);
        } catch (Exception e) {
            log.error("Error updating charge status: {}", e.getMessage(), e);
        }
        
        // Cria a notificação
        PaymentNotification notification = new PaymentNotification();
        notification.setId(UUID.randomUUID());
        notification.setChargeId(chargeId);
        notification.setEventType(eventType);
        notification.setExternalEventId(externalEventId);
        notification.setPayload(payload);
        notification.setReceivedAt(LocalDateTime.now());
        notification.setProcessed(true);
        notification.setProcessedAt(LocalDateTime.now());
        
        notification = notificationRepository.save(notification);
        
        log.info("Payment event processed successfully: id={}", notification.getId());
        return notification;
    }
    
    /**
     * Lista notificações de uma cobrança
     */
    public List<PaymentNotification> getNotificationsByCharge(UUID chargeId) {
        return notificationRepository.findByChargeId(chargeId);
    }
    
    /**
     * Mapeia tipo de evento para status da cobrança
     */
    private ChargeStatus mapEventTypeToStatus(String eventType) {
        return switch (eventType.toUpperCase()) {
            case "PAYMENT_CREATED" -> ChargeStatus.PENDING;
            case "PAYMENT_CONFIRMED" -> ChargeStatus.CONFIRMED;
            case "PAYMENT_RECEIVED" -> ChargeStatus.RECEIVED;
            case "PAYMENT_OVERDUE" -> ChargeStatus.OVERDUE;
            case "PAYMENT_REFUNDED" -> ChargeStatus.REFUNDED;
            case "PAYMENT_DELETED", "PAYMENT_CANCELLED" -> ChargeStatus.CANCELLED;
            default -> {
                log.warn("Unknown event type: {}, defaulting to PENDING", eventType);
                yield ChargeStatus.PENDING;
            }
        };
    }
}
