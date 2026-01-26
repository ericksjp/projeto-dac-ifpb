-- cria coluna temporaria jsonb
ALTER TABLE payment_events
    ADD COLUMN payload_tmp jsonb;

-- converte somente registros validos
UPDATE payment_events
SET payload_tmp = payment_raw_payload::jsonb;

-- remove coluna antiga
ALTER TABLE payment_events
    DROP COLUMN payment_raw_payload;

-- renomeia coluna nova
ALTER TABLE payment_events
    RENAME COLUMN payload_tmp TO payload;

-- not null constraint na nova coluna
ALTER TABLE payment_events
    ALTER COLUMN payload SET NOT NULL;
