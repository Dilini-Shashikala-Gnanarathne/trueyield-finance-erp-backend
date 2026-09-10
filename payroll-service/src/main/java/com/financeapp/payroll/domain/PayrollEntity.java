package com.financeapp.payroll.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * JPA Entity representing a Payroll record.
 *
 * <p><strong>Why BigDecimal for financial amounts?</strong>
 * Java's {@code double} and {@code float} types use IEEE 754 binary floating-point,
 * which cannot represent many decimal fractions exactly. For example:
 * {@code 0.1 + 0.2 = 0.30000000000000004} in IEEE 754.
 * For payroll totals involving thousands of employees and cents, these rounding
 * errors accumulate and can result in incorrect financial statements.
 * {@code BigDecimal} provides arbitrary-precision decimal arithmetic, which is
 * the correct type for all monetary calculations. It is stored as NUMERIC(19,4)
 * in PostgreSQL to preserve up to 4 decimal places with 15 integer digits.</p>
 *
 * <p><strong>Why an enum for status?</strong>
 * Storing status as a string enum ensures that only valid transitions are possible
 * at the application level, and the stored string is human-readable in the database
 * without needing to join a lookup table.</p>
 */
@Entity
@Table(
        name = "payroll",
        uniqueConstraints = {
                // Ensures the same payroll period is not processed twice.
                // This is the primary business idempotency guard at the payroll level.
                @UniqueConstraint(
                        name = "uk_payroll_reference",
                        columnNames = "payroll_reference"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class PayrollEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Unique business reference for this payroll run.
     * Format: PAY-{YEAR}-{MONTH}-{SEQUENCE}, e.g., PAY-2026-08-0001.
     * Used as the idempotency key when calling Finance Service via gRPC.
     */
    @Column(name = "payroll_reference", nullable = false, unique = true, length = 50)
    private String payrollReference;

    /**
     * The payroll period in YYYY-MM format, e.g., "2026-08".
     */
    @Column(name = "payroll_period", nullable = false, length = 7)
    private String payrollPeriod;

    /**
     * Number of employees included in this payroll run.
     */
    @Column(name = "employee_count", nullable = false)
    private Integer employeeCount;

    /**
     * Total gross payroll amount.
     * NUMERIC(19,4) stores up to 15 integer digits and 4 decimal places.
     * This supports payrolls up to approximately $999,999,999,999,999.
     */
    @Column(name = "total_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal totalAmount;

    /**
     * Current processing status of the payroll record.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PayrollStatus status;

    /**
     * Reference to the journal entry created in Finance Service.
     * Populated after successful gRPC call to Finance Service.
     * Null if journal creation has not completed.
     */
    @Column(name = "journal_reference", length = 100)
    private String journalReference;

    /**
     * Name of the transport mechanism used to call Finance Service.
     * Values: "GRPC" or "REST". Used for benchmark comparison.
     */
    @Column(name = "finance_transport", length = 10)
    private String financeTransport;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
