CREATE TABLE sample_entity (
    id          UUID         PRIMARY KEY,
    tenant_id   UUID         NOT NULL REFERENCES tenant(id),
    label       VARCHAR(80)  NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL,
    created_by  UUID,
    updated_at  TIMESTAMPTZ  NOT NULL,
    updated_by  UUID
);
