--liquibase formatted sql
--changeset lamahafiz:002-create-carts-table

CREATE SEQUENCE IF NOT EXISTS orders.carts_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE IF NOT EXISTS orders.carts (
    cart_id           BIGINT PRIMARY KEY DEFAULT nextval('orders.carts_seq'),
    mall_id           BIGINT NOT NULL,
    customer_id       BIGINT NOT NULL,
    city_id           BIGINT,
    delivery_fee      NUMERIC(10,2) NOT NULL DEFAULT 0,
    total_amount      NUMERIC(10,2) NOT NULL DEFAULT 0,
    delivery_name     VARCHAR(100),
    delivery_phone    VARCHAR(20),
    delivery_note     TEXT,
    delivery_location VARCHAR(255),
    status            VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by        VARCHAR(150),
    updated_at        TIMESTAMP,
    updated_by        VARCHAR(150)
);

CREATE INDEX IF NOT EXISTS idx_cart_customer_status ON orders.carts (customer_id, status);
CREATE INDEX IF NOT EXISTS idx_cart_mall_customer ON orders.carts (mall_id, customer_id);