CREATE TABLE IF NOT EXISTS battery (
    id  bigserial PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    postcode INT NOT NULL,
    watt_capacity BIGINT NOT NULL,
    created_at  timestamp with time zone NOT NULL,
    modified_at timestamp with time zone NOT NULL
);
