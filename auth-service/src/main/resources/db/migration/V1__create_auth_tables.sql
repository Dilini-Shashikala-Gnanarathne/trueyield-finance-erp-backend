-- =============================================================================
-- Migration: V1__create_auth_tables.sql
-- Description: Creates user and profile tables for Auth Service
-- =============================================================================

CREATE TABLE IF NOT EXISTS usr_user (
    id              VARCHAR(36)  NOT NULL,
    phone           VARCHAR(20)  NOT NULL,
    email           VARCHAR(100),
    password        VARCHAR(255) NOT NULL,
    full_name       VARCHAR(100) NOT NULL,
    role            VARCHAR(20)  NOT NULL,
    status          VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    registered_at   TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    registered_by   VARCHAR(36),
    updated_at      TIMESTAMPTZ,
    updated_by      VARCHAR(36),
    deleted_at      TIMESTAMPTZ,
    deleted_by      VARCHAR(36),
    CONSTRAINT pk_usr_user PRIMARY KEY (id),
    CONSTRAINT uq_usr_user_phone UNIQUE (phone)
);

CREATE INDEX IF NOT EXISTS idx_usr_user_phone ON usr_user (phone);
CREATE INDEX IF NOT EXISTS idx_usr_user_email ON usr_user (email);
CREATE INDEX IF NOT EXISTS idx_usr_user_role ON usr_user (role);
CREATE INDEX IF NOT EXISTS idx_usr_user_status ON usr_user (status);

CREATE TABLE IF NOT EXISTS usr_farmer_profile (
    id                  VARCHAR(36)    NOT NULL,
    user_id             VARCHAR(36)    NOT NULL,
    farm_name           VARCHAR(150),
    bio                 TEXT,
    latitude            NUMERIC(10, 7),
    longitude           NUMERIC(10, 7),
    locality            VARCHAR(100),
    district            VARCHAR(100),
    location_visibility VARCHAR(30)    NOT NULL DEFAULT 'APPROXIMATE',
    location_updated_at TIMESTAMPTZ,
    avatar_url          TEXT,
    created_at          TIMESTAMPTZ    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMPTZ,
    CONSTRAINT pk_usr_farmer_profile PRIMARY KEY (id),
    CONSTRAINT uq_usr_farmer_profile_user_id UNIQUE (user_id),
    CONSTRAINT fk_usr_farmer_profile_user FOREIGN KEY (user_id) REFERENCES usr_user (id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_usr_farmer_district ON usr_farmer_profile (district);

CREATE TABLE IF NOT EXISTS usr_buyer_profile (
    id                       VARCHAR(36)  NOT NULL,
    user_id                  VARCHAR(36)  NOT NULL,
    delivery_address         VARCHAR(255),
    locality                 VARCHAR(100),
    district                 VARCHAR(100),
    preferred_contact_method VARCHAR(30)  DEFAULT 'PHONE',
    avatar_url               TEXT,
    created_at               TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at               TIMESTAMPTZ,
    CONSTRAINT pk_usr_buyer_profile PRIMARY KEY (id),
    CONSTRAINT uq_usr_buyer_profile_user_id UNIQUE (user_id),
    CONSTRAINT fk_usr_buyer_profile_user FOREIGN KEY (user_id) REFERENCES usr_user (id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_usr_buyer_district ON usr_buyer_profile (district);
