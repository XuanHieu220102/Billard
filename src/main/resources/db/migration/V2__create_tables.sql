CREATE TABLE tables (
    id              UUID PRIMARY KEY,
    shop_id         UUID NOT NULL REFERENCES shops(id),
    table_number    VARCHAR(50) NOT NULL,
    table_type      VARCHAR(50),
    price_per_hour  NUMERIC(19, 2) NOT NULL DEFAULT 40000,
    status          VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    created_at      TIMESTAMPTZ NOT NULL,
    updated_at      TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_tables_shop_id_table_number UNIQUE (shop_id, table_number)
);

CREATE INDEX idx_tables_shop_id ON tables(shop_id);
CREATE INDEX idx_tables_shop_id_status ON tables(shop_id, status);
