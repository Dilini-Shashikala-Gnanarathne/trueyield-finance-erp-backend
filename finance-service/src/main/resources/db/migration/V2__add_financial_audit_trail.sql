-- =============================================================================
-- V2__add_financial_audit_trail.sql
--
-- Flyway migration: Cryptographically verifiable audit trail for journal entries.
-- Implements SHA-256 hash chaining to ensure tamper evidence.
-- =============================================================================

CREATE TABLE IF NOT EXISTS journal_entry_audit (
    id            BIGSERIAL       PRIMARY KEY,
    reference     VARCHAR(100)    NOT NULL,
    action        VARCHAR(50)     NOT NULL,
    entry_hash    VARCHAR(64)     NOT NULL,
    previous_hash VARCHAR(64)     NOT NULL,
    performed_by  VARCHAR(100)    NOT NULL DEFAULT 'SYSTEM',
    timestamp     TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_journal_audit_ref ON journal_entry_audit(reference);
CREATE INDEX IF NOT EXISTS idx_journal_audit_timestamp ON journal_entry_audit(timestamp);
