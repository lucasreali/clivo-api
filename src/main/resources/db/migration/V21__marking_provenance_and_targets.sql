ALTER TABLE encounter ADD COLUMN last_saved_at TIMESTAMPTZ;

UPDATE encounter SET last_saved_at = completed_at WHERE completed_at IS NOT NULL;

ALTER TABLE component_mark ADD COLUMN applies_to VARCHAR(6);

UPDATE component_mark SET applies_to = 'REGION';
UPDATE component_mark SET applies_to = 'PART'
    WHERE component = 'ODONTOGRAM' AND code IN ('carie', 'restaurado');
UPDATE component_mark SET applies_to = 'ANY'
    WHERE component = 'ODONTOGRAM' AND code = 'fratura';

ALTER TABLE component_mark ALTER COLUMN applies_to SET NOT NULL;
ALTER TABLE component_mark ADD CONSTRAINT ck_component_mark_applies_to
    CHECK (applies_to IN ('REGION', 'PART', 'ANY'));

INSERT INTO component_mark (id, component, code, label, rendering, sort_order, applies_to) VALUES
 ('28000000-0000-7000-8000-00000000001b','ODONTOGRAM','selante','Selante','#00695c',11,'PART');

CREATE FUNCTION marking_parts_of(value JSONB) RETURNS JSONB AS $$
    SELECT CASE
        WHEN jsonb_typeof(value) <> 'array' THEN value
        ELSE COALESCE(
            (SELECT jsonb_agg(
                        CASE
                            WHEN jsonb_typeof(entry) = 'object' AND entry ? 'part'
                            THEN (entry - 'part')
                                 || jsonb_build_object('parts', jsonb_build_array(entry -> 'part'))
                            ELSE entry
                        END
                        ORDER BY position)
             FROM jsonb_array_elements(value) WITH ORDINALITY AS elements(entry, position)),
            value)
    END
$$ LANGUAGE SQL IMMUTABLE;

UPDATE encounter SET field_values = COALESCE(
    (SELECT jsonb_object_agg(field.key, marking_parts_of(field.value))
     FROM jsonb_each(encounter.field_values) AS field),
    field_values)
WHERE field_values @? '$.*[*].part';

DROP FUNCTION marking_parts_of(JSONB);
