--liquibase formatted sql
--changeset lamahafiz:007-create-deliveries-table-audit

CREATE TABLE IF NOT EXISTS audit.deliveries_audit (
    rev                 INT NOT NULL,
    revtype             SMALLINT,
    delivery_id         BIGINT NOT NULL,
    cart_id             BIGINT,
    delivery_company_id BIGINT,
    tracking_id         VARCHAR(100),
    status              VARCHAR(20),
    failure_reason      TEXT,
    sent_at             TIMESTAMP,
    delivered_at        TIMESTAMP,
    created_at          TIMESTAMP,
    created_by          VARCHAR(150),
    updated_at          TIMESTAMP,
    updated_by          VARCHAR(150),
    PRIMARY KEY (delivery_id, rev),
    CONSTRAINT fk_delivery_audit_rev
    FOREIGN KEY (rev)
    REFERENCES audit.revinfo (rev)
);