package com.ifpb.charger_proxy.application.webhook;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.ifpb.charger_proxy.domain.model.PaymentEvent;
import com.ifpb.charger_proxy.domain.repository.PaymentEventRepository;
import com.ifpb.charger_proxy.web.dto.AsaasWebhookPayload;

import com.ifpb.charger_proxy.infra.client.ChargerManagerClient;
import com.ifpb.charger_proxy.web.dto.PaymentEventDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.Map;

/**
 * Serviço responsável por processar webhooks recebidos do Asaas
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookProcessorService {

    private final PaymentEventRepository paymentEventRepository;
    private final ChargerManagerClient chargerManagerClient;
    /**
     * Processa webhook recebido
     */
    public void processWebhook(AsaasWebhookPayload payload) {
        if (payload == null || payload.getId() == null) {
            log.warn("Payload inválido recebido!");
            return;
        }

        saveEvent(payload);
    }

    /**
     * Persiste o evento de pagamento no banco de dados se ainda não existir
     */
    private void saveEvent(AsaasWebhookPayload payload) {
        try {
            // verifica se o evento ja foi processado fazendo uma consulta no banco de dados
            if (paymentEventRepository.existsByProviderEventId(payload.getId())) {
                log.info("Evento já processado anteriormente - eventId={}. Ignorando.", payload.getId());
                return;
            }

            PaymentEvent event = new PaymentEvent();
            event.setId(UUID.randomUUID());
            event.setProviderEventId(payload.getId());
            event.setEventType(payload.getEvent());
            event.setReceivedAt(LocalDateTime.now());
            event.setPayload(payload.getPayload());
            event.setProcessed(false);

            paymentEventRepository.save(event);
            log.info("Webhook salvo com sucesso - eventId={}", payload.getId());
            
            sendNotification(event, payload);

        } catch (DataIntegrityViolationException e) {
            log.warn("Tentativa de duplicidade detectada pelo banco de dados para o evento {}", payload.getId());
        } catch (Exception e) {
            log.error("Erro ao salvar webhook - eventId={}", payload.getId(), e);
            throw e;
        }
    }

    private void sendNotification(PaymentEvent event, AsaasWebhookPayload payload) {
        try {
            PaymentEventDto dto = new PaymentEventDto();
            dto.setId(event.getId());
            dto.setEventType(payload.getEvent());
            
            Map<String, Object> paymentData = payload.getPayload();
            if (paymentData != null) {
                dto.setChargeId((String) paymentData.get("id"));
                dto.setStatus((String) paymentData.get("status"));
                dto.setCustomerId((String) paymentData.get("customer"));
            }
            
            chargerManagerClient.send(dto, 
                () -> log.info("Notification sent successfully to manager for event {}", event.getId()),
                error -> log.error("Error sending notification to manager for event {}: {}", event.getId(), error.getMessage())
            );
            
        } catch (Exception e) {
            log.error("Error preparing notification for event {}: {}", event.getId(), e.getMessage());
        }
    }
}
