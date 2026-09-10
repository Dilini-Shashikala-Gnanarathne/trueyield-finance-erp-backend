-- =============================================================================
-- V1__create_payroll_table.sql
--
-- Flyway migration: creates the payroll table.
--
-- Design notes:
-- - total_amount uses NUMERIC(19,4) — the SQL standard for precise decimals.
--   This is the PostgreSQL equivalent of Java's BigDecimal. Using FLOAT or
--   DOUBLE PRECISION would introduce IEEE 754 rounding errors in financial data.
-- - payroll_reference has a UNIQUE constraint to enforce idempotency at the DB level.
--   Even if the application layer fails to catch a duplicate, the database will reject it.
-- - status uses VARCHAR instead of an ENUM type to allow adding new statuses
--   without a DDL migration.
-- =============================================================================

CREATE TABLE IF NOT EXISTS payroll (
    id                BIGSERIAL       PRIMARY KEY,
    payroll_reference VARCHAR(50)     NOT NULL UNIQUE,
    payroll_period    VARCHAR(7)      NOT NULL,
    employee_count    INTEGER         NOT NULL CHECK (employee_count > 0),
    total_amount      NUMERIC(19, 4)  NOT NULL CHECK (total_amount >= 0),
    status            VARCHAR(20)     NOT NULL DEFAULT 'PENDING',
    journal_reference VARCHAR(100),
    finance_transport VARCHAR(10),
    created_at        TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_payroll_status CHECK (status IN ('PENDING', 'PROCESSING', 'PROCESSED', 'FAILED'))
);

-- Index for quick lookup by reference (used in idempotency checks)
CREATE UNIQUE INDEX IF NOT EXISTS uk_payroll_reference ON payroll (payroll_reference);

-- Index for period-based queries (used in duplicate period detection)
CREATE INDEX IF NOT EXISTS idx_payroll_period ON payroll (payroll_period);

-- Index for status-based monitoring queries
CREATE INDEX IF NOT EXISTS idx_payroll_status ON payroll (status);

COMMENT ON TABLE payroll IS 'Payroll processing records. Each row represents one payroll run for a given period.';
COMMENT ON COLUMN payroll.payroll_reference IS 'Unique business reference. Format: PAY-YYYY-MM-NNNN. Used as idempotency key with Finance Service.';
COMMENT ON COLUMN payroll.total_amount IS 'Total gross payroll amount. NUMERIC(19,4) preserves exact decimal precision. Never use FLOAT for financial data.';
COMMENT ON COLUMN payroll.journal_reference IS 'Reference to the journal entry created in Finance Service. Null until Finance Service responds.';
COMMENT ON COLUMN payroll.finance_transport IS 'Transport mechanism used (GRPC or REST). Populated for benchmark comparison.';
