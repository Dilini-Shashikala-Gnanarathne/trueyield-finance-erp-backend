-- V1: Lookup tables (districts and cities)
CREATE TABLE usr_district (
    id   SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL
);

CREATE TABLE usr_city (
    id          SERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    zipcode     VARCHAR(20),
    district_id INT NOT NULL REFERENCES usr_district(id) ON DELETE RESTRICT
);

-- Seed some initial districts/cities for testing
INSERT INTO usr_district (name) VALUES ('Colombo'), ('Gampaha'), ('Kandy'), ('Galle'), ('Matara');
INSERT INTO usr_city (name, zipcode, district_id) VALUES
    ('Colombo 1', '00100', 1), ('Colombo 7', '00700', 1),
    ('Gampaha', '11000', 2), ('Negombo', '11500', 2),
    ('Kandy', '20000', 3), ('Peradeniya', '20400', 3),
    ('Galle', '80000', 4), ('Hikkaduwa', '80240', 4);
