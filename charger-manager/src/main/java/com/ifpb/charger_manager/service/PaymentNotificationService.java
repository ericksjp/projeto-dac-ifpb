package com.ifpb.charger_manager.service;

import com.ifpb.charger_manager.api.dto.PaymentEventDto;
import com.ifpb.charger_manager.domain.enums.ChargeStatus;
import com.ifpb.charger_manager.domain.model.Charge;
import com.ifpb.charger_manager.domain.model.PaymentNotification;
import com.ifpb.charger_manager.domain.repository.PaymentNotificationRepository;
import com.ifpb.charger_manager.exception.ChargeNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
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

    public PaymentNotificationService(PaymentNotificationRepository notificationRepository, ChargeService chargeService,
            EmailService emailService) {
        this.notificationRepository = notificationRepository;
        this.chargeService = chargeService;
        this.emailService = emailService;
    }

    @Transactional(readOnly = false)
    public void processPaymentEvent(PaymentEventDto dto) {

        if (notificationProcessed(dto.getId())) {
            log.info("Notification with external event ID {} already processed. Skipping.", dto.getId());
            return;
        }

        UUID chargeId = null;

        try {
            Charge charge = chargeService.getChargeByExternalId(dto.getChargeId());
            chargeId = charge.getId();
            charge = chargeService.updateChargeStatus(chargeId, mapEventTypeToStatus(dto.getEventType()));
            emailService.sendChargeStatusUpdateEmail(charge);
        } catch (ChargeNotFoundException e) {
            log.info("Charge with external ID {} not found. Skipping status update.", dto.getChargeId());
        }

        saveNotification(dto, chargeId);
    }

    public PaymentNotification saveNotification(PaymentEventDto dto, UUID chargeId) {
        PaymentNotification notification = new PaymentNotification();
        notification.setId(UUID.randomUUID());
        notification.setChargeId(chargeId);
        notification.setChargeExternalId(dto.getChargeId());
        notification.setExternalEventId(dto.getId());
        notification.setEventType(dto.getEventType());
        notification.setReceivedAt(LocalDateTime.now());
        notification.setProcessed(true);
        notification.setProcessedAt(LocalDateTime.now());
        return notificationRepository.save(notification);
    }

    public boolean notificationProcessed(String externalEventId) {
        Optional<PaymentNotification> notification = notificationRepository.findByExternalEventId(externalEventId);

        if (notification.isEmpty()) {
            return false;
        }

        return notification.get().getProcessed();
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
