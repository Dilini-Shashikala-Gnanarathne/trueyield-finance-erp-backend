package com.financeapp.payroll.client;

import com.financeapp.payroll.dto.FinanceJournalRequest;
import com.financeapp.payroll.dto.FinanceJournalResponse;

/**
 * Port (interface) for Finance Service communication.
 *
 * <p>This interface is the key architectural abstraction that allows the
 * {@code PayrollService} (business logic) to remain completely independent
 * of the transport mechanism used to communicate with Finance Service.</p>
 *
 * <p>Two implementations are provided:
 * <ul>
 *   <li>{@link GrpcFinanceClient} — uses gRPC/Protobuf (primary, production-ready)</li>
 *   <li>{@link RestFinanceClient} — uses REST/JSON (benchmark comparison)</li>
 * </ul>
 * </p>
 *
 * <p>This is an application of the Ports and Adapters (Hexagonal Architecture) pattern.
 * The PayrollService depends only on this interface; the actual implementation is
 * injected by Spring and selected via configuration.</p>
 */
public interface FinanceClient {

    /**
     * Request Finance Service to create a journal entry for the given payroll.
     *
     * @param request the journal entry request containing reference, amounts, accounts
     * @return the response from Finance Service
     * @throws com.financeapp.payroll.exception.FinanceServiceException if Finance Service fails
     */
    FinanceJournalResponse createJournalEntry(FinanceJournalRequest request);
}
