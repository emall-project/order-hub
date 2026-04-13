--liquibase formatted sql
--changeset lamahafiz:003-create-carts-table-audit

CREATE TABLE IF NOT EXISTS audit.carts_audit (
    rev             INT NOT NULL,
    revtype         SMALLINT,
    cart_id         BIGINT NOT NULL,
    mall_id         BIGINT,
    customer_id     BIGINT,
    city_id         BIGINT,
    delivery_fee    NUMERIC(10,2),
    total_amount    NUMERIC(10,2),
    delivery_name   VARCHAR(100),
    delivery_phone  VARCHAR(20),
    delivery_note   TEXT,
    delivery_location VARCHAR(255),
    status          VARCHAR(20),
    created_at      TIMESTAMP,
    created_by      VARCHAR(150),
    updated_at      TIMESTAMP,
    updated_by      VARCHAR(150),
    PRIMARY KEY (cart_id, rev),
    CONSTRAINT fk_cart_audit_rev
    FOREIGN KEY (rev)
    REFERENCES audit.revinfo (rev)
);
