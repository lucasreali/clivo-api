CREATE TABLE clinical_alert (
    id          UUID         PRIMARY KEY,
    tenant_id   UUID         NOT NULL REFERENCES tenant(id),
    customer_id UUID         NOT NULL REFERENCES customer(id),
    note        VARCHAR(400) NOT NULL,
    recorded_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    recorded_by UUID         NOT NULL REFERENCES app_user(id)
);

CREATE INDEX ix_clinical_alert_customer ON clinical_alert (tenant_id, customer_id);
