package com.financeapp.payroll.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for {@link PayrollEntity}.
 *
 * Spring Data generates the SQL implementation at runtime based on
 * method naming conventions. No boilerplate SQL required.
 */
@Repository
public interface PayrollRepository extends JpaRepository<PayrollEntity, Long> {

    /**
     * Find a payroll record by its unique business reference.
     * Used for idempotency checks before creating a new payroll.
     */
    Optional<PayrollEntity> findByPayrollReference(String payrollReference);

    /**
     * Check whether a payroll for the given period already exists.
     * Used to prevent duplicate processing of the same payroll period.
     */
    boolean existsByPayrollPeriod(String payrollPeriod);

    /**
     * Count payrolls created for a given period (for reference generation).
     * Used to generate sequential reference numbers: PAY-2026-08-0001, -0002, etc.
     */
    long countByPayrollPeriod(String payrollPeriod);
}
