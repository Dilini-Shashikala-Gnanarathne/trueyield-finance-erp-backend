package com.financeapp.payslip.model;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

/**
 * Represents a single employee's pay slip for a payroll period.
 *
 * <p>In a real system, this data would come from the HR/Employee database.
 * Here we simulate realistic values based on the aggregated payroll totals.</p>
 */
@Value
@Builder
public class PaySlip {

    /** Synthetic employee ID — e.g. EMP-00001 */
    String employeeId;

    /** Display name — simulated for demo purposes */
    String employeeName;

    /** Payroll period this slip belongs to */
    String payrollPeriod;

    /** Parent payroll run reference */
    String payrollReference;

    /** Gross monthly salary for this employee */
    BigDecimal grossSalary;

    /** Income tax deduction (simulated at 20%) */
    BigDecimal taxDeduction;

    /** Provident fund deduction (simulated at 8%) */
    BigDecimal providentFundDeduction;

    /** Net take-home pay = gross - tax - PF */
    BigDecimal netPay;

    /** Currency code */
    String currency;

    /** Date the slip was generated */
    LocalDate generatedOn;

    /**
     * Formats this pay slip as a structured text block.
     * In production this would render a PDF using iText or JasperReports.
     */
    public String toTextBlock() {
        return String.format("""
                ┌─────────────────────────────────────────────┐
                │           EMPLOYEE PAY SLIP                 │
                ├─────────────────────────────────────────────┤
                │  Employee ID   : %-28s│
                │  Employee Name : %-28s│
                │  Period        : %-28s│
                │  Payroll Ref   : %-28s│
                ├─────────────────────────────────────────────┤
                │  Gross Salary  : %s %-22s│
                │  Tax (20%%)    : %s %-22s│
                │  PF  (8%%)     : %s %-22s│
                ├─────────────────────────────────────────────┤
                │  NET PAY       : %s %-22s│
                ├─────────────────────────────────────────────┤
                │  Generated On  : %-28s│
                └─────────────────────────────────────────────┘
                """,
                employeeId,
                employeeName,
                payrollPeriod,
                payrollReference,
                currency, grossSalary.setScale(2, RoundingMode.HALF_UP),
                currency, taxDeduction.setScale(2, RoundingMode.HALF_UP),
                currency, providentFundDeduction.setScale(2, RoundingMode.HALF_UP),
                currency, netPay.setScale(2, RoundingMode.HALF_UP),
                generatedOn
        );
    }
}
