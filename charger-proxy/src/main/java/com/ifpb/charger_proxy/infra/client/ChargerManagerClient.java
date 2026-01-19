package com.ifpb.charger_proxy.infra.client;

import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.ifpb.charger_proxy.web.dto.PaymentEventDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import java.util.function.Consumer;

/**
 * Cliente para comunicação com o Charger Manager
 *
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ChargerManagerClient {
    private final WebClient webClient;

    /**
     * @param onSuccess função chamada se o evento for enviado com sucesso
     * @param onError função chamada se ocorrer erro
     */
    public void send(PaymentEventDto request, Runnable onSuccess, Consumer<Throwable> onError) {
        webClient.post()
                .bodyValue(request)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> 
                    Mono.error(new RuntimeException("Erro no destino: " + response.statusCode())))
                .toBodilessEntity()
                .retry(1)
                .subscribe(
                    success -> {
                        if (onSuccess != null) onSuccess.run();
                    },
                    error -> {
                        if (onError != null) onError.accept(error);
                    }
                );
    }
}

