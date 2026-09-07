UPDATE tenant   SET tax_id      = regexp_replace(tax_id, '\D', '', 'g')      WHERE tax_id IS NOT NULL;
UPDATE customer SET national_id = regexp_replace(national_id, '\D', '', 'g') WHERE national_id IS NOT NULL;
UPDATE customer SET postal_code = regexp_replace(postal_code, '\D', '', 'g') WHERE postal_code IS NOT NULL;
UPDATE customer SET phone       = regexp_replace(phone, '\D', '', 'g');
UPDATE customer SET email       = lower(btrim(email))                        WHERE email IS NOT NULL;
UPDATE app_user SET email       = lower(btrim(email));
