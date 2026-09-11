-- V3: Core user table with audit trail and soft-delete
CREATE TABLE usr_user (
    id                     VARCHAR(36)  PRIMARY KEY,
    username               VARCHAR(100) NOT NULL,
    password               VARCHAR(255) NOT NULL,
    mobile                 VARCHAR(15)  NOT NULL,
    nic                    VARCHAR(20),
    email                  VARCHAR(100),
    user_type              VARCHAR(20)  NOT NULL
        CONSTRAINT chk_user_type CHECK (user_type IN ('ADMIN', 'STUDENT')),
    status                 VARCHAR(20)  NOT NULL DEFAULT 'NOT_VERIFIED'
        CONSTRAINT chk_user_status CHECK (status IN ('ACTIVE', 'DEACTIVE', 'DELETED', 'NOT_VERIFIED', 'SUSPENDED')),
    force_password_reset   BOOLEAN      NOT NULL DEFAULT FALSE,
    registered_at          TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    registered_by          VARCHAR(36),
    updated_at             TIMESTAMPTZ           DEFAULT CURRENT_TIMESTAMP,
    updated_by             VARCHAR(36),
    last_password_reset_at TIMESTAMPTZ,
    deleted_at             TIMESTAMPTZ,
    deleted_by             VARCHAR(36),
    CONSTRAINT uq_usr_user_username UNIQUE (username)
);

-- Partial unique indexes: enforce uniqueness only on non-deleted records
CREATE UNIQUE INDEX uq_usr_user_mobile_active ON usr_user(mobile)
    WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX uq_usr_user_nic_active ON usr_user(nic)
    WHERE deleted_at IS NULL AND nic IS NOT NULL;

-- Performance indexes
CREATE INDEX idx_usr_user_status     ON usr_user(status);
CREATE INDEX idx_usr_user_user_type  ON usr_user(user_type);
CREATE INDEX idx_usr_user_deleted_at ON usr_user(deleted_at);
