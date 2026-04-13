--liquibase formatted sql
--changeset lamahafiz:005-create-deliveries-table

CREATE SEQUENCE IF NOT EXISTS orders.deliveries_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE IF NOT EXISTS orders.deliveries (
    delivery_id         BIGINT PRIMARY KEY DEFAULT nextval('orders.deliveries_seq'),
    cart_id             BIGINT NOT NULL,
    delivery_company_id BIGINT,
    tracking_id         VARCHAR(100),
    status              VARCHAR(20) NOT NULL DEFAULT 'CREATED',
    failure_reason      TEXT,
    sent_at             TIMESTAMP,
    delivered_at        TIMESTAMP,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by          VARCHAR(150),
    updated_at          TIMESTAMP,
    updated_by          VARCHAR(150)
);

CREATE INDEX IF NOT EXISTS idx_delivery_cart ON orders.deliveries (cart_id);
CREATE INDEX IF NOT EXISTS idx_delivery_status ON orders.deliveries (status);