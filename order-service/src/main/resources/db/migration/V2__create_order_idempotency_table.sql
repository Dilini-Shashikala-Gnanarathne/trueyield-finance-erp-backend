-- V2: Create Order Idempotency table (ORDER-005)
CREATE TABLE IF NOT EXISTS order_idempotency (
    idempotency_key VARCHAR(100) PRIMARY KEY,
    buyer_id VARCHAR(36) NOT NULL,
    order_id VARCHAR(36),
    request_hash VARCHAR(64) NOT NULL,
    response_payload TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'PROCESSING',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_order_idempotency_buyer ON order_idempotency(buyer_id);
