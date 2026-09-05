ALTER TABLE commission
    ADD COLUMN status VARCHAR(10) NOT NULL DEFAULT 'OPEN'
    CHECK (status IN ('OPEN','CLOSED'));

ALTER TABLE commission ADD COLUMN closed_at TIMESTAMPTZ;

CREATE TABLE commission_rate (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    tenant_id       BIGINT       NOT NULL REFERENCES tenant(id),
    practitioner_id BIGINT       NOT NULL REFERENCES practitioner(id),
    percentage      NUMERIC(5,2) NOT NULL CHECK (percentage BETWEEN 0 AND 100),
    UNIQUE (tenant_id, practitioner_id)
);
