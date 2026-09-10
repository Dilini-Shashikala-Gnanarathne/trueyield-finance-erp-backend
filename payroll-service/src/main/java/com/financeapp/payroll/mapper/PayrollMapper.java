package com.financeapp.payroll.mapper;

import com.financeapp.payroll.domain.PayrollEntity;
import com.financeapp.payroll.dto.ProcessPayrollResponse;
import org.springframework.stereotype.Component;

/**
 * Mapper between {@link PayrollEntity} and {@link ProcessPayrollResponse}.
 *
 * <p>A dedicated mapper keeps the entity and DTO decoupled.
 * We use a simple hand-written mapper here rather than MapStruct to keep
 * the project dependencies minimal and the code transparent.</p>
 */
@Component
public class PayrollMapper {

    public ProcessPayrollResponse toResponse(PayrollEntity entity) {
        return ProcessPayrollResponse.builder()
                .payrollReference(entity.getPayrollReference())
                .payrollPeriod(entity.getPayrollPeriod())
                .employeeCount(entity.getEmployeeCount())
                .totalAmount(entity.getTotalAmount())
                .status(entity.getStatus().name())
                .journalReference(entity.getJournalReference())
                .financeTransport(entity.getFinanceTransport())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
