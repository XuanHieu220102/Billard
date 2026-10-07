CREATE TABLE shops (
    id              UUID PRIMARY KEY,
    name            VARCHAR(255) NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL,
    updated_at      TIMESTAMPTZ NOT NULL
);

CREATE TABLE users (
    id              UUID PRIMARY KEY,
    shop_id         UUID NOT NULL REFERENCES shops(id),
    phone_number    VARCHAR(30) NOT NULL,
    password_hash   VARCHAR(255) NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL,
    updated_at      TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_users_phone_number UNIQUE (phone_number)
);

CREATE INDEX idx_users_shop_id ON users(shop_id);
