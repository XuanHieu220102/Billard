CREATE TABLE invoices (
    id                              UUID PRIMARY KEY,
    shop_id                         UUID NOT NULL REFERENCES shops(id),
    table_session_id                UUID NOT NULL REFERENCES table_sessions(id),
    table_amount                    NUMERIC(19, 2) NOT NULL,
    discount_percent                INTEGER NOT NULL DEFAULT 0,
    table_amount_after_discount     NUMERIC(19, 2) NOT NULL,
    food_drink_amount               NUMERIC(19, 2) NOT NULL DEFAULT 0,
    total_amount                    NUMERIC(19, 2) NOT NULL,
    status                          VARCHAR(20) NOT NULL DEFAULT 'UNPAID',
    created_at                      TIMESTAMPTZ NOT NULL,
    updated_at                      TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_invoices_table_session_id UNIQUE (table_session_id),
    CONSTRAINT chk_invoices_discount_percent CHECK (discount_percent IN (0, 10, 30, 50))
);

CREATE INDEX idx_invoices_shop_id ON invoices(shop_id);
CREATE INDEX idx_invoices_shop_id_status ON invoices(shop_id, status);

CREATE TABLE payments (
    id              UUID PRIMARY KEY,
    shop_id         UUID NOT NULL REFERENCES shops(id),
    invoice_id      UUID NOT NULL REFERENCES invoices(id),
    amount          NUMERIC(19, 2) NOT NULL,
    payment_method  VARCHAR(30) NOT NULL,
    paid_at         TIMESTAMPTZ NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_payments_shop_id ON payments(shop_id);
CREATE INDEX idx_payments_invoice_id ON payments(invoice_id);
