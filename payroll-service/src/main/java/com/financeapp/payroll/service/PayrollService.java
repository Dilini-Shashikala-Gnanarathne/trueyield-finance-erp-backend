package com.financeapp.payroll.service;

import com.financeapp.payroll.client.FinanceClient;
import com.financeapp.payroll.domain.PayrollEntity;
import com.financeapp.payroll.domain.PayrollRepository;
import com.financeapp.payroll.domain.PayrollStatus;
import com.financeapp.payroll.dto.*;
import com.financeapp.payroll.event.PayrollEventPublisher;
import com.financeapp.payroll.exception.DuplicatePayrollException;
import com.financeapp.payroll.exception.FinanceServiceException;
import com.financeapp.payroll.mapper.PayrollMapper;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Core business service for payroll processing.
 *
 * <p>This service contains all payroll business logic and is completely
 * independent of the transport mechanism used to communicate with Finance Service.
 * It works with the {@link FinanceClient} interface — the actual implementation
 * (gRPC or REST) is injected by Spring.</p>
 *
 * <h2>Processing Flow</h2>
 * <pre>
 * 1. Check for duplicate payroll period
 * 2. Calculate payroll total
 * 3. Generate unique payroll reference
 * 4. Persist payroll record (PENDING status)
 * 5. Call Finance Service (gRPC or REST)
 * 6. Update payroll record (PROCESSED or FAILED status)
 * 7. Return response
 * </pre>
 *
 * <h2>Transaction Design</h2>
 * <p>The initial payroll record save and the Finance Service call are in the same
 * method but the transaction only wraps DB operations. The gRPC call happens outside
 * a database transaction to avoid holding DB connections during network I/O.
 * Status update after gRPC uses a separate transaction.</p>
 */
@Slf4j
@Service
public class PayrollService {

    private final PayrollRepository payrollRepository;
    private final FinanceClient grpcFinanceClient;
    private final FinanceClient restFinanceClient;
    private final PayrollMapper payrollMapper;
    private final PayrollEventPublisher eventPublisher;

    // Average salary per employee for demonstration payroll calculation.
    // In production this would come from HR system data.
    private static final BigDecimal AVERAGE_SALARY_PER_EMPLOYEE =
            new BigDecimal("100000.00"); // $100,000 per year

    private static final DateTimeFormatter PERIOD_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM");

    public PayrollService(
            PayrollRepository payrollRepository,
            @Qualifier("grpcFinanceClient") FinanceClient grpcFinanceClient,
            @Qualifier("restFinanceClient") FinanceClient restFinanceClient,
            PayrollMapper payrollMapper,
            PayrollEventPublisher eventPublisher) {
        this.payrollRepository = payrollRepository;
        this.grpcFinanceClient = grpcFinanceClient;
        this.restFinanceClient = restFinanceClient;
        this.payrollMapper = payrollMapper;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Process payroll using gRPC to communicate with Finance Service.
     * This is the primary production path.
     */
    public ProcessPayrollResponse processPayrollViaGrpc(ProcessPayrollRequest request) {
        return processPayroll(request, grpcFinanceClient, "GRPC");
    }

    /**
     * Process payroll using REST to communicate with Finance Service.
     * This path exists for benchmark comparison only.
     */
    public ProcessPayrollResponse processPayrollViaRest(ProcessPayrollRequest request) {
        return processPayroll(request, restFinanceClient, "REST");
    }

    /**
     * Core payroll processing logic — transport-agnostic.
     *
     * @param request       validated payroll request
     * @param financeClient the transport-specific Finance client to use
     * @param transport     label for logging and response ("GRPC" or "REST")
     */
    private ProcessPayrollResponse processPayroll(
            ProcessPayrollRequest request,
            FinanceClient financeClient,
            String transport) {

        // Set up MDC for structured logging — every log statement in this thread
        // will include the correlation ID and payroll period.
        String correlationId = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        MDC.put("correlationId", correlationId);
        MDC.put("payrollPeriod", request.getPayrollPeriod());
        MDC.put("transport", transport);

        try {
            log.info("Starting payroll processing. period={}, employees={}, transport={}",
                    request.getPayrollPeriod(), request.getEmployeeCount(), transport);

            // Step 1: Guard against duplicate payroll periods
            checkForDuplicatePayrollPeriod(request.getPayrollPeriod());

            // Step 2: Calculate payroll total
            BigDecimal totalAmount = calculatePayrollTotal(request.getEmployeeCount());

            // Step 3: Generate unique payroll reference
            String payrollReference = generatePayrollReference(request.getPayrollPeriod());

            MDC.put("payrollReference", payrollReference);
            log.info("Generated payroll reference. reference={}, total={}", payrollReference, totalAmount);

            // Step 4: Persist payroll record with PENDING status
            PayrollEntity payroll = createAndSavePayroll(
                    request, payrollReference, totalAmount, transport);

            // Step 5 & 6: Call Finance Service and update payroll record
            return callFinanceAndUpdatePayroll(payroll, financeClient, transport);

        } finally {
            MDC.clear();
        }
    }

    /**
     * Validates that no payroll has been processed for the given period.
     *
     * <p>This prevents double-processing of payroll (e.g., running January payroll twice).
     * This is separate from the Finance Service idempotency check, which prevents
     * duplicate journal entries for a given reference.</p>
     */
    private void checkForDuplicatePayrollPeriod(String payrollPeriod) {
        if (payrollRepository.existsByPayrollPeriod(payrollPeriod)) {
            log.warn("Duplicate payroll period detected. period={}", payrollPeriod);
            throw new DuplicatePayrollException(payrollPeriod);
        }
    }

    /**
     * Calculates the total payroll amount.
     *
     * <p>Simulates a realistic payroll calculation:
     * - Annual salary = employee count × average salary
     * - Monthly payroll = annual / 12
     * In production, this would query an HR database for actual employee salaries.</p>
     */
    private BigDecimal calculatePayrollTotal(int employeeCount) {
        return AVERAGE_SALARY_PER_EMPLOYEE
                .multiply(BigDecimal.valueOf(employeeCount))
                .divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);
    }

    /**
     * Generates a sequential payroll reference for the given period.
     *
     * <p>Format: PAY-{YYYY}-{MM}-{SEQUENCE}
     * Example: PAY-2026-08-0001, PAY-2026-08-0002
     *
     * The sequence is based on the count of existing payrolls for the period.
     * This is safe because we guard against duplicates before reaching this point.</p>
     */
    private String generatePayrollReference(String payrollPeriod) {
        long count = payrollRepository.countByPayrollPeriod(payrollPeriod);
        YearMonth ym = YearMonth.parse(payrollPeriod, PERIOD_FORMATTER);
        return String.format("PAY-%d-%02d-%04d",
                ym.getYear(), ym.getMonthValue(), count + 1);
    }

    @Transactional
    protected PayrollEntity createAndSavePayroll(
            ProcessPayrollRequest request,
            String payrollReference,
            BigDecimal totalAmount,
            String transport) {

        PayrollEntity payroll = new PayrollEntity();
        payroll.setPayrollReference(payrollReference);
        payroll.setPayrollPeriod(request.getPayrollPeriod());
        payroll.setEmployeeCount(request.getEmployeeCount());
        payroll.setTotalAmount(totalAmount);
        payroll.setStatus(PayrollStatus.PENDING);
        payroll.setFinanceTransport(transport);

        PayrollEntity saved = payrollRepository.save(payroll);
        log.info("Payroll record created. id={}, reference={}, status={}",
                saved.getId(), saved.getPayrollReference(), saved.getStatus());
        return saved;
    }

    /**
     * Calls Finance Service to create the journal entry, then updates payroll status.
     *
     * <h2>Idempotency in the Finance Call</h2>
     * <p>The payroll reference (e.g., PAY-2026-08-0001) serves as the idempotency key
     * in the Finance Service. If this call succeeds on the first attempt, Finance creates
     * a new journal. If the call is retried (e.g., after a network failure), Finance
     * detects the duplicate reference and returns the existing journal — no double entry.</p>
     *
     * <h2>Failure Handling</h2>
     * <p>If Finance Service fails, the payroll status is updated to FAILED.
     * The exception is re-thrown to propagate to the controller, which maps it
     * to the appropriate HTTP status code.</p>
     */
    @Transactional
    protected ProcessPayrollResponse callFinanceAndUpdatePayroll(
            PayrollEntity payroll,
            FinanceClient financeClient,
            String transport) {

        // Update to PROCESSING before making the gRPC call
        payroll.setStatus(PayrollStatus.PROCESSING);
        payrollRepository.save(payroll);

        FinanceJournalRequest journalRequest = FinanceJournalRequest.builder()
                .reference(payroll.getPayrollReference())
                .description("Payroll for period " + payroll.getPayrollPeriod() +
                        " - " + payroll.getEmployeeCount() + " employees")
                .debitAccount("SALARY_EXPENSE")
                .creditAccount("PAYROLL_PAYABLE")
                .amount(payroll.getTotalAmount())
                .currency("USD")
                .sourceSystem("PAYROLL-SERVICE")
                .build();

        log.info("Calling Finance Service. reference={}, transport={}, amount={}",
                payroll.getPayrollReference(), transport, payroll.getTotalAmount());

        try {
            FinanceJournalResponse journalResponse = financeClient.createJournalEntry(journalRequest);

            // Finance call succeeded — update payroll to PROCESSED
            payroll.setStatus(PayrollStatus.PROCESSED);
            payroll.setJournalReference(journalResponse.getJournalReference());
            payrollRepository.save(payroll);

            log.info("Payroll processed successfully. payrollReference={}, journalReference={}, wasIdempotent={}",
                    payroll.getPayrollReference(),
                    journalResponse.getJournalReference(),
                    journalResponse.isWasIdempotent());

            // Publish async Kafka event — fire-and-forget.
            // Kafka failure MUST NOT fail the payroll response; the DB record is already PROCESSED.
            // Downstream consumers (notification-service, audit-service, etc.) catch up on recovery.
            eventPublisher.publishPayrollProcessed(
                    payroll.getPayrollReference(),
                    payroll.getPayrollPeriod(),
                    payroll.getEmployeeCount(),
                    payroll.getTotalAmount(),
                    transport,
                    journalResponse.getJournalReference(),
                    payroll.getCreatedAt() != null ? payroll.getCreatedAt().toString() : null
            );

            return payrollMapper.toResponse(payroll);

        } catch (FinanceServiceException e) {
            // Finance call failed — update payroll to FAILED before re-throwing
            payroll.setStatus(PayrollStatus.FAILED);
            payrollRepository.save(payroll);

            log.error("Payroll processing failed. payrollReference={}, errorType={}, message={}",
                    payroll.getPayrollReference(), e.getErrorType(), e.getMessage());

            throw e; // Re-throw to propagate to GlobalExceptionHandler
        }
    }
}
