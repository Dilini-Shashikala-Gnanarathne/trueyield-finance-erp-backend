-- V2: Create Listings table with agricultural & location domain model
CREATE TABLE IF NOT EXISTS listings (
    id VARCHAR(36) PRIMARY KEY,
    farmer_id VARCHAR(36) NOT NULL,
    produce_id VARCHAR(36) NOT NULL REFERENCES produce(id),
    title VARCHAR(200) NOT NULL,
    description TEXT,
    total_quantity NUMERIC(12, 2) NOT NULL,
    available_quantity NUMERIC(12, 2) NOT NULL,
    reserved_quantity NUMERIC(12, 2) NOT NULL DEFAULT 0.00,
    unit VARCHAR(20) NOT NULL,
    price_per_unit NUMERIC(12, 2) NOT NULL,
    quality_grade VARCHAR(50) NOT NULL,
    harvest_date DATE NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    min_order_quantity NUMERIC(12, 2),
    latitude NUMERIC(10, 7) NOT NULL,
    longitude NUMERIC(10, 7) NOT NULL,
    locality VARCHAR(150) NOT NULL,
    district VARCHAR(100),
    location_visibility VARCHAR(30) NOT NULL DEFAULT 'APPROXIMATE',
    cancellation_reason TEXT,
    published_at TIMESTAMPTZ,
    cancelled_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_listings_farmer_id ON listings(farmer_id);
CREATE INDEX idx_listings_produce_id ON listings(produce_id);
CREATE INDEX idx_listings_status ON listings(status);
CREATE INDEX idx_listings_harvest_date ON listings(harvest_date);
CREATE INDEX idx_listings_locality ON listings(locality);
