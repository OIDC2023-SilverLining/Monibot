CREATE TABLE IF NOT EXISTS alerts (
    id SERIAL PRIMARY KEY,
    type VARCHAR(255),
    metric VARCHAR(255),
    threshold VARCHAR(255),
    condition VARCHAR(255),
    duration VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS loki (
    label VARCHAR(255)
);
