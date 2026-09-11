UPDATE module SET name = 'Customer dependents',
                  description = 'Dependents section in the customer record and encounters on behalf of a dependent'
    WHERE code = 'dependent';
UPDATE module SET name = 'Session packages',
                  description = 'Package sale and balance drawdown per encounter'
    WHERE code = 'sessionpackage';
UPDATE module SET name = 'Inventory control',
                  description = 'Products, on-hand balance and write-off tied to the encounter'
    WHERE code = 'inventory';
UPDATE module SET name = 'Batch and expiry',
                  description = 'Batch field on the record, expiry alert and refusal to write off'
    WHERE code = 'batch';
UPDATE module SET name = 'Insurance',
                  description = 'Insurance plan and member number on the record, reimbursement table'
    WHERE code = 'insurance';
UPDATE module SET name = 'Notifications',
                  description = 'Appointment reminder and confirmation by the customer'
    WHERE code = 'notification';
UPDATE module SET name = 'Commissions',
                  description = 'Percentage per practitioner and closing report'
    WHERE code = 'commission';
UPDATE module SET name = 'Odontogram',
                  description = 'Dental chart on the clinical record'
    WHERE code = 'odontogram';
UPDATE module SET name = 'Body map',
                  description = 'Map of body regions on the clinical record'
    WHERE code = 'bodymap';

UPDATE parameter SET name = 'Role model'                 WHERE code = 'role_model';
UPDATE parameter SET name = 'Default encounter duration' WHERE code = 'default_duration';
UPDATE parameter SET name = 'Reminder lead time (h)'     WHERE code = 'reminder_lead_hours';
UPDATE parameter SET name = 'Notification channel'       WHERE code = 'notification_channel';
UPDATE parameter SET name = 'Reschedule window (h)'      WHERE code = 'reschedule_window_hours';
UPDATE parameter SET name = 'Monthly late interest (%)'  WHERE code = 'late_interest_pct';
UPDATE parameter SET name = 'Late payment fee (%)'       WHERE code = 'late_fee_pct';
UPDATE parameter SET name = 'Expiry alert lead (days)'   WHERE code = 'expiry_alert_days';
UPDATE parameter SET name = 'Block use of expired batch' WHERE code = 'block_expired_batch';
UPDATE parameter SET name = 'Default record template'    WHERE code = 'default_record_template';

UPDATE component_mark SET label = 'Healthy'              WHERE component = 'ODONTOGRAM' AND code = 'higido';
UPDATE component_mark SET label = 'Caries'               WHERE component = 'ODONTOGRAM' AND code = 'carie';
UPDATE component_mark SET label = 'Restored'             WHERE component = 'ODONTOGRAM' AND code = 'restaurado';
UPDATE component_mark SET label = 'Missing'              WHERE component = 'ODONTOGRAM' AND code = 'ausente';
UPDATE component_mark SET label = 'Extraction indicated' WHERE component = 'ODONTOGRAM' AND code = 'extracao_indicada';
UPDATE component_mark SET label = 'Fracture'             WHERE component = 'ODONTOGRAM' AND code = 'fratura';
UPDATE component_mark SET label = 'Root canal'           WHERE component = 'ODONTOGRAM' AND code = 'canal';
UPDATE component_mark SET label = 'Crown'                WHERE component = 'ODONTOGRAM' AND code = 'coroa';
UPDATE component_mark SET label = 'Implant'              WHERE component = 'ODONTOGRAM' AND code = 'implante';
UPDATE component_mark SET label = 'Prosthesis'           WHERE component = 'ODONTOGRAM' AND code = 'protese';
UPDATE component_mark SET label = 'Sealant'              WHERE component = 'ODONTOGRAM' AND code = 'selante';
UPDATE component_mark SET label = 'Pain'                 WHERE component = 'BODY_MAP'  AND code = 'dor';
UPDATE component_mark SET label = 'Swelling'             WHERE component = 'BODY_MAP'  AND code = 'edema';
UPDATE component_mark SET label = 'Bruising'             WHERE component = 'BODY_MAP'  AND code = 'hematoma';
UPDATE component_mark SET label = 'Scar'                 WHERE component = 'BODY_MAP'  AND code = 'cicatriz';
UPDATE component_mark SET label = 'Limited movement'     WHERE component = 'BODY_MAP'  AND code = 'limitacao';

UPDATE component_mark SET code = 'healthy'              WHERE component = 'ODONTOGRAM' AND code = 'higido';
UPDATE component_mark SET code = 'caries'               WHERE component = 'ODONTOGRAM' AND code = 'carie';
UPDATE component_mark SET code = 'restored'             WHERE component = 'ODONTOGRAM' AND code = 'restaurado';
UPDATE component_mark SET code = 'missing'              WHERE component = 'ODONTOGRAM' AND code = 'ausente';
UPDATE component_mark SET code = 'extraction_indicated' WHERE component = 'ODONTOGRAM' AND code = 'extracao_indicada';
UPDATE component_mark SET code = 'fracture'             WHERE component = 'ODONTOGRAM' AND code = 'fratura';
UPDATE component_mark SET code = 'root_canal'           WHERE component = 'ODONTOGRAM' AND code = 'canal';
UPDATE component_mark SET code = 'crown'                WHERE component = 'ODONTOGRAM' AND code = 'coroa';
UPDATE component_mark SET code = 'implant'              WHERE component = 'ODONTOGRAM' AND code = 'implante';
UPDATE component_mark SET code = 'prosthesis'           WHERE component = 'ODONTOGRAM' AND code = 'protese';
UPDATE component_mark SET code = 'sealant'              WHERE component = 'ODONTOGRAM' AND code = 'selante';
UPDATE component_mark SET code = 'pain'                 WHERE component = 'BODY_MAP'  AND code = 'dor';
UPDATE component_mark SET code = 'swelling'             WHERE component = 'BODY_MAP'  AND code = 'edema';
UPDATE component_mark SET code = 'bruising'             WHERE component = 'BODY_MAP'  AND code = 'hematoma';
UPDATE component_mark SET code = 'scar'                 WHERE component = 'BODY_MAP'  AND code = 'cicatriz';
UPDATE component_mark SET code = 'limited_movement'     WHERE component = 'BODY_MAP'  AND code = 'limitacao';

-- Condition codes and tooth surfaces travel inside the markings already
-- written, so the catalogue and the records are renamed together.
CREATE FUNCTION english_condition(code TEXT) RETURNS TEXT AS $$
    SELECT CASE code
        WHEN 'higido'            THEN 'healthy'
        WHEN 'carie'             THEN 'caries'
        WHEN 'restaurado'        THEN 'restored'
        WHEN 'ausente'           THEN 'missing'
        WHEN 'extracao_indicada' THEN 'extraction_indicated'
        WHEN 'fratura'           THEN 'fracture'
        WHEN 'canal'             THEN 'root_canal'
        WHEN 'coroa'             THEN 'crown'
        WHEN 'implante'          THEN 'implant'
        WHEN 'protese'           THEN 'prosthesis'
        WHEN 'selante'           THEN 'sealant'
        WHEN 'dor'               THEN 'pain'
        WHEN 'edema'             THEN 'swelling'
        WHEN 'hematoma'          THEN 'bruising'
        WHEN 'cicatriz'          THEN 'scar'
        WHEN 'limitacao'         THEN 'limited_movement'
        ELSE code
    END
$$ LANGUAGE SQL IMMUTABLE;

CREATE FUNCTION english_surface(surface TEXT) RETURNS TEXT AS $$
    SELECT CASE surface
        WHEN 'palatina' THEN 'palatal'
        WHEN 'oclusal'  THEN 'occlusal'
        ELSE surface
    END
$$ LANGUAGE SQL IMMUTABLE;

CREATE FUNCTION english_surfaces(value JSONB) RETURNS JSONB AS $$
    SELECT CASE
        WHEN jsonb_typeof(value) <> 'array' THEN value
        ELSE COALESCE(
            (SELECT jsonb_agg(to_jsonb(english_surface(surface)) ORDER BY position)
             FROM jsonb_array_elements_text(value) WITH ORDINALITY AS surfaces(surface, position)),
            value)
    END
$$ LANGUAGE SQL IMMUTABLE;

UPDATE component_chart SET regions = COALESCE(
    (SELECT jsonb_agg(
                CASE
                    WHEN region ? 'parts'
                    THEN jsonb_set(region, '{parts}', english_surfaces(region -> 'parts'))
                    ELSE region
                END
                ORDER BY position)
     FROM jsonb_array_elements(component_chart.regions) WITH ORDINALITY AS entries(region, position)),
    regions)
WHERE regions @? '$[*].parts[*]';

CREATE FUNCTION english_marking(entry JSONB) RETURNS JSONB AS $$
    SELECT CASE WHEN entry ? 'parts'
                THEN jsonb_set(marked, '{parts}', english_surfaces(entry -> 'parts'))
                ELSE marked
           END
    FROM (SELECT CASE WHEN entry ? 'mark'
                      THEN jsonb_set(entry, '{mark}', to_jsonb(english_condition(entry ->> 'mark')))
                      ELSE entry
                 END) AS renamed(marked)
$$ LANGUAGE SQL IMMUTABLE;

CREATE FUNCTION english_markings(value JSONB) RETURNS JSONB AS $$
    SELECT CASE
        WHEN jsonb_typeof(value) <> 'array' THEN value
        ELSE COALESCE(
            (SELECT jsonb_agg(
                        CASE
                            WHEN jsonb_typeof(entry) = 'object'
                            THEN english_marking(entry)
                            ELSE entry
                        END
                        ORDER BY position)
             FROM jsonb_array_elements(value) WITH ORDINALITY AS elements(entry, position)),
            value)
    END
$$ LANGUAGE SQL IMMUTABLE;

UPDATE encounter SET field_values = COALESCE(
    (SELECT jsonb_object_agg(field.key, english_markings(field.value))
     FROM jsonb_each(encounter.field_values) AS field),
    field_values)
WHERE field_values @? '$.*[*].mark';

DROP FUNCTION english_markings(JSONB);
DROP FUNCTION english_marking(JSONB);
DROP FUNCTION english_surfaces(JSONB);
DROP FUNCTION english_surface(TEXT);
DROP FUNCTION english_condition(TEXT);
