-- =============================================================================
-- TrueYield Auth Service - Initial Schema
-- Supports:
--   AUTH-001: Farmer Registration
--   AUTH-002: Buyer Registration
--   AUTH-003: Login & Token Management
--   AUTH-004: Role-Based Access Control (FARMER, BUYER, ADMIN)
--   AUTH-005: Farmer & Buyer Profile Management (with seller location privacy)
-- =============================================================================

CREATE TABLE IF NOT EXISTS usr_user (
    id                     VARCHAR(36)  PRIMARY KEY,
    phone                  VARCHAR(20)  NOT NULL,
    email                  VARCHAR(100),
    password               VARCHAR(255) NOT NULL,
    full_name              VARCHAR(100) NOT NULL,
    role                   VARCHAR(20)  NOT NULL
        CONSTRAINT chk_usr_user_role CHECK (role IN ('FARMER', 'BUYER', 'ADMIN')),
    status                 VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE'
        CONSTRAINT chk_usr_user_status CHECK (status IN ('ACTIVE', 'DEACTIVE', 'SUSPENDED', 'NOT_VERIFIED', 'DELETED')),
    registered_at          TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    registered_by          VARCHAR(36),
    updated_at             TIMESTAMPTZ           DEFAULT CURRENT_TIMESTAMP,
    updated_by             VARCHAR(36),
    deleted_at             TIMESTAMPTZ,
    deleted_by             VARCHAR(36)
);

-- Partial unique constraints for active accounts
CREATE UNIQUE INDEX IF NOT EXISTS uq_usr_user_phone_active ON usr_user(phone)
    WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uq_usr_user_email_active ON usr_user(email)
    WHERE deleted_at IS NULL AND email IS NOT NULL;

-- Performance indexes
CREATE INDEX IF NOT EXISTS idx_usr_user_role       ON usr_user(role);
CREATE INDEX IF NOT EXISTS idx_usr_user_status     ON usr_user(status);
CREATE INDEX IF NOT EXISTS idx_usr_user_deleted_at ON usr_user(deleted_at);

-- =============================================================================
-- Farmer Profile (Marketplace Seller Location & Details)
-- =============================================================================
CREATE TABLE IF NOT EXISTS usr_farmer_profile (
    id                  VARCHAR(36)     PRIMARY KEY,
    user_id             VARCHAR(36)     NOT NULL UNIQUE REFERENCES usr_user(id) ON DELETE CASCADE,
    farm_name           VARCHAR(150),
    bio                 TEXT,
    latitude            NUMERIC(10, 7),
    longitude           NUMERIC(10, 7),
    locality            VARCHAR(100),
    district            VARCHAR(100),
    location_visibility VARCHAR(30)     NOT NULL DEFAULT 'APPROXIMATE'
        CONSTRAINT chk_farmer_loc_visibility CHECK (location_visibility IN ('APPROXIMATE', 'EXACT_AFTER_ORDER')),
    location_updated_at TIMESTAMPTZ,
    avatar_url          TEXT,
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMPTZ              DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_farmer_locality ON usr_farmer_profile(locality);
CREATE INDEX IF NOT EXISTS idx_farmer_district ON usr_farmer_profile(district);

-- =============================================================================
-- Buyer Profile
-- =============================================================================
CREATE TABLE IF NOT EXISTS usr_buyer_profile (
    id                       VARCHAR(36) PRIMARY KEY,
    user_id                  VARCHAR(36) NOT NULL UNIQUE REFERENCES usr_user(id) ON DELETE CASCADE,
    delivery_address         VARCHAR(255),
    locality                 VARCHAR(100),
    district                 VARCHAR(100),
    preferred_contact_method VARCHAR(30) DEFAULT 'PHONE',
    avatar_url               TEXT,
    created_at               TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at               TIMESTAMPTZ          DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_buyer_locality ON usr_buyer_profile(locality);
CREATE INDEX IF NOT EXISTS idx_buyer_district ON usr_buyer_profile(district);

-- Seed default Administrator account:
-- Mobile: 0770000000 | Email: admin@trueyield.com | Password: AdminPassword123!
-- BCrypt hash (strength 12) for 'AdminPassword123!': $2a$12$e0MYzXy4Yw3UOz9fHh50c.l25i58Z2z5hX71Lp6F0Q1dFw0Zz3W0q
INSERT INTO usr_user (
    id, phone, email, password, full_name, role, status, registered_at, registered_by
) VALUES (
    'admin-00000000-0000-0000-0000-000000000001',
    '0770000000',
    'admin@trueyield.com',
    '$2a$12$K1qgYwL12Fk1.zD3vK75ue0yK7JbWbsp5M5G5v8G/N52Vz6510j9.',
    'System Administrator',
    'ADMIN',
    'ACTIVE',
    CURRENT_TIMESTAMP,
    'SYSTEM'
) ON CONFLICT DO NOTHING;
