--liquibase formatted sql
--changeset lamahafiz:007-alter-return-requests-image-uuid-not-null

ALTER TABLE orders.return_requests
    ALTER COLUMN image_uuid SET NOT NULL;