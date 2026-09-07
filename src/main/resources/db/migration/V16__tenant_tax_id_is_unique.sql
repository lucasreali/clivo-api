CREATE UNIQUE INDEX ux_tenant_tax_id ON tenant (tax_id) WHERE tax_id IS NOT NULL;
