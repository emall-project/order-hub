--liquibase formatted sql
--changeset lamahafiz:006-create-return-requests-table

CREATE SEQUENCE IF NOT EXISTS orders.return_requests_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE IF NOT EXISTS orders.return_requests (
    return_request_id   BIGINT PRIMARY KEY DEFAULT nextval('orders.return_requests_seq'),
    order_item_id       BIGINT NOT NULL,
    customer_id         BIGINT NOT NULL,
    shop_id             BIGINT NOT NULL,
    reason              TEXT NOT NULL,
    description         TEXT,
    status              VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    rejection_reason    TEXT,
    image_uuid          UUID,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by          VARCHAR(150),
    updated_at          TIMESTAMP,
    updated_by          VARCHAR(150),

    CONSTRAINT uk_return_order_item UNIQUE (order_item_id),
    CONSTRAINT fk_return_order_item FOREIGN KEY (order_item_id)
    REFERENCES orders.order_items (order_item_id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_return_order_item ON orders.return_requests (order_item_id);
CREATE INDEX IF NOT EXISTS idx_return_shop_status ON orders.return_requests (shop_id, status);
CREATE INDEX IF NOT EXISTS idx_return_customer ON orders.return_requests (customer_id);