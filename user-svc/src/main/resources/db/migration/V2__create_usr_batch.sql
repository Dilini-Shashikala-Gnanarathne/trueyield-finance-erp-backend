-- V2: Core user table
CREATE TABLE usr_batch (
    id     VARCHAR(36) PRIMARY KEY,
    name   VARCHAR(100) NOT NULL,
    status VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE'
        CONSTRAINT chk_batch_status CHECK (status IN ('ACTIVE', 'DEACTIVE', 'DELETED')),
    CONSTRAINT uq_batch_name UNIQUE (name)
);

-- Default batch required for new student self-registration
INSERT INTO usr_batch (id, name, status) VALUES
    ('00000000-0000-0000-0000-000000000001', 'Default Batch', 'ACTIVE');
