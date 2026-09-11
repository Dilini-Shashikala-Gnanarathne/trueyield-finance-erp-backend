-- V8: Password history pool (retain last 3 passwords to prevent reuse)
CREATE TABLE usr_password_pool (
    id         VARCHAR(36)  PRIMARY KEY,
    user_id    VARCHAR(36)  NOT NULL REFERENCES usr_user(id) ON DELETE CASCADE,
    password   VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Index for fast retrieval ordered by recency per user
CREATE INDEX idx_pwd_pool_user_created ON usr_password_pool(user_id, created_at DESC);
