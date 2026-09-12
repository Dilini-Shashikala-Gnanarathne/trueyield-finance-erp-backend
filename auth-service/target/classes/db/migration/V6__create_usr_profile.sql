-- V6: User profile image (optional, one-to-one)
CREATE TABLE usr_profile (
    id         VARCHAR(36) PRIMARY KEY,
    user_id    VARCHAR(36) NOT NULL REFERENCES usr_user(id) ON DELETE CASCADE,
    url        TEXT        NOT NULL,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(36),
    CONSTRAINT uq_profile_user_id UNIQUE (user_id)
);
