-- =============================================================================
-- V1__create_finance_tables.sql
--
-- Flyway migration: creates Finance Service tables.
--
-- Design notes:
-- - journal_entry.reference has a UNIQUE constraint for idempotency.
--   This is the database-level guard that prevents duplicate journal entries
--   even under concurrent gRPC retries.
-- - All amounts use NUMERIC(19,4) — not FLOAT/DOUBLE — for financial precision.
-- - journal_entry_line allows multiple debit/credit lines per entry,
--   supporting complex multi-line accounting entries.
-- =============================================================================

-- Main journal entry table
CREATE TABLE IF NOT EXISTS journal_entry (
    id            BIGSERIAL       PRIMARY KEY,
    reference     VARCHAR(100)    NOT NULL UNIQUE,
    description   VARCHAR(500)    NOT NULL,
    entry_type    VARCHAR(30)     NOT NULL DEFAULT 'GENERAL_LEDGER',
    currency      CHAR(3)         NOT NULL,
    total_amount  NUMERIC(19, 4)  NOT NULL CHECK (total_amount > 0),
    status        VARCHAR(20)     NOT NULL DEFAULT 'CREATED',
    source_system VARCHAR(100)    NOT NULL DEFAULT 'UNKNOWN',
    created_at    TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_journal_entry_status
        CHECK (status IN ('CREATED', 'POSTED', 'REVERSED', 'FAILED')),
    CONSTRAINT chk_journal_entry_type
        CHECK (entry_type IN ('PAYROLL', 'ACCOUNTS_PAYABLE', 'ACCOUNTS_RECEIVABLE', 'GENERAL_LEDGER'))
);

-- Idempotency index (also enforced by unique constraint above)
CREATE UNIQUE INDEX IF NOT EXISTS uk_journal_entry_reference
    ON journal_entry (reference);

-- Status index for operational queries
CREATE INDEX IF NOT EXISTS idx_journal_entry_status
    ON journal_entry (status);

-- Source system index for audit queries
CREATE INDEX IF NOT EXISTS idx_journal_entry_source
    ON journal_entry (source_system);

-- =============================================================================
-- Journal entry lines (double-entry accounting lines)
-- Each journal entry has at least one debit and one credit line.
-- Sum of debits must equal sum of credits (enforced by application logic).
-- =============================================================================
CREATE TABLE IF NOT EXISTS journal_entry_line (
    id               BIGSERIAL       PRIMARY KEY,
    journal_entry_id BIGINT          NOT NULL REFERENCES journal_entry(id) ON DELETE CASCADE,
    line_type        VARCHAR(10)     NOT NULL,
    account_code     VARCHAR(100)    NOT NULL,
    account_name     VARCHAR(200)    NOT NULL,
    amount           NUMERIC(19, 4)  NOT NULL CHECK (amount > 0),
    description      VARCHAR(300),

    CONSTRAINT chk_line_type CHECK (line_type IN ('DEBIT', 'CREDIT'))
);

-- Index for journal entry line lookup
CREATE INDEX IF NOT EXISTS idx_journal_line_entry_id
    ON journal_entry_line (journal_entry_id);

-- Index for account-based queries (e.g., "show all SALARY_EXPENSE entries")
CREATE INDEX IF NOT EXISTS idx_journal_line_account_code
    ON journal_entry_line (account_code);

COMMENT ON TABLE journal_entry IS 'Double-entry accounting journal entries. Each entry must have balanced debits and credits.';
COMMENT ON COLUMN journal_entry.reference IS 'Unique business reference used as idempotency key. E.g., PAY-2026-08-0001. UNIQUE constraint prevents duplicate entries.';
COMMENT ON COLUMN journal_entry.total_amount IS 'Total transaction amount. NUMERIC(19,4) for exact decimal precision. Equal to sum of all debit lines (= sum of all credit lines).';
COMMENT ON TABLE journal_entry_line IS 'Individual debit/credit lines within a journal entry. Enables multi-line entries for complex transactions.';
