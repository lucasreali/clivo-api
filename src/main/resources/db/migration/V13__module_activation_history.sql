CREATE TABLE tenant_module_history (
    id              UUID         PRIMARY KEY,
    tenant_id       UUID         NOT NULL REFERENCES tenant(id),
    module_code     VARCHAR(20)  NOT NULL,
    action          VARCHAR(12)  NOT NULL
                    CHECK (action IN ('ACTIVATION','DEACTIVATION')),
    changed_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    changed_by      UUID
);

CREATE INDEX ix_tenant_module_history
    ON tenant_module_history (tenant_id, changed_at DESC);
