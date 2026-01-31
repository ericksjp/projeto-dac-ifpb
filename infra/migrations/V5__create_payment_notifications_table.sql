-- Tabela de notificações de pagamento
CREATE TABLE payment_notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    
    -- Relacionamento com cobrança
    charge_id UUID REFERENCES charges(id) ON DELETE SET NULL,
    
    -- Dados do evento
    event_type VARCHAR(50) NOT NULL,
    external_event_id VARCHAR(100),
    
    -- Payload completo do evento (JSON)
    payload JSONB NOT NULL,
    
    -- Auditoria
    received_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed BOOLEAN NOT NULL DEFAULT FALSE,
    processed_at TIMESTAMP
);

-- Índices
CREATE INDEX idx_payment_notifications_charge_id ON payment_notifications (charge_id);
CREATE INDEX idx_payment_notifications_event_type ON payment_notifications (event_type);
CREATE INDEX idx_payment_notifications_processed ON payment_notifications (processed);
CREATE INDEX idx_payment_notifications_received_at ON payment_notifications (received_at);
