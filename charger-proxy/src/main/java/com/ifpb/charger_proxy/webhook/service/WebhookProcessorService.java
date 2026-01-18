package com.ifpb.charger_proxy.webhook.service;

import com.ifpb.charger_proxy.webhook.dto.AsaasWebhookPayload;
import com.ifpb.charger_proxy.webhook.dto.PaymentWebhookData;
import com.ifpb.charger_proxy.webhook.dto.WebhookEventType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Serviço para processar webhooks recebidos do ASAAS
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookProcessorService {

    /**
     * Processa o payload do webhook recebido do ASAAS
     * 
     * @param payload dados do webhook
     */
    public void processWebhook(AsaasWebhookPayload payload) {
        if (payload == null) {
            log.warn("Received null webhook payload");
            return;
        }

        WebhookEventType eventType = WebhookEventType.fromString(payload.getEvent());
        
        log.info("Processing webhook - Event: {} ({})", 
                 eventType, eventType.getDescription());

        try {
            processPaymentEvent(eventType, payload.getPayment());
        } catch (Exception e) {
            log.error("Error processing webhook for event {}: {}", 
                      eventType, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * processa eventos relacionados a pagamentos
     */
    private void processPaymentEvent(WebhookEventType eventType, PaymentWebhookData payment) {
        if (payment == null) {
            log.warn("Received payment event without payment data: {}", eventType);
            return;
        }

        log.info("Payment Event - Type: {}, Payment ID: {}, Status: {}, Value: {}", 
                 eventType, payment.getId(), payment.getStatus(), payment.getValue());

        switch (eventType) {
            case PAYMENT_CREATED:
                handlePaymentCreated(payment);
                break;
                
            case PAYMENT_CONFIRMED:
                handlePaymentConfirmed(payment);
                break;
                
            case PAYMENT_RECEIVED:
                handlePaymentReceived(payment);
                break;
                
            case PAYMENT_OVERDUE:
                handlePaymentOverdue(payment);
                break;
                
            case PAYMENT_REFUNDED:
                handlePaymentRefunded(payment);
                break;
                
            case PAYMENT_DELETED:
                handlePaymentDeleted(payment);
                break;
                
            default:
                log.debug("Payment event {} logged but not specifically handled", eventType);
        }
    }

    // ======= handlers de pagamento =======

    private void handlePaymentCreated(PaymentWebhookData payment) {
        log.info("Nova cobrança criada - ID: {}, Valor: R$ {}, Vencimento: {}", 
                 payment.getId(), payment.getValue(), payment.getDueDate());
        
        // TODO: Implementar lógica específica
    }

    private void handlePaymentConfirmed(PaymentWebhookData payment) {
        log.info("Pagamento CONFIRMADO - ID: {}, Valor: R$ {}, Data: {}", 
                 payment.getId(), payment.getValue(), payment.getConfirmedDate());
        
        // TODO: Implementar lógica específica
    }

    private void handlePaymentReceived(PaymentWebhookData payment) {
        log.info("Pagamento RECEBIDO - ID: {}, Valor Líquido: R$ {}", 
                 payment.getId(), payment.getNetValue());
        
        // TODO: Implementar lógica específica
    }

    private void handlePaymentOverdue(PaymentWebhookData payment) {
        log.warn("Pagamento VENCIDO - ID: {}, Valor: R$ {}, Vencimento: {}", 
                 payment.getId(), payment.getValue(), payment.getDueDate());
        
        // TODO: Implementar lógica específica
    }

    private void handlePaymentRefunded(PaymentWebhookData payment) {
        log.info("Pagamento ESTORNADO - ID: {}, Valor: R$ {}", 
                 payment.getId(), payment.getValue());
        
        // TODO: Implementar lógica específica
    }

    private void handlePaymentDeleted(PaymentWebhookData payment) {
        log.info("Cobrança DELETADA - ID: {}", payment.getId());
        
        // TODO: Implementar lógica específica
    }
}
