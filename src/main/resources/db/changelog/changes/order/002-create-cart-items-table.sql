--liquibase formatted sql
--changeset lamahafiz:002-create-cart-items-table

CREATE SEQUENCE IF NOT EXISTS orders.cart_items_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE IF NOT EXISTS orders.cart_items (
    cart_item_id      BIGINT PRIMARY KEY DEFAULT nextval('orders.cart_items_seq'),
    cart_id           BIGINT NOT NULL,
    product_id        BIGINT NOT NULL,
    product_name      VARCHAR(255) NOT NULL,
    variant_id        BIGINT NOT NULL,
    variant_name      VARCHAR(255) NOT NULL,
    store_id          BIGINT NOT NULL,
    mall_id           BIGINT NOT NULL,
    base_price        NUMERIC(10,2) NOT NULL,
    discounted_price  NUMERIC(10,2),
    quantity          INT NOT NULL,
    offer_id          BIGINT,
    created_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by        VARCHAR(150),
    updated_at        TIMESTAMP,
    updated_by        VARCHAR(150),

    CONSTRAINT fk_cart_item_cart FOREIGN KEY (cart_id)
        REFERENCES orders.carts (cart_id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_cart_item_cart ON orders.cart_items (cart_id);
CREATE INDEX IF NOT EXISTS idx_cart_item_variant ON orders.cart_items (cart_id, variant_id);