ALTER TABLE module          ALTER COLUMN code            TYPE VARCHAR(20);
ALTER TABLE module          ALTER COLUMN requires_module TYPE VARCHAR(20);
ALTER TABLE tenant_module   ALTER COLUMN module_code     TYPE VARCHAR(20);
ALTER TABLE parameter       ALTER COLUMN module_code     TYPE VARCHAR(20);
ALTER TABLE record_template ALTER COLUMN requires_module TYPE VARCHAR(20);
ALTER TABLE template_field  ALTER COLUMN requires_module TYPE VARCHAR(20);

ALTER TABLE module    DROP CONSTRAINT module_requires_module_fkey;
ALTER TABLE parameter DROP CONSTRAINT parameter_module_code_fkey;

UPDATE module SET code = 'dependent'      WHERE code = 'M01';
UPDATE module SET code = 'sessionpackage' WHERE code = 'M02';
UPDATE module SET code = 'inventory'      WHERE code = 'M03';
UPDATE module SET code = 'batch'          WHERE code = 'M04';
UPDATE module SET code = 'insurance'      WHERE code = 'M05';
UPDATE module SET code = 'notification'   WHERE code = 'M06';
UPDATE module SET code = 'commission'     WHERE code = 'M07';

UPDATE module    SET requires_module = 'inventory'    WHERE requires_module = 'M03';
UPDATE parameter SET module_code     = 'batch'        WHERE module_code = 'M04';
UPDATE parameter SET module_code     = 'notification' WHERE module_code = 'M06';

ALTER TABLE module
    ADD CONSTRAINT module_requires_module_fkey FOREIGN KEY (requires_module) REFERENCES module(code);
ALTER TABLE parameter
    ADD CONSTRAINT parameter_module_code_fkey FOREIGN KEY (module_code) REFERENCES module(code);

UPDATE parameter SET min_value = 1, max_value = 168 WHERE code = 'reschedule_window_hours';

ALTER TABLE template_field DROP CONSTRAINT template_field_field_type_check;
