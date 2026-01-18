package com.ifpb.charger_proxy.config;

import com.asaas.apisdk.AsaasSdk;
import com.asaas.apisdk.config.ApiKeyAuthConfig;
import com.asaas.apisdk.config.AsaasSdkConfig;
import com.asaas.apisdk.http.Environment;

import com.asaas.apisdk.services.CustomerService;
import com.asaas.apisdk.services.PaymentService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AsaasConfig {

    @Value("${asaas.api.key}")
    private String asaasApiKey;

    @Value("${asaas.env}")
    private String asaasEnvironment;

    @Bean
    public AsaasSdk asaasSdk() {
        AsaasSdkConfig config = AsaasSdkConfig.builder()
                .apiKeyAuthConfig(ApiKeyAuthConfig.builder().apiKey(asaasApiKey).build())
                .build();

        config.setEnvironment(getEnvironment(asaasEnvironment));

        return new AsaasSdk(config);
    }

    @Bean
    public CustomerService customerService(AsaasSdk asaasSdk) {
        return asaasSdk.customer;
    }

    @Bean
    public PaymentService paymentService(AsaasSdk asaasSdk) {
        return asaasSdk.payment;
    }

    private Environment getEnvironment(String env) {
        if ("PRODUCTION".equalsIgnoreCase(env)) {
            return Environment.PRODUCTION;
        } else {
            return Environment.SANDBOX;
        }
    }
}
