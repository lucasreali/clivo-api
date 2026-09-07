CREATE TABLE module_grant (
    id          UUID         PRIMARY KEY,
    tenant_id   UUID         NOT NULL REFERENCES tenant(id),
    user_id     UUID         NOT NULL REFERENCES app_user(id),
    module_code VARCHAR(20)  NOT NULL REFERENCES module(code),
    granted_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    granted_by  UUID,
    UNIQUE (user_id, module_code)
);

CREATE INDEX ix_module_grant_user ON module_grant (tenant_id, user_id);
