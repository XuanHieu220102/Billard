CREATE TABLE table_sessions (
    id                      UUID PRIMARY KEY,
    shop_id                 UUID NOT NULL REFERENCES shops(id),
    table_id                UUID NOT NULL REFERENCES tables(id),
    start_time              TIMESTAMPTZ NOT NULL,
    end_time                TIMESTAMPTZ,
    price_per_hour_snapshot NUMERIC(19, 2) NOT NULL,
    status                  VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at              TIMESTAMPTZ NOT NULL,
    updated_at              TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_table_sessions_shop_id ON table_sessions(shop_id);
CREATE INDEX idx_table_sessions_table_id ON table_sessions(table_id);
CREATE INDEX idx_table_sessions_shop_id_status ON table_sessions(shop_id, status);

-- Ensure only one ACTIVE session per table at a time
CREATE UNIQUE INDEX uq_table_sessions_active_per_table
    ON table_sessions(table_id)
    WHERE status = 'ACTIVE';
