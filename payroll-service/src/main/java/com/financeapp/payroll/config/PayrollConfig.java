package com.financeapp.payroll.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Configuration for the Payroll Service.
 *
 * <p>Configures the WebClient for REST-based Finance calls (benchmark comparison).
 * The gRPC channel is configured automatically by net.devh's starter via application.yml.</p>
 */
@Configuration
public class PayrollConfig {

    @Value("${finance.rest.base-url:http://localhost:8082}")
    private String financeRestBaseUrl;

    /**
     * WebClient for REST-based Finance Service communication.
     * Used only by {@link com.financeapp.payroll.client.RestFinanceClient} for benchmarks.
     */
    @Bean
    public WebClient financeWebClient() {
        return WebClient.builder()
                .baseUrl(financeRestBaseUrl)
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("Accept", "application/json")
                .defaultHeader("X-Source-System", "PAYROLL-SERVICE")
                .build();
    }
}
