-- V1: Create Produce Catalogue table and seed initial produce (Rambutan)
CREATE TABLE IF NOT EXISTS produce (
    id VARCHAR(36) PRIMARY KEY,
    code VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    scientific_name VARCHAR(150),
    category VARCHAR(50) NOT NULL,
    description TEXT,
    default_unit VARCHAR(20) NOT NULL,
    supported_units VARCHAR(200) NOT NULL,
    supported_grades VARCHAR(200) NOT NULL,
    image_url VARCHAR(500),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_produce_code ON produce(code);
CREATE INDEX idx_produce_is_active ON produce(is_active);

-- MARKET-001: Seed initial produce — Rambutan
INSERT INTO produce (
    id,
    code,
    name,
    scientific_name,
    category,
    description,
    default_unit,
    supported_units,
    supported_grades,
    image_url,
    is_active,
    created_at,
    updated_at
) VALUES (
    'a1b2c3d4-e5f6-4a5b-8c9d-0e1f2a3b4c5d',
    'RAMBUTAN',
    'Rambutan',
    'Nephelium lappaceum',
    'FRUIT',
    'Fresh Sri Lankan tropical Rambutan, specially cultivated in Malwana and surrounding river valleys. Known for its vivid red spiky rind, translucent juicy flesh, and sweet balanced flavour.',
    'KG',
    'KG,CRATE',
    'PREMIUM,STANDARD,PROCESSING',
    'https://storage.trueyield.lk/catalogue/rambutan.jpg',
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;
