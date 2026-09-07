ALTER TABLE customer_insurance DROP CONSTRAINT customer_insurance_pkey;

ALTER TABLE customer_insurance
    ADD COLUMN id UUID PRIMARY KEY;

ALTER TABLE customer_insurance
    ADD CONSTRAINT ux_customer_insurance UNIQUE (customer_id, insurance_plan_id);
