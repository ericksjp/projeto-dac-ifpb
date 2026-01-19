package com.ifpb.charger_proxy.application.webhook;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.ifpb.charger_proxy.domain.model.PaymentEvent;
import com.ifpb.charger_proxy.domain.repository.PaymentEventRepository;
import com.ifpb.charger_proxy.web.dto.AsaasWebhookPayload;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;

/**
 * Serviço responsável por processar webhooks recebidos do Asaas
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookProcessorService {

    private final PaymentEventRepository paymentEventRepository;
    private final ObjectMapper objectMapper;

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
            event.setPaymentRawPayload(objectMapper.writeValueAsString(payload.getPayment()));
            event.setProcessed(false);

            paymentEventRepository.save(event);
            log.info("Webhook salvo com sucesso - eventId={}", payload.getId());

        } catch (DataIntegrityViolationException e) {
            log.warn("Tentativa de duplicidade detectada pelo banco de dados para o evento {}", payload.getId());
        } catch (Exception e) {
            log.error("Erro ao salvar webhook - eventId={}", payload.getId(), e);
            throw e;
        }
    }
}
