package com.ifpb.charger_proxy.webhook.controller;

import com.ifpb.charger_proxy.webhook.dto.AsaasWebhookPayload;
import com.ifpb.charger_proxy.webhook.service.WebhookProcessorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<String> receiveWebhook(@RequestBody AsaasWebhookPayload payload) {
        log.info("Webhook received from ASAAS - Event: {}", payload.getEvent());

        try {
            webhookProcessorService.processWebhook(payload);

            log.info("Webhook processed successfully");
            return ResponseEntity.ok("Webhook received and processed");

        } catch (Exception e) {
            log.error("Error processing webhook: {}", e.getMessage(), e);

            // Retornamos 200 mesmo em caso de erro para evitar reenvios do ASAAS
            // Se você quiser que o ASAAS reenvie em caso de erro, retorne 500
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing webhook: " + e.getMessage());
        }
    }

    /**
     * Endpoint de teste para verificar se o webhook está funcionando
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Webhook endpoint is healthy and ready to receive notifications");
    }
}
