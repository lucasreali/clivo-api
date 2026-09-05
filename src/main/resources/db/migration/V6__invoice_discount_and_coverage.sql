ALTER TABLE invoice ADD COLUMN discount_reason VARCHAR(200);

ALTER TABLE invoice ADD COLUMN coverage VARCHAR(20) NOT NULL DEFAULT 'DIRECT'
    CHECK (coverage IN ('DIRECT','SESSION_PACKAGE','INSURANCE'));

ALTER TABLE invoice
    ADD CONSTRAINT invoice_discount_reason_required
    CHECK (discount = 0 OR discount_reason IS NOT NULL);

ALTER TABLE invoice ADD CONSTRAINT ux_invoice_encounter UNIQUE (encounter_id);
