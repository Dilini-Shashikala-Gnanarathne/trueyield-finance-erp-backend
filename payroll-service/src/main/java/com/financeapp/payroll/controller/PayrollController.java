package com.financeapp.payroll.controller;

import com.financeapp.payroll.dto.ProcessPayrollRequest;
import com.financeapp.payroll.dto.ProcessPayrollResponse;
import com.financeapp.payroll.service.PayrollService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for payroll operations.
 *
 * <p>This is the external API surface of the Payroll Service.
 * Clients (via the API Gateway) interact with this controller.</p>
 *
 * <h2>Two Endpoints</h2>
 * <p>Two endpoints are exposed for benchmark comparison:
 * <ul>
 *   <li>{@code POST /api/payroll/process-grpc} — uses gRPC to Finance Service (primary)</li>
 *   <li>{@code POST /api/payroll/process-rest} — uses REST to Finance Service (benchmark)</li>
 * </ul>
 * Both perform identical business logic; only the internal transport differs.</p>
 *
 * <h2>Validation</h2>
 * <p>{@code @Valid} triggers Bean Validation on the request body.
 * Validation failures are handled by {@link com.financeapp.payroll.exception.GlobalExceptionHandler}
 * and return HTTP 400 with detailed field error messages.</p>
 */
@Slf4j
@RestController
@RequestMapping("/api/payroll")
@RequiredArgsConstructor
public class PayrollController {

    private final PayrollService payrollService;

    /**
     * Process payroll using gRPC to communicate with Finance Service.
     *
     * <p>This is the <strong>primary endpoint</strong> demonstrating the
     * REST at edge, gRPC inside pattern:
     * <pre>
     *   Client → REST → API Gateway → REST → Payroll → gRPC → Finance
     * </pre>
     * </p>
     *
     * @param request payroll period and employee count
     * @return 201 Created with payroll and journal reference
     */
    @PostMapping("/process-grpc")
    public ResponseEntity<ProcessPayrollResponse> processPayrollViaGrpc(
            @Valid @RequestBody ProcessPayrollRequest request) {

        log.info("REST request received for gRPC-backed payroll. period={}, employees={}",
                request.getPayrollPeriod(), request.getEmployeeCount());

        ProcessPayrollResponse response = payrollService.processPayrollViaGrpc(request);

        log.info("Payroll gRPC processing complete. reference={}, status={}",
                response.getPayrollReference(), response.getStatus());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Process payroll using REST to communicate with Finance Service.
     *
     * <p>This endpoint exists <strong>solely for benchmark comparison</strong>.
     * It allows measuring REST vs gRPC performance under the same workload
     * with identical business logic and database operations.
     * <pre>
     *   Client → REST → API Gateway → REST → Payroll → REST → Finance
     * </pre>
     * </p>
     *
     * @param request payroll period and employee count
     * @return 201 Created with payroll and journal reference
     */
    @PostMapping("/process-rest")
    public ResponseEntity<ProcessPayrollResponse> processPayrollViaRest(
            @Valid @RequestBody ProcessPayrollRequest request) {

        log.info("REST request received for REST-backed payroll. period={}, employees={}",
                request.getPayrollPeriod(), request.getEmployeeCount());

        ProcessPayrollResponse response = payrollService.processPayrollViaRest(request);

        log.info("Payroll REST processing complete. reference={}, status={}",
                response.getPayrollReference(), response.getStatus());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Convenience alias for the primary gRPC-backed endpoint.
     * This is the endpoint used in the article's main demonstration.
     */
    @PostMapping("/process")
    public ResponseEntity<ProcessPayrollResponse> processPayroll(
            @Valid @RequestBody ProcessPayrollRequest request) {
        return processPayrollViaGrpc(request);
    }

    /**
     * Health check endpoint for the API Gateway to verify Payroll Service is up.
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Payroll Service is running");
    }
}
