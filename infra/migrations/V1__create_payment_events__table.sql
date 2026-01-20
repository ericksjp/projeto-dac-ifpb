CREATE TABLE payment_events (
    id UUID PRIMARY KEY,

    provider_event_id VARCHAR(100) NOT NULL,
    event_type VARCHAR(50) NOT NULL,

    payment_raw_payload varchar NOT NULL,

    processed BOOLEAN NOT NULL DEFAULT FALSE,

    received_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP,

    version INT NOT NULL DEFAULT 1
);

CREATE UNIQUE INDEX uq_payment_events_provider_event
    ON payment_events (provider_event_id);

CREATE INDEX idx_payment_events_processed
    ON payment_events (processed);
