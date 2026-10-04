-- =============================================================================
-- V3__add_journal_party_references.sql
--
-- Adds order/farmer/buyer references to journal entries so that per-party
-- statements (farmer earnings, buyer spending) can be queried efficiently.
-- Columns are nullable: non-order entries (manual/payroll) have no parties.
-- =============================================================================

ALTER TABLE journal_entry ADD COLUMN IF NOT EXISTS order_id  VARCHAR(36);
ALTER TABLE journal_entry ADD COLUMN IF NOT EXISTS farmer_id VARCHAR(36);
ALTER TABLE journal_entry ADD COLUMN IF NOT EXISTS buyer_id  VARCHAR(36);

CREATE INDEX IF NOT EXISTS idx_journal_entry_order  ON journal_entry (order_id);
CREATE INDEX IF NOT EXISTS idx_journal_entry_farmer ON journal_entry (farmer_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_journal_entry_buyer  ON journal_entry (buyer_id, created_at DESC);
