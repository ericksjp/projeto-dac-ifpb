package com.ifpb.charger_proxy.runner;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.asaas.apisdk.exceptions.ApiError;
import com.asaas.apisdk.models.ListCustomersParameters;
import com.asaas.apisdk.services.CustomerService;
import com.ifpb.charger_proxy.exception.AsaasClientException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class AsaasHealthCheck implements ApplicationRunner {

    private final CustomerService customerService;

    @Override
    public void run(ApplicationArguments args) {
        try {
            ListCustomersParameters params = ListCustomersParameters.builder()
                    .limit(0L)
                    .build();

            customerService.listCustomers(params);

            log.info("Asaas API startup connection test succeeded.");

        } catch (ApiError e) {
            int status = e.getStatus();

            if (status == 401 || status == 403) {
                log.error("Asaas API authentication failed (status {}). Check API key and environment.", status, e);
                throw new AsaasClientException("Invalid Asaas API key or insufficient permissions", e);
            }

            log.error("Asaas API responded with error (status {}): {}", status, e.getMessage(), e);

            throw new AsaasClientException("Failed to connect to Asaas API during startup", e);

        } catch (Exception e) {
            log.error("Unexpected error while testing Asaas API connection", e);
            throw new AsaasClientException("Unexpected error while connecting to Asaas API", e);
        }
    }
}
