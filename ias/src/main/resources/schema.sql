CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE IF NOT EXISTS cuentas (
    id UUID PRIMARY KEY,
    numero VARCHAR(40) NOT NULL UNIQUE,
    condicion VARCHAR(100) NOT NULL,
    limite_diario NUMERIC(19, 2) NOT NULL,
    moneda VARCHAR(10) NOT NULL,
    saldo NUMERIC(19, 2) NOT NULL
);

CREATE TABLE IF NOT EXISTS transferencias (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    request_reference VARCHAR(255) NOT NULL UNIQUE,
    source_account_id VARCHAR(40) NOT NULL,
    destination_account_id VARCHAR(40) NOT NULL,
    amount NUMERIC(19, 2) NOT NULL,
    currency VARCHAR(10) NOT NULL,
    estado VARCHAR(30) NOT NULL,
    resultado VARCHAR(200),
    observacion VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_transferencia_origen FOREIGN KEY (source_account_id) REFERENCES cuentas(numero),
    CONSTRAINT fk_transferencia_destino FOREIGN KEY (destination_account_id) REFERENCES cuentas(numero)
);

CREATE TABLE IF NOT EXISTS outbox_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    request_reference VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    attempts INTEGER NOT NULL DEFAULT 0,
    available_at TIMESTAMP WITH TIME ZONE NOT NULL,
    published_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uq_outbox_request_reference UNIQUE (request_reference),
    CONSTRAINT fk_outbox_transferencia FOREIGN KEY (request_reference)
        REFERENCES transferencias(request_reference)
);

CREATE INDEX IF NOT EXISTS idx_outbox_pending ON outbox_events(status, available_at, created_at);

ALTER TABLE transferencias ALTER COLUMN id SET DEFAULT gen_random_uuid();

CREATE INDEX IF NOT EXISTS idx_transferencias_cuenta_origen ON transferencias(source_account_id);
CREATE INDEX IF NOT EXISTS idx_transferencias_cuenta_destino ON transferencias(destination_account_id);
CREATE INDEX IF NOT EXISTS idx_transferencias_acumulado ON transferencias(source_account_id, estado, created_at);
