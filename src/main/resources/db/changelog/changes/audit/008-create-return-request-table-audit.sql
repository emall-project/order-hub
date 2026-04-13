--liquibase formatted sql
--changeset lamahafiz:008-create-return-request-table-audit

CREATE TABLE IF NOT EXISTS audit.return_requests_audit (
    rev               INT NOT NULL,
    revtype           SMALLINT,
    return_request_id BIGINT NOT NULL,
    order_item_id     BIGINT,
    customer_id       BIGINT,
    shop_id           BIGINT,
    reason            TEXT,
    description       TEXT,
    status            VARCHAR(20),
    rejection_reason  TEXT,
    image_uuid        UUID,
    created_at        TIMESTAMP,
    created_by        VARCHAR(150),
    updated_at        TIMESTAMP,
    updated_by        VARCHAR(150),
    PRIMARY KEY (return_request_id, rev),
    CONSTRAINT fk_return_request_audit_rev
    FOREIGN KEY (rev)
    REFERENCES audit.revinfo (rev)
);