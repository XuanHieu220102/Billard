ALTER TABLE session_orders DROP CONSTRAINT chk_session_orders_item_type;

ALTER TABLE session_orders
    ADD CONSTRAINT chk_session_orders_item_type CHECK (item_type IN ('FOOD', 'DRINK', 'SERVICE'));
