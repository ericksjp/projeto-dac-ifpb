-- Tabela de clientes
CREATE TABLE customers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    
    -- ID externo do cliente no Asaas
    external_id VARCHAR(100) UNIQUE,
    
    -- Dados do cliente
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    cpf_cnpj VARCHAR(18) NOT NULL UNIQUE,
    
    -- Auditoria
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Índices
CREATE INDEX idx_customers_external_id ON customers (external_id);
CREATE INDEX idx_customers_cpf_cnpj ON customers (cpf_cnpj);
CREATE INDEX idx_customers_email ON customers (email);
