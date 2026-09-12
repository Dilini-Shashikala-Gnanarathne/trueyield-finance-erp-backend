-- V5: Admin-specific details (one-to-one with usr_user)
CREATE TABLE usr_admin (
    user_id    VARCHAR(36) PRIMARY KEY REFERENCES usr_user(id) ON DELETE CASCADE,
    fname      VARCHAR(20) NOT NULL,
    lname      VARCHAR(20) NOT NULL,
    role       VARCHAR(30) NOT NULL
        CONSTRAINT chk_admin_role CHECK (role IN ('SUPER_ADMIN', 'SUB_ADMIN')),
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(36)
);
