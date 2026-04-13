--liquibase formatted sql
--changeset lamahafiz:006-create-order-items-table-audit

CREATE TABLE IF NOT EXISTS audit.order_items_audit (
    order_item_id       BIGINT NOT NULL,
    rev                 INT NOT NULL,
    revtype             SMALLINT,
    shop_order_id       BIGINT,
    shop_id             BIGINT,
    product_id          BIGINT,
    product_name        VARCHAR(255),
    variant_id          BIGINT,
    variant_name        VARCHAR(255),
    unit_price          NUMERIC(10,2),
    quantity            INT,
    status              VARCHAR(30),
    holding_expires_at  TIMESTAMP,
    has_return_request  BOOLEAN,
    created_at          TIMESTAMP,
    created_by          VARCHAR(150),
    updated_at          TIMESTAMP,
    updated_by          VARCHAR(150),
    PRIMARY KEY (order_item_id, rev),
    CONSTRAINT fk_order_item_audit_rev
    FOREIGN KEY (rev)
    REFERENCES audit.revinfo (rev)
);