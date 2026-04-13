--liquibase formatted sql
--changeset lamahafiz:004-create-cart-items-table-audit

CREATE TABLE IF NOT EXISTS audit.cart_items_audit (
    rev              INT NOT NULL,
    revtype          SMALLINT,
    cart_item_id     BIGINT NOT NULL,
    cart_id          BIGINT,
    product_id       BIGINT,
    product_name     VARCHAR(255),
    variant_id       BIGINT,
    variant_name     VARCHAR(255),
    store_id         BIGINT,
    mall_id          BIGINT,
    base_price       NUMERIC(10,2),
    discounted_price NUMERIC(10,2),
    quantity         INT,
    offer_id         BIGINT,
    created_at       TIMESTAMP,
    created_by       VARCHAR(150),
    updated_at       TIMESTAMP,
    updated_by       VARCHAR(150),
    PRIMARY KEY (cart_item_id, rev),
    CONSTRAINT fk_cart_item_audit_rev
    FOREIGN KEY (rev)
    REFERENCES audit.revinfo (rev)
);