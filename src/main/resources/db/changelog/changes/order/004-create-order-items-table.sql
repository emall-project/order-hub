--liquibase formatted sql
--changeset lamahafiz:004-create-order-items-table

CREATE SEQUENCE IF NOT EXISTS orders.order_items_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE IF NOT EXISTS orders.order_items (
    order_item_id       BIGINT PRIMARY KEY DEFAULT nextval('orders.order_items_seq'),
    shop_order_id       BIGINT NOT NULL,
    shop_id             BIGINT NOT NULL,
    product_id          BIGINT NOT NULL,
    product_name        VARCHAR(255) NOT NULL,
    variant_id          BIGINT NOT NULL,
    variant_name        VARCHAR(255) NOT NULL,
    unit_price          NUMERIC(10,2) NOT NULL,
    quantity            INT NOT NULL,
    status              VARCHAR(30) NOT NULL DEFAULT 'CREATED',
    holding_expires_at  TIMESTAMP,
    has_return_request  BOOLEAN NOT NULL DEFAULT FALSE,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by          VARCHAR(150),
    updated_at          TIMESTAMP,
    updated_by          VARCHAR(150),

    CONSTRAINT fk_order_item_shop_order FOREIGN KEY (shop_order_id)
        REFERENCES orders.shop_orders (shop_order_id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_order_item_shop_order ON orders.order_items (shop_order_id);
CREATE INDEX IF NOT EXISTS idx_order_item_status ON orders.order_items (status);
CREATE INDEX IF NOT EXISTS idx_order_item_holding ON orders.order_items (status, holding_expires_at);