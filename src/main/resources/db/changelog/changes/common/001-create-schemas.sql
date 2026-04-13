--liquibase formatted sql
--changeset lamahafiz:001-create-schemas

CREATE SCHEMA IF NOT EXISTS orders;
CREATE SCHEMA IF NOT EXISTS audit;
