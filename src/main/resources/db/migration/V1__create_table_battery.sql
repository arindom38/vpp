CREATE TABLE IF NOT EXISTS battery (
    id  BIGINT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    postcode INT NOT NULL,
    watt_capacity BIGINT NOT NULL,
    created_at  TIMESTAMP NOT NULL,
    modified_at TIMESTAMP NOT NULL
);
