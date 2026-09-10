package com.financeapp.payslip.service;

import com.financeapp.payslip.event.PayrollProcessedEvent;
import com.financeapp.payslip.model.PaySlip;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Generates simulated pay slips for each employee in a payroll run.
 *
 * <h2>Real-World Extension Points</h2>
 * <p>In production, this service would:</p>
 * <ol>
 *   <li>Query HR database for actual employee records (name, salary grade, tax bracket)</li>
 *   <li>Apply individual tax calculations (varies by income bracket)</li>
 *   <li>Render a PDF using iText / JasperReports / Apache PDFBox</li>
 *   <li>Upload to S3 / Azure Blob / GCS as {@code payslips/{period}/{employeeId}.pdf}</li>
 *   <li>Send a pre-signed download URL to each employee via email</li>
 * </ol>
 */
@Slf4j
@Service
public class PayslipGeneratorService {

    // Deduction rates — fixed for simulation, real system would use tax tables
    private static final BigDecimal TAX_RATE         = new BigDecimal("0.20"); // 20%
    private static final BigDecimal PROVIDENT_RATE   = new BigDecimal("0.08"); // 8%

    /**
     * Generates individual pay slips for all employees in a payroll run.
     *
     * @param event the payroll event from Kafka
     * @return list of generated pay slips (one per employee)
     */
    public List<PaySlip> generatePaySlips(PayrollProcessedEvent event) {
        int count = event.getEmployeeCount();
        BigDecimal totalAmount = event.getTotalAmount();

        // Derive per-employee gross from the aggregated total
        BigDecimal perEmployeeGross = totalAmount
                .divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);

        log.info("📄 Generating {} pay slips for period={}. " +
                        "Total={} {}, Per-employee gross={}",
                count,
                event.getPayrollPeriod(),
                totalAmount,
                event.getCurrency(),
                perEmployeeGross);

        List<PaySlip> slips = new ArrayList<>(count);
        LocalDate today = LocalDate.now();

        for (int i = 1; i <= count; i++) {
            PaySlip slip = buildPaySlip(event, i, perEmployeeGross, today);
            slips.add(slip);

            // Log individual slip (first 3 + last 1 to avoid flooding logs for large payrolls)
            if (i <= 3 || i == count) {
                log.info(slip.toTextBlock());
            } else if (i == 4 && count > 4) {
                log.info("    ... ({} more pay slips generated, suppressed for readability) ...", count - 4);
            }
        }

        return slips;
    }

    private PaySlip buildPaySlip(
            PayrollProcessedEvent event,
            int employeeNumber,
            BigDecimal grossSalary,
            LocalDate generatedOn) {

        // Simulate realistic names — in production, fetch from HR DB by employeeId
        String employeeId   = String.format("EMP-%05d", employeeNumber);
        String employeeName = generateSimulatedName(employeeNumber);

        // Calculate deductions
        BigDecimal tax = grossSalary.multiply(TAX_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal pf  = grossSalary.multiply(PROVIDENT_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal net = grossSalary.subtract(tax).subtract(pf);

        return PaySlip.builder()
                .employeeId(employeeId)
                .employeeName(employeeName)
                .payrollPeriod(event.getPayrollPeriod())
                .payrollReference(event.getPayrollReference())
                .grossSalary(grossSalary)
                .taxDeduction(tax)
                .providentFundDeduction(pf)
                .netPay(net)
                .currency(event.getCurrency() != null ? event.getCurrency() : "USD")
                .generatedOn(generatedOn)
                .build();
    }

    /**
     * Generates a simulated employee name based on their number.
     * In production, this is replaced by a real HR API / database call.
     */
    private String generateSimulatedName(int n) {
        String[] firstNames = {
            "Alice", "Bob", "Carol", "David", "Emma",
            "Frank", "Grace", "Henry", "Isabel", "James",
            "Karen", "Liam", "Mary", "Noah", "Olivia"
        };
        String[] lastNames = {
            "Smith", "Johnson", "Williams", "Brown", "Jones",
            "Garcia", "Miller", "Davis", "Wilson", "Taylor"
        };
        String first = firstNames[(n - 1) % firstNames.length];
        String last  = lastNames[(n - 1) % lastNames.length];
        return first + " " + last;
    }
}
