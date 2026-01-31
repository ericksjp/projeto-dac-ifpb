-- Tabela de cobranças
CREATE TABLE charges (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    
    -- Relacionamento com cliente
    customer_id UUID NOT NULL REFERENCES customers(id) ON DELETE CASCADE,
    
    -- IDs externos do Asaas
    external_id VARCHAR(100) UNIQUE,
    installment_id VARCHAR(100),
    
    -- Dados da cobrança
    billing_type VARCHAR(20) NOT NULL,
    value DECIMAL(10, 2) NOT NULL,
    due_date DATE NOT NULL,
    description TEXT NOT NULL,
    
    -- Status da cobrança
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    
    -- URLs de pagamento
    invoice_url TEXT,
    bank_slip_url TEXT,
    pix_qr_code TEXT,
    
    -- Parcelamento
    installment_count INTEGER,
    installment_number INTEGER,
    
    -- Auditoria
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    cancelled_at TIMESTAMP
);

-- Índices
CREATE INDEX idx_charges_customer_id ON charges (customer_id);
CREATE INDEX idx_charges_external_id ON charges (external_id);
CREATE INDEX idx_charges_status ON charges (status);
CREATE INDEX idx_charges_due_date ON charges (due_date);
CREATE INDEX idx_charges_installment_id ON charges (installment_id);
