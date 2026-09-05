ALTER TABLE encounter ADD COLUMN service_id BIGINT REFERENCES service(id);

UPDATE encounter e
   SET service_id = a.service_id
  FROM appointment a
 WHERE a.id = e.appointment_id;

UPDATE encounter e
   SET service_id = (SELECT s.id FROM service s WHERE s.tenant_id = e.tenant_id ORDER BY s.id LIMIT 1)
 WHERE e.service_id IS NULL;

ALTER TABLE encounter ALTER COLUMN service_id SET NOT NULL;
