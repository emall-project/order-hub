--liquibase formatted sql
--changeset lamahafiz:001-create-audit-revinfo

CREATE SCHEMA IF NOT EXISTS audit;

CREATE SEQUENCE IF NOT EXISTS audit.revinfo_seq
    START WITH 1
    INCREMENT BY 50
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE IF NOT EXISTS audit.revinfo (
    rev INT PRIMARY KEY DEFAULT nextval('audit.revinfo_seq'),
    revtstmp BIGINT NOT NULL
);
