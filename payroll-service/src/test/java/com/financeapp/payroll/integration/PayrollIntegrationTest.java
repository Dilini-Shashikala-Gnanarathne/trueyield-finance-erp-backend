package com.financeapp.payroll.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financeapp.payroll.dto.ProcessPayrollRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for Payroll Service using Testcontainers.
 *
 * <p>These tests start a real PostgreSQL database in a Docker container,
 * run Flyway migrations, and test the full request → DB flow.
 * The Finance Service gRPC call is tested by starting Finance Service separately
 * or using a mock gRPC server. In this integration test, we use a stub for Finance.</p>
 *
 * <p>Testcontainers automatically:
 * <ul>
 *   <li>Starts a PostgreSQL container before tests</li>
 *   <li>Injects the correct JDBC URL via @DynamicPropertySource</li>
 *   <li>Stops the container after tests</li>
 * </ul>
 * </p>
 *
 * <p><strong>Note:</strong> These tests require Docker to be running.
 * They are slower than unit tests but verify real database behavior.
 * Run with: {@code mvn test -Dtest=PayrollIntegrationTest}</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Payroll Service Integration Tests")
class PayrollIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("payroll_test_db")
            .withUsername("test_user")
            .withPassword("test_pass");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        // Point gRPC to a non-existent address — Finance calls will fail with UNAVAILABLE
        // which is the expected behavior for integration tests without Finance Service running
        registry.add("grpc.client.finance-service.address", () -> "localhost:19090");
        // Very short deadline for fast test failure
        registry.add("grpc.client.finance-service.deadline", () -> "1s");
        // REST Finance also points to non-existent server
        registry.add("finance.rest.base-url", () -> "http://localhost:19082");
        registry.add("finance.rest.timeout-seconds", () -> "1");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @Order(1)
    @DisplayName("POST /api/payroll/process-grpc with valid request → Finance unavailable → 503")
    void shouldReturn503WhenFinanceUnavailable() throws Exception {
        ProcessPayrollRequest request = new ProcessPayrollRequest();
        request.setPayrollPeriod("2025-01");
        request.setEmployeeCount(10);

        mockMvc.perform(post("/api/payroll/process-grpc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isServiceUnavailable());
    }

    @Test
    @Order(2)
    @DisplayName("POST /api/payroll/process-grpc with invalid period format → 400")
    void shouldRejectInvalidPeriodFormat() throws Exception {
        ProcessPayrollRequest request = new ProcessPayrollRequest();
        request.setPayrollPeriod("January-2026");
        request.setEmployeeCount(10);

        mockMvc.perform(post("/api/payroll/process-grpc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation Failed"));
    }

    @Test
    @Order(3)
    @DisplayName("POST /api/payroll/process-grpc with missing body → 400")
    void shouldRejectMissingBody() throws Exception {
        mockMvc.perform(post("/api/payroll/process-grpc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(4)
    @DisplayName("Sending same period twice → 409 Conflict on second request")
    void shouldReturn409OnDuplicatePeriod() throws Exception {
        // First request — Finance will be unavailable but payroll record is created as PENDING then FAILED
        ProcessPayrollRequest request = new ProcessPayrollRequest();
        request.setPayrollPeriod("2025-02");
        request.setEmployeeCount(5);

        // First request fails at Finance (expected in this test context)
        mockMvc.perform(post("/api/payroll/process-grpc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isServiceUnavailable());

        // Second request with same period → should be rejected as duplicate
        // (The payroll record for 2025-02 was created even though Finance failed)
        mockMvc.perform(post("/api/payroll/process-grpc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Duplicate Payroll"));
    }
}
