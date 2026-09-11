-- V7: User address (UPSERT-able, one-to-one)
CREATE TABLE usr_address (
    user_id    VARCHAR(36) PRIMARY KEY REFERENCES usr_user(id) ON DELETE CASCADE,
    line1      VARCHAR(50) NOT NULL,
    line2      VARCHAR(50),
    city_id    INT         REFERENCES usr_city(id) ON DELETE RESTRICT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(36),
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(36)
);
