ALTER TABLE attachment
    ADD COLUMN uploaded_by UUID NOT NULL REFERENCES app_user(id);

CREATE INDEX ix_attachment_encounter ON attachment (tenant_id, encounter_id);
