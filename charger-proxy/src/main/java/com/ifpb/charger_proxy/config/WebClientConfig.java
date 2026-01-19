package com.ifpb.charger_proxy.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;


@Configuration
public class WebClientConfig {

    @Value("${charger-manager.payment-events.publish-url}")
    private String chargerManagerBaseUrl;

    @Bean
    public WebClient chargerManagerWebClient() {
        return WebClient.builder()
                .baseUrl(chargerManagerBaseUrl)
                .build();
    }
}

