CREATE TABLE sample_entity (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    tenant_id   BIGINT       NOT NULL REFERENCES tenant(id),
    label       VARCHAR(80)  NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL,
    created_by  BIGINT,
    updated_at  TIMESTAMPTZ  NOT NULL,
    updated_by  BIGINT
);
