package com.ifpb.charger_proxy.application.webhook;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.ifpb.charger_proxy.domain.model.PaymentEvent;
import com.ifpb.charger_proxy.domain.repository.PaymentEventRepository;
import com.ifpb.charger_proxy.infra.client.ChargerManagerClient;
import com.ifpb.charger_proxy.web.dto.PaymentEventDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Serviço responsável por consumir eventos de pagamento pendentes e enviá-los
 * ao Charger Manager
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentEventConsumer {

    private final PaymentEventRepository repository;
    private final ChargerManagerClient chargerManagerClient;

    @Value("${payment-event.consumer.batch-size}")
    private int batchSize;

    /**
     * Busca e processa eventos de pagamento pendentes a um intervalo fixo
     */
    @Scheduled(fixedDelayString = "${payment-event.consumer.fixed-delay-ms}")
    public void processPendingEvents() {
        var events = repository.findPendingEventsForProcessing(batchSize);

        if (events.isEmpty())
            return;

        log.info("Processando {} eventos pendentes", events.size());

        for (PaymentEvent event : events) {
            PaymentEventDto dto = new PaymentEventDto(
                    event.getId(),
                    event.getProviderEventId(),
                    event.getEventType(),
                    event.getPaymentRawPayload(),
                    event.getReceivedAt());

            chargerManagerClient.send(dto, () -> markEventAsProcessed(event), error -> LogError(error, event));
        }
    }

    private void markEventAsProcessed(PaymentEvent event) {
        try {
            event.setProcessed(true);
            event.setProcessedAt(LocalDateTime.now());
            repository.save(event);
            log.info("Evento {} processado com sucesso", event.getProviderEventId());
        } catch (OptimisticLockingFailureException ex) {
            log.warn("Evento {} já processado por outro worker", event.getProviderEventId());
        } catch (Exception e) {
            log.error("Erro ao processar evento {}", event.getProviderEventId(), e);
        }
    }

    private void LogError(Throwable error, PaymentEvent event) {
        log.error("Não foi possível enviar o evento {}: {}", event.getProviderEventId(), error.getMessage());
    }
}
