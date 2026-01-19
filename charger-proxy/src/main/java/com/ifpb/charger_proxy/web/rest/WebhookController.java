package com.ifpb.charger_proxy.web.rest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ifpb.charger_proxy.application.webhook.WebhookProcessorService;
import com.ifpb.charger_proxy.web.dto.AsaasWebhookPayload;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST Controller para receber webhooks do ASAAS
 */
@Slf4j
@RestController
@RequestMapping("/api/webhook")
@RequiredArgsConstructor
public class WebhookController {

    private final WebhookProcessorService webhookProcessorService;

    /**
     * Endpoint para receber webhooks do ASAAS
     * 
     * @param payload dados do webhook enviados pelo ASAAS
     * @return 200 OK se processado com sucesso, 500 em caso de erro
     */
    @PostMapping("/asaas")
    public ResponseEntity<Void> receiveWebhook(@RequestBody AsaasWebhookPayload payload) {
        log.info("Webhook received from ASAAS - EventId: {}", payload.getId());

        try {
            webhookProcessorService.processWebhook(payload);

            log.info("Webhook processed successfully");
            return ResponseEntity.ok().build();

        } catch (Exception e) {
            log.error("Error processing webhook: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Endpoint de teste para verificar se o webhook está funcionando
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("healthy");
    }
}
