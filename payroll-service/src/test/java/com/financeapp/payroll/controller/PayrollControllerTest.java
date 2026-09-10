package com.financeapp.payroll.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financeapp.payroll.dto.ProcessPayrollRequest;
import com.financeapp.payroll.dto.ProcessPayrollResponse;
import com.financeapp.payroll.exception.DuplicatePayrollException;
import com.financeapp.payroll.exception.FinanceServiceException;
import com.financeapp.payroll.service.PayrollService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller slice tests for PayrollController.
 *
 * <p>Uses @WebMvcTest to load only the web layer (controller + exception handler).
 * The PayrollService is mocked. Tests focus on HTTP request/response mapping.</p>
 */
@WebMvcTest(PayrollController.class)
@DisplayName("PayrollController Tests")
class PayrollControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PayrollService payrollService;

    @Test
    @DisplayName("POST /api/payroll/process-grpc → 201 Created with response body")
    void shouldReturn201OnSuccess() throws Exception {
        ProcessPayrollResponse response = ProcessPayrollResponse.builder()
                .payrollReference("PAY-2026-08-0001")
                .payrollPeriod("2026-08")
                .employeeCount(25)
                .totalAmount(new BigDecimal("208333.33"))
                .status("PROCESSED")
                .journalReference("PAY-2026-08-0001")
                .financeTransport("GRPC")
                .build();

        given(payrollService.processPayrollViaGrpc(any())).willReturn(response);

        ProcessPayrollRequest request = new ProcessPayrollRequest();
        request.setPayrollPeriod("2026-08");
        request.setEmployeeCount(25);

        mockMvc.perform(post("/api/payroll/process-grpc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.payrollReference").value("PAY-2026-08-0001"))
                .andExpect(jsonPath("$.status").value("PROCESSED"))
                .andExpect(jsonPath("$.journalReference").value("PAY-2026-08-0001"))
                .andExpect(jsonPath("$.financeTransport").value("GRPC"));
    }

    @Test
    @DisplayName("POST /api/payroll/process-grpc with blank period → 400 Bad Request")
    void shouldReturn400WhenPayrollPeriodMissing() throws Exception {
        ProcessPayrollRequest request = new ProcessPayrollRequest();
        request.setPayrollPeriod("");
        request.setEmployeeCount(10);

        mockMvc.perform(post("/api/payroll/process-grpc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation Failed"));
    }

    @Test
    @DisplayName("POST /api/payroll/process-grpc with invalid period format → 400 Bad Request")
    void shouldReturn400WhenPeriodFormatInvalid() throws Exception {
        ProcessPayrollRequest request = new ProcessPayrollRequest();
        request.setPayrollPeriod("08-2026"); // Wrong format
        request.setEmployeeCount(10);

        mockMvc.perform(post("/api/payroll/process-grpc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/payroll/process-grpc with employeeCount=0 → 400 Bad Request")
    void shouldReturn400WhenEmployeeCountZero() throws Exception {
        ProcessPayrollRequest request = new ProcessPayrollRequest();
        request.setPayrollPeriod("2026-08");
        request.setEmployeeCount(0);

        mockMvc.perform(post("/api/payroll/process-grpc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/payroll/process-grpc with duplicate period → 409 Conflict")
    void shouldReturn409WhenDuplicatePeriod() throws Exception {
        given(payrollService.processPayrollViaGrpc(any()))
                .willThrow(new DuplicatePayrollException("2026-08"));

        ProcessPayrollRequest request = new ProcessPayrollRequest();
        request.setPayrollPeriod("2026-08");
        request.setEmployeeCount(25);

        mockMvc.perform(post("/api/payroll/process-grpc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Duplicate Payroll"))
                .andExpect(jsonPath("$.payrollPeriod").value("2026-08"));
    }

    @Test
    @DisplayName("POST /api/payroll/process-grpc when Finance is unavailable → 503 Service Unavailable")
    void shouldReturn503WhenFinanceUnavailable() throws Exception {
        given(payrollService.processPayrollViaGrpc(any()))
                .willThrow(FinanceServiceException.serviceUnavailable("Finance Service is down"));

        ProcessPayrollRequest request = new ProcessPayrollRequest();
        request.setPayrollPeriod("2026-09");
        request.setEmployeeCount(10);

        mockMvc.perform(post("/api/payroll/process-grpc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.title").value("Finance Service Unavailable"));
    }

    @Test
    @DisplayName("POST /api/payroll/process-grpc when Finance times out → 504 Gateway Timeout")
    void shouldReturn504WhenFinanceTimesOut() throws Exception {
        given(payrollService.processPayrollViaGrpc(any()))
                .willThrow(FinanceServiceException.timeout("Deadline exceeded"));

        ProcessPayrollRequest request = new ProcessPayrollRequest();
        request.setPayrollPeriod("2026-10");
        request.setEmployeeCount(15);

        mockMvc.perform(post("/api/payroll/process-grpc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isGatewayTimeout())
                .andExpect(jsonPath("$.title").value("Finance Service Timeout"));
    }

    @Test
    @DisplayName("GET /api/payroll/health → 200 OK")
    void shouldReturnHealthOk() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .get("/api/payroll/health"))
                .andExpect(status().isOk());
    }
}
