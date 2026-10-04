package com.financeapp.finance.repository;

import com.financeapp.finance.domain.JournalEntryLineEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JournalEntryLineRepository extends JpaRepository<JournalEntryLineEntity, Long> {

    /**
     * Sums line amounts per account and side. Rows: [accountCode, accountName, lineType, sum].
     * Aggregation into debit/credit columns is done in the service to stay DB-agnostic.
     */
    @Query("select l.accountCode, l.accountName, l.lineType, sum(l.amount) "
            + "from JournalEntryLineEntity l "
            + "group by l.accountCode, l.accountName, l.lineType "
            + "order by l.accountCode")
    List<Object[]> sumByAccountAndSide();

    @Query("select l.accountCode, l.accountName, l.lineType, sum(l.amount) "
            + "from JournalEntryLineEntity l "
            + "where l.accountCode = :accountCode "
            + "group by l.accountCode, l.accountName, l.lineType")
    List<Object[]> sumForAccount(@Param("accountCode") String accountCode);

    Page<JournalEntryLineEntity> findByAccountCodeOrderByIdDesc(String accountCode, Pageable pageable);
}
