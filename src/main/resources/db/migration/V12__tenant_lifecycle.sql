ALTER TABLE tenant
    ADD COLUMN status_reason     VARCHAR(200),
    ADD COLUMN status_changed_at TIMESTAMPTZ NOT NULL DEFAULT now();
