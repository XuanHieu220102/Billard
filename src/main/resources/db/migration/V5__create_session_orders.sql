CREATE TABLE session_orders (
    id                  UUID PRIMARY KEY,
    shop_id             UUID NOT NULL REFERENCES shops(id),
    table_session_id    UUID NOT NULL REFERENCES table_sessions(id),
    item_type           VARCHAR(10) NOT NULL,
    item_id             UUID NOT NULL,
    item_name           VARCHAR(255) NOT NULL,
    unit_price          NUMERIC(19, 2) NOT NULL,
    quantity            INTEGER NOT NULL,
    line_total          NUMERIC(19, 2) NOT NULL,
    created_at          TIMESTAMPTZ NOT NULL,
    created_by          UUID,
    updated_at          TIMESTAMPTZ NOT NULL,
    CONSTRAINT chk_session_orders_item_type CHECK (item_type IN ('FOOD', 'DRINK')),
    CONSTRAINT chk_session_orders_quantity_positive CHECK (quantity > 0)
);

CREATE INDEX idx_session_orders_shop_id ON session_orders(shop_id);
CREATE INDEX idx_session_orders_table_session_id ON session_orders(table_session_id);
