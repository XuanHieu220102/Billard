CREATE TABLE food_items (
    id              UUID PRIMARY KEY,
    shop_id         UUID NOT NULL REFERENCES shops(id),
    name            VARCHAR(255) NOT NULL,
    price           NUMERIC(19, 2) NOT NULL,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ NOT NULL,
    updated_at      TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_food_items_shop_id ON food_items(shop_id);
CREATE INDEX idx_food_items_shop_id_is_active ON food_items(shop_id, is_active);

CREATE TABLE drink_items (
    id              UUID PRIMARY KEY,
    shop_id         UUID NOT NULL REFERENCES shops(id),
    name            VARCHAR(255) NOT NULL,
    price           NUMERIC(19, 2) NOT NULL,
    stock_quantity  INTEGER NOT NULL DEFAULT 0,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ NOT NULL,
    updated_at      TIMESTAMPTZ NOT NULL,
    CONSTRAINT chk_drink_items_stock_non_negative CHECK (stock_quantity >= 0)
);

CREATE INDEX idx_drink_items_shop_id ON drink_items(shop_id);
CREATE INDEX idx_drink_items_shop_id_is_active ON drink_items(shop_id, is_active);

CREATE TABLE drink_stock_entries (
    id              UUID PRIMARY KEY,
    shop_id         UUID NOT NULL REFERENCES shops(id),
    drink_item_id   UUID NOT NULL REFERENCES drink_items(id),
    quantity_added  INTEGER NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL,
    created_by      UUID,
    CONSTRAINT chk_drink_stock_entries_quantity_positive CHECK (quantity_added > 0)
);

CREATE INDEX idx_drink_stock_entries_shop_id ON drink_stock_entries(shop_id);
CREATE INDEX idx_drink_stock_entries_drink_item_id ON drink_stock_entries(drink_item_id);
