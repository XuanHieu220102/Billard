-- Splits the "food_items" table between FOOD and SERVICE (e.g. cue rental, table
-- cleaning) items — both share the same shape (no stock tracking, flat price),
-- unlike DRINK items which live in their own table with stock_quantity.
ALTER TABLE food_items
    ADD COLUMN category VARCHAR(20) NOT NULL DEFAULT 'FOOD';

CREATE INDEX idx_food_items_shop_id_category ON food_items(shop_id, category);
