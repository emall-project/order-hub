--liquibase formatted sql
--changeset lamahafiz:005-create-shop-orders-table-audit

CREATE TABLE IF NOT EXISTS audit.shop_orders_audit (
    rev            INT NOT NULL,
    revtype        SMALLINT,
    shop_order_id  BIGINT NOT NULL,
    cart_id        BIGINT,
    shop_id        BIGINT,
    mall_id        BIGINT,
    customer_id    BIGINT,
    total          NUMERIC(10,2),
    status         VARCHAR(30),
    created_at     TIMESTAMP,
    created_by     VARCHAR(150),
    updated_at     TIMESTAMP,
    updated_by     VARCHAR(150),
    PRIMARY KEY (shop_order_id, rev),
    CONSTRAINT fk_shop_order_audit_rev
    FOREIGN KEY (rev)
    REFERENCES audit.revinfo (rev)
);