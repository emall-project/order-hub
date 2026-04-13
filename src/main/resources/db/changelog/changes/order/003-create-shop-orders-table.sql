--liquibase formatted sql
--changeset lamahafiz:003-create-shop-orders-table

CREATE SEQUENCE IF NOT EXISTS orders.shop_orders_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE IF NOT EXISTS orders.shop_orders (
    shop_order_id     BIGINT PRIMARY KEY DEFAULT nextval('orders.shop_orders_seq'),
    cart_id           BIGINT NOT NULL,
    shop_id           BIGINT NOT NULL,
    mall_id           BIGINT NOT NULL,
    customer_id       BIGINT NOT NULL,
    total             NUMERIC(10,2) NOT NULL,
    status            VARCHAR(30) NOT NULL DEFAULT 'NEW',
    created_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by        VARCHAR(150),
    updated_at        TIMESTAMP,
    updated_by        VARCHAR(150)
);

CREATE INDEX IF NOT EXISTS idx_shop_order_cart ON orders.shop_orders (cart_id);
CREATE INDEX IF NOT EXISTS idx_shop_order_shop_status ON orders.shop_orders (shop_id, status);
CREATE INDEX IF NOT EXISTS idx_shop_order_customer ON orders.shop_orders (customer_id, status);