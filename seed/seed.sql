-- Clivo API — development seed
--
-- Wipes every application row and repopulates the database with a full
-- walkthrough of the system. Reference data owned by the migrations
-- (module, parameter) and Flyway's own history are left untouched.
--
--   docker exec -i clivo-postgres psql -U clivo -d clivo < seed/seed.sql
--
-- Identifiers are literal so the file can be read and referenced: the first
-- byte names the table (0a tenant, 0b app_user, 0c specialty, ...) and the
-- last byte numbers the row. Every one is a well-formed UUID version 7.
-- Dates are relative to the moment the script runs, so "today" is always today.
--
-- Passwords: every user signs in with 12345678 (bcrypt, cost 10).

BEGIN;

TRUNCATE TABLE
    audit_log,
    tenant_module_history,
    module_grant,
    notification,
    package_usage,
    session_package,
    stock_movement,
    batch,
    product,
    commission,
    commission_rate,
    payment,
    invoice_item,
    invoice,
    attachment,
    encounter,
    schedule_block,
    appointment,
    template_field,
    template_section,
    record_template,
    customer_insurance,
    insurance_plan,
    consent,
    dependent,
    customer,
    service,
    availability,
    practitioner,
    specialty,
    tenant_parameter,
    tenant_module,
    app_user,
    tenant,
    event_publication
RESTART IDENTITY CASCADE;

-- ---------------------------------------------------------------------------
-- Clinics
-- ---------------------------------------------------------------------------

INSERT INTO tenant (id, name, legal_name, tax_id, segment, status, status_reason, status_changed_at, created_at) VALUES
 ('0a000000-0000-7000-8000-000000000001', 'Clínica Vida', 'Clínica Vida Saúde Integrada LTDA', '11222333000181', 'Saúde integrativa', 'ACTIVE', NULL, now() - interval '400 days', now() - interval '400 days'),
 ('0a000000-0000-7000-8000-000000000002', 'Odonto Sorriso', 'Odonto Sorriso Serviços Odontológicos LTDA', '22333444000181', 'Odontologia', 'ACTIVE', NULL, now() - interval '200 days', now() - interval '200 days'),
 ('0a000000-0000-7000-8000-000000000003', 'Pet Care', 'Pet Care Clínica Veterinária LTDA', '33444555000181', 'Veterinária', 'SUSPENDED', 'Mensalidade da plataforma em atraso', now() - interval '15 days', now() - interval '120 days');

-- ---------------------------------------------------------------------------
-- Users — password 12345678 for all of them
-- ---------------------------------------------------------------------------

INSERT INTO app_user (id, tenant_id, name, email, password_hash, user_role, status, last_login_at, created_at) VALUES
 ('0b000000-0000-7000-8000-000000000001', NULL,                                   'Administrador da Plataforma', 'admin@mail.com',  '$2a$10$rPtfHczEJnYClCFrJRqpYuQEJ1PwwNLR3rwZ/j7.6MLPXke6wUjkq', 'PLATFORM_ADMIN', 'ACTIVE',   now() - interval '1 day',   now() - interval '400 days'),
 ('0b000000-0000-7000-8000-000000000002', '0a000000-0000-7000-8000-000000000001', 'Maria Souza',                'maria@mail.com',  '$2a$10$rPtfHczEJnYClCFrJRqpYuQEJ1PwwNLR3rwZ/j7.6MLPXke6wUjkq', 'MANAGER',        'ACTIVE',   now() - interval '2 hours', now() - interval '400 days'),
 ('0b000000-0000-7000-8000-000000000003', '0a000000-0000-7000-8000-000000000001', 'João Lima',                  'joao@mail.com',   '$2a$10$rPtfHczEJnYClCFrJRqpYuQEJ1PwwNLR3rwZ/j7.6MLPXke6wUjkq', 'RECEPTION',      'ACTIVE',   now() - interval '3 hours', now() - interval '390 days'),
 ('0b000000-0000-7000-8000-000000000004', '0a000000-0000-7000-8000-000000000001', 'Carla Nunes',                'carla@mail.com',  '$2a$10$rPtfHczEJnYClCFrJRqpYuQEJ1PwwNLR3rwZ/j7.6MLPXke6wUjkq', 'PRACTITIONER',   'ACTIVE',   now() - interval '1 day',   now() - interval '390 days'),
 ('0b000000-0000-7000-8000-000000000005', '0a000000-0000-7000-8000-000000000001', 'Rafael Dias',                'rafael@mail.com', '$2a$10$rPtfHczEJnYClCFrJRqpYuQEJ1PwwNLR3rwZ/j7.6MLPXke6wUjkq', 'PRACTITIONER',   'ACTIVE',   now() - interval '2 days',  now() - interval '360 days'),
 ('0b000000-0000-7000-8000-000000000006', '0a000000-0000-7000-8000-000000000001', 'Pedro Alves',                'pedro@mail.com',  '$2a$10$rPtfHczEJnYClCFrJRqpYuQEJ1PwwNLR3rwZ/j7.6MLPXke6wUjkq', 'ASSISTANT',      'ACTIVE',   NULL,                       now() - interval '120 days'),
 ('0b000000-0000-7000-8000-000000000007', '0a000000-0000-7000-8000-000000000001', 'Inês Barros',                'ines@mail.com',   '$2a$10$rPtfHczEJnYClCFrJRqpYuQEJ1PwwNLR3rwZ/j7.6MLPXke6wUjkq', 'RECEPTION',      'INACTIVE', now() - interval '90 days', now() - interval '300 days'),
 ('0b000000-0000-7000-8000-000000000008', '0a000000-0000-7000-8000-000000000002', 'Lucas Reali',                'lucas@mail.com',  '$2a$10$rPtfHczEJnYClCFrJRqpYuQEJ1PwwNLR3rwZ/j7.6MLPXke6wUjkq', 'MANAGER',        'ACTIVE',   now() - interval '5 hours', now() - interval '200 days'),
 ('0b000000-0000-7000-8000-000000000009', '0a000000-0000-7000-8000-000000000002', 'Ana Prado',                  'ana@mail.com',    '$2a$10$rPtfHczEJnYClCFrJRqpYuQEJ1PwwNLR3rwZ/j7.6MLPXke6wUjkq', 'RECEPTION',      'ACTIVE',   now() - interval '6 hours', now() - interval '190 days'),
 ('0b000000-0000-7000-8000-00000000000a', '0a000000-0000-7000-8000-000000000002', 'Bruno Castro',               'bruno@mail.com',  '$2a$10$rPtfHczEJnYClCFrJRqpYuQEJ1PwwNLR3rwZ/j7.6MLPXke6wUjkq', 'PRACTITIONER',   'ACTIVE',   now() - interval '1 day',   now() - interval '190 days'),
 ('0b000000-0000-7000-8000-00000000000b', '0a000000-0000-7000-8000-000000000003', 'Sofia Martins',              'sofia@mail.com',  '$2a$10$rPtfHczEJnYClCFrJRqpYuQEJ1PwwNLR3rwZ/j7.6MLPXke6wUjkq', 'MANAGER',        'ACTIVE',   now() - interval '20 days', now() - interval '120 days'),
 ('0b000000-0000-7000-8000-00000000000c', '0a000000-0000-7000-8000-000000000003', 'Tiago Rocha',                'tiago@mail.com',  '$2a$10$rPtfHczEJnYClCFrJRqpYuQEJ1PwwNLR3rwZ/j7.6MLPXke6wUjkq', 'PRACTITIONER',   'ACTIVE',   now() - interval '20 days', now() - interval '120 days');

-- ---------------------------------------------------------------------------
-- Module activation (mechanism A)
-- ---------------------------------------------------------------------------

INSERT INTO tenant_module (tenant_id, module_code, enabled, enabled_at, enabled_by) VALUES
 -- Clínica Vida: everything except the odontogram, which was tried and turned off
 ('0a000000-0000-7000-8000-000000000001', 'dependent',      true,  now() - interval '390 days', '0b000000-0000-7000-8000-000000000001'),
 ('0a000000-0000-7000-8000-000000000001', 'sessionpackage', true,  now() - interval '380 days', '0b000000-0000-7000-8000-000000000001'),
 ('0a000000-0000-7000-8000-000000000001', 'inventory',      true,  now() - interval '370 days', '0b000000-0000-7000-8000-000000000001'),
 ('0a000000-0000-7000-8000-000000000001', 'batch',          true,  now() - interval '369 days', '0b000000-0000-7000-8000-000000000001'),
 ('0a000000-0000-7000-8000-000000000001', 'insurance',      true,  now() - interval '360 days', '0b000000-0000-7000-8000-000000000001'),
 ('0a000000-0000-7000-8000-000000000001', 'notification',   true,  now() - interval '350 days', '0b000000-0000-7000-8000-000000000001'),
 ('0a000000-0000-7000-8000-000000000001', 'commission',     true,  now() - interval '340 days', '0b000000-0000-7000-8000-000000000001'),
 ('0a000000-0000-7000-8000-000000000001', 'bodymap',        true,  now() - interval '330 days', '0b000000-0000-7000-8000-000000000001'),
 ('0a000000-0000-7000-8000-000000000001', 'odontogram',     false, NULL, NULL),
 -- Odonto Sorriso
 ('0a000000-0000-7000-8000-000000000002', 'odontogram',     true,  now() - interval '199 days', '0b000000-0000-7000-8000-000000000001'),
 ('0a000000-0000-7000-8000-000000000002', 'commission',     true,  now() - interval '198 days', '0b000000-0000-7000-8000-000000000001'),
 ('0a000000-0000-7000-8000-000000000002', 'notification',   true,  now() - interval '197 days', '0b000000-0000-7000-8000-000000000001'),
 -- Pet Care
 ('0a000000-0000-7000-8000-000000000003', 'dependent',      true,  now() - interval '119 days', '0b000000-0000-7000-8000-000000000001');

INSERT INTO tenant_module_history (id, tenant_id, module_code, action, changed_at, changed_by) VALUES
 ('27000000-0000-7000-8000-000000000001', '0a000000-0000-7000-8000-000000000001', 'dependent',      'ACTIVATION',   now() - interval '390 days', '0b000000-0000-7000-8000-000000000001'),
 ('27000000-0000-7000-8000-000000000002', '0a000000-0000-7000-8000-000000000001', 'sessionpackage', 'ACTIVATION',   now() - interval '380 days', '0b000000-0000-7000-8000-000000000001'),
 ('27000000-0000-7000-8000-000000000003', '0a000000-0000-7000-8000-000000000001', 'inventory',      'ACTIVATION',   now() - interval '370 days', '0b000000-0000-7000-8000-000000000001'),
 ('27000000-0000-7000-8000-000000000004', '0a000000-0000-7000-8000-000000000001', 'batch',          'ACTIVATION',   now() - interval '369 days', '0b000000-0000-7000-8000-000000000001'),
 ('27000000-0000-7000-8000-000000000005', '0a000000-0000-7000-8000-000000000001', 'insurance',      'ACTIVATION',   now() - interval '360 days', '0b000000-0000-7000-8000-000000000001'),
 ('27000000-0000-7000-8000-000000000006', '0a000000-0000-7000-8000-000000000001', 'notification',   'ACTIVATION',   now() - interval '350 days', '0b000000-0000-7000-8000-000000000001'),
 ('27000000-0000-7000-8000-000000000007', '0a000000-0000-7000-8000-000000000001', 'commission',     'ACTIVATION',   now() - interval '340 days', '0b000000-0000-7000-8000-000000000001'),
 ('27000000-0000-7000-8000-000000000008', '0a000000-0000-7000-8000-000000000001', 'bodymap',        'ACTIVATION',   now() - interval '330 days', '0b000000-0000-7000-8000-000000000001'),
 ('27000000-0000-7000-8000-000000000009', '0a000000-0000-7000-8000-000000000001', 'odontogram',     'ACTIVATION',   now() - interval '100 days', '0b000000-0000-7000-8000-000000000001'),
 ('27000000-0000-7000-8000-00000000000a', '0a000000-0000-7000-8000-000000000001', 'odontogram',     'DEACTIVATION', now() - interval '80 days',  '0b000000-0000-7000-8000-000000000001'),
 ('27000000-0000-7000-8000-00000000000b', '0a000000-0000-7000-8000-000000000002', 'odontogram',     'ACTIVATION',   now() - interval '199 days', '0b000000-0000-7000-8000-000000000001'),
 ('27000000-0000-7000-8000-00000000000c', '0a000000-0000-7000-8000-000000000002', 'commission',     'ACTIVATION',   now() - interval '198 days', '0b000000-0000-7000-8000-000000000001'),
 ('27000000-0000-7000-8000-00000000000d', '0a000000-0000-7000-8000-000000000002', 'notification',   'ACTIVATION',   now() - interval '197 days', '0b000000-0000-7000-8000-000000000001'),
 ('27000000-0000-7000-8000-00000000000e', '0a000000-0000-7000-8000-000000000003', 'dependent',      'ACTIVATION',   now() - interval '119 days', '0b000000-0000-7000-8000-000000000001');

-- ---------------------------------------------------------------------------
-- Specialties, practitioners and their weekly availability
-- ---------------------------------------------------------------------------

INSERT INTO specialty (id, tenant_id, name) VALUES
 ('0c000000-0000-7000-8000-000000000001', '0a000000-0000-7000-8000-000000000001', 'Fisioterapia'),
 ('0c000000-0000-7000-8000-000000000002', '0a000000-0000-7000-8000-000000000001', 'Nutrição'),
 ('0c000000-0000-7000-8000-000000000003', '0a000000-0000-7000-8000-000000000001', 'Dermatologia'),
 ('0c000000-0000-7000-8000-000000000004', '0a000000-0000-7000-8000-000000000002', 'Odontologia Geral'),
 ('0c000000-0000-7000-8000-000000000005', '0a000000-0000-7000-8000-000000000002', 'Ortodontia'),
 ('0c000000-0000-7000-8000-000000000006', '0a000000-0000-7000-8000-000000000003', 'Clínica Veterinária');

INSERT INTO practitioner (id, tenant_id, user_id, name, license_number, specialty_id, status) VALUES
 ('0d000000-0000-7000-8000-000000000001', '0a000000-0000-7000-8000-000000000001', '0b000000-0000-7000-8000-000000000004', 'Carla Nunes',    'CREFITO 12345-PR', '0c000000-0000-7000-8000-000000000001', 'ACTIVE'),
 ('0d000000-0000-7000-8000-000000000002', '0a000000-0000-7000-8000-000000000001', '0b000000-0000-7000-8000-000000000005', 'Rafael Dias',    'CRN 54321-PR',     '0c000000-0000-7000-8000-000000000002', 'ACTIVE'),
 ('0d000000-0000-7000-8000-000000000003', '0a000000-0000-7000-8000-000000000001', NULL,                                   'Juliana Moraes', 'CRM 98765-PR',     '0c000000-0000-7000-8000-000000000003', 'ACTIVE'),
 ('0d000000-0000-7000-8000-000000000004', '0a000000-0000-7000-8000-000000000002', '0b000000-0000-7000-8000-00000000000a', 'Bruno Castro',   'CRO 24680-PR',     '0c000000-0000-7000-8000-000000000004', 'ACTIVE'),
 ('0d000000-0000-7000-8000-000000000005', '0a000000-0000-7000-8000-000000000003', '0b000000-0000-7000-8000-00000000000c', 'Tiago Rocha',    'CRMV 13579-PR',    '0c000000-0000-7000-8000-000000000006', 'ACTIVE');

-- weekday: 0 = Monday ... 6 = Sunday.
-- Carla, Rafael, Bruno and Tiago attend every day 08:00–18:00; Juliana only
-- on Tuesday and Thursday afternoons, so /practitioners/{id}/attendance has
-- something to answer "no" to.
INSERT INTO availability (id, tenant_id, practitioner_id, weekday, start_time, end_time)
SELECT ('0e000000-0000-7000-8000-' || lpad(to_hex((row_number() OVER (ORDER BY p.ordinal, d.weekday))::int), 12, '0'))::uuid,
       p.tenant_id, p.practitioner_id, d.weekday::smallint, p.start_time, p.end_time
FROM (VALUES
 (1, '0a000000-0000-7000-8000-000000000001'::uuid, '0d000000-0000-7000-8000-000000000001'::uuid, TIME '08:00', TIME '18:00', ARRAY[0,1,2,3,4,5,6]),
 (2, '0a000000-0000-7000-8000-000000000001'::uuid, '0d000000-0000-7000-8000-000000000002'::uuid, TIME '08:00', TIME '18:00', ARRAY[0,1,2,3,4,5,6]),
 (3, '0a000000-0000-7000-8000-000000000001'::uuid, '0d000000-0000-7000-8000-000000000003'::uuid, TIME '13:00', TIME '17:00', ARRAY[1,3]),
 (4, '0a000000-0000-7000-8000-000000000002'::uuid, '0d000000-0000-7000-8000-000000000004'::uuid, TIME '08:00', TIME '18:00', ARRAY[0,1,2,3,4,5,6]),
 (5, '0a000000-0000-7000-8000-000000000003'::uuid, '0d000000-0000-7000-8000-000000000005'::uuid, TIME '08:00', TIME '18:00', ARRAY[0,1,2,3,4,5,6])
) AS p(ordinal, tenant_id, practitioner_id, start_time, end_time, days)
CROSS JOIN LATERAL unnest(p.days) AS d(weekday);

INSERT INTO commission_rate (id, tenant_id, practitioner_id, percentage) VALUES
 ('1e000000-0000-7000-8000-000000000001', '0a000000-0000-7000-8000-000000000001', '0d000000-0000-7000-8000-000000000001', 30.00),
 ('1e000000-0000-7000-8000-000000000002', '0a000000-0000-7000-8000-000000000001', '0d000000-0000-7000-8000-000000000002', 25.00),
 ('1e000000-0000-7000-8000-000000000003', '0a000000-0000-7000-8000-000000000002', '0d000000-0000-7000-8000-000000000004', 40.00);

-- ---------------------------------------------------------------------------
-- Service catalogue
-- ---------------------------------------------------------------------------

INSERT INTO service (id, tenant_id, name, duration_min, price, specialty_id, status) VALUES
 ('0f000000-0000-7000-8000-000000000001', '0a000000-0000-7000-8000-000000000001', 'Sessão de Fisioterapia',   60, 180.00, '0c000000-0000-7000-8000-000000000001', 'ACTIVE'),
 ('0f000000-0000-7000-8000-000000000002', '0a000000-0000-7000-8000-000000000001', 'Consulta Nutricional',     45, 150.00, '0c000000-0000-7000-8000-000000000002', 'ACTIVE'),
 ('0f000000-0000-7000-8000-000000000003', '0a000000-0000-7000-8000-000000000001', 'Avaliação Dermatológica',  30, 220.00, '0c000000-0000-7000-8000-000000000003', 'ACTIVE'),
 ('0f000000-0000-7000-8000-000000000004', '0a000000-0000-7000-8000-000000000001', 'Massagem Terapêutica',     50, 130.00, NULL,                                   'INACTIVE'),
 ('0f000000-0000-7000-8000-000000000005', '0a000000-0000-7000-8000-000000000002', 'Limpeza Dental',           40, 120.00, '0c000000-0000-7000-8000-000000000004', 'ACTIVE'),
 ('0f000000-0000-7000-8000-000000000006', '0a000000-0000-7000-8000-000000000002', 'Manutenção de Aparelho',   30,  90.00, '0c000000-0000-7000-8000-000000000005', 'ACTIVE'),
 ('0f000000-0000-7000-8000-000000000007', '0a000000-0000-7000-8000-000000000003', 'Consulta Veterinária',     30, 140.00, '0c000000-0000-7000-8000-000000000006', 'ACTIVE');

-- ---------------------------------------------------------------------------
-- Customers, dependents and consents
-- ---------------------------------------------------------------------------

INSERT INTO customer (id, tenant_id, name, national_id, birth_date, phone, email, postal_code, street, status, deactivation_reason, created_at, updated_at, updated_by) VALUES
 ('10000000-0000-7000-8000-000000000001', '0a000000-0000-7000-8000-000000000001', 'Ana Prado',         '11144477735', DATE '1990-03-12', '41999990001', 'ana.prado@mail.com',         '80010000', 'Rua XV de Novembro, 1000',   'ACTIVE',   NULL,                                       now() - interval '380 days', now() - interval '10 days', '0b000000-0000-7000-8000-000000000003'),
 ('10000000-0000-7000-8000-000000000002', '0a000000-0000-7000-8000-000000000001', 'Bruno Carvalho',    '12345678909', DATE '1985-07-24', '41999990002', 'bruno.carvalho@mail.com',    '80230010', 'Av. Sete de Setembro, 250',  'ACTIVE',   NULL,                                       now() - interval '300 days', now() - interval '300 days', NULL),
 ('10000000-0000-7000-8000-000000000003', '0a000000-0000-7000-8000-000000000001', 'Clara Ribeiro',     '98765432100', DATE '2015-11-05', '41999990003', NULL,                         '80420000', 'Rua Bispo Dom José, 800',    'ACTIVE',   NULL,                                       now() - interval '150 days', now() - interval '150 days', NULL),
 ('10000000-0000-7000-8000-000000000004', '0a000000-0000-7000-8000-000000000001', 'Diego Santos',      '52998224725', DATE '1978-01-30', '41999990004', 'diego.santos@mail.com',      NULL,       NULL,                         'INACTIVE', 'Mudou-se de cidade e pediu o encerramento', now() - interval '250 days', now() - interval '5 days',   '0b000000-0000-7000-8000-000000000002'),
 ('10000000-0000-7000-8000-000000000005', '0a000000-0000-7000-8000-000000000002', 'Eduarda Lopes',     '39053344705', DATE '1995-09-18', '41999990005', 'eduarda.lopes@mail.com',     '81530000', 'Rua Jornalista Caio Machado, 45', 'ACTIVE', NULL,                                  now() - interval '190 days', now() - interval '190 days', NULL),
 ('10000000-0000-7000-8000-000000000006', '0a000000-0000-7000-8000-000000000002', 'Felipe Moura',      '11122233396', DATE '2000-02-02', '41999990006', 'felipe.moura@mail.com',      '82530000', 'Rua Nicolau Maeder, 120',    'ACTIVE',   NULL,                                       now() - interval '120 days', now() - interval '120 days', NULL),
 ('10000000-0000-7000-8000-000000000007', '0a000000-0000-7000-8000-000000000003', 'Gabriela Nogueira', '22233344405', DATE '1988-06-21', '41999990007', 'gabriela.nogueira@mail.com', '80530000', 'Rua Padre Anchieta, 2200',   'ACTIVE',   NULL,                                       now() - interval '110 days', now() - interval '110 days', NULL);

INSERT INTO dependent (id, tenant_id, customer_id, name, dependent_type, birth_date, attributes, status, created_at) VALUES
 ('11000000-0000-7000-8000-000000000001', '0a000000-0000-7000-8000-000000000001', '10000000-0000-7000-8000-000000000001', 'Miguel Prado',     'MINOR',    DATE '2018-04-10', '{"parentesco": "filho"}',                                        'ACTIVE', now() - interval '370 days'),
 ('11000000-0000-7000-8000-000000000002', '0a000000-0000-7000-8000-000000000001', '10000000-0000-7000-8000-000000000002', 'Alzira Carvalho',  'ASSISTED', DATE '1940-12-01', '{"condicao": "mobilidade reduzida"}',                            'ACTIVE', now() - interval '290 days'),
 ('11000000-0000-7000-8000-000000000003', '0a000000-0000-7000-8000-000000000003', '10000000-0000-7000-8000-000000000007', 'Thor',             'ANIMAL',   DATE '2020-08-15', '{"especie": "Cão", "raca": "Golden Retriever", "pesoKg": 32.5}',  'ACTIVE', now() - interval '110 days'),
 ('11000000-0000-7000-8000-000000000004', '0a000000-0000-7000-8000-000000000003', '10000000-0000-7000-8000-000000000007', 'Mel',              'ANIMAL',   DATE '2022-01-20', '{"especie": "Gato", "raca": "SRD", "pesoKg": 4.2}',               'ACTIVE', now() - interval '100 days');

INSERT INTO consent (id, tenant_id, customer_id, purpose, granted, source, recorded_at) VALUES
 ('12000000-0000-7000-8000-000000000001', '0a000000-0000-7000-8000-000000000001', '10000000-0000-7000-8000-000000000001', 'DATA_PROCESSING', true,  'FICHA_DE_CADASTRO', now() - interval '380 days'),
 ('12000000-0000-7000-8000-000000000002', '0a000000-0000-7000-8000-000000000001', '10000000-0000-7000-8000-000000000001', 'MARKETING',       true,  'PORTAL',            now() - interval '300 days'),
 ('12000000-0000-7000-8000-000000000003', '0a000000-0000-7000-8000-000000000001', '10000000-0000-7000-8000-000000000001', 'MARKETING',       false, 'PORTAL',            now() - interval '2 days'),
 ('12000000-0000-7000-8000-000000000004', '0a000000-0000-7000-8000-000000000001', '10000000-0000-7000-8000-000000000002', 'DATA_PROCESSING', true,  'FICHA_DE_CADASTRO', now() - interval '300 days'),
 ('12000000-0000-7000-8000-000000000005', '0a000000-0000-7000-8000-000000000001', '10000000-0000-7000-8000-000000000003', 'DATA_PROCESSING', true,  'FICHA_DE_CADASTRO', now() - interval '150 days'),
 ('12000000-0000-7000-8000-000000000006', '0a000000-0000-7000-8000-000000000001', '10000000-0000-7000-8000-000000000004', 'DATA_PROCESSING', true,  'FICHA_DE_CADASTRO', now() - interval '250 days'),
 ('12000000-0000-7000-8000-000000000007', '0a000000-0000-7000-8000-000000000002', '10000000-0000-7000-8000-000000000005', 'DATA_PROCESSING', true,  'FICHA_DE_CADASTRO', now() - interval '190 days'),
 ('12000000-0000-7000-8000-000000000008', '0a000000-0000-7000-8000-000000000002', '10000000-0000-7000-8000-000000000006', 'DATA_PROCESSING', true,  'FICHA_DE_CADASTRO', now() - interval '120 days'),
 ('12000000-0000-7000-8000-000000000009', '0a000000-0000-7000-8000-000000000003', '10000000-0000-7000-8000-000000000007', 'DATA_PROCESSING', true,  'FICHA_DE_CADASTRO', now() - interval '110 days');

-- ---------------------------------------------------------------------------
-- Insurance (module `insurance`, Clínica Vida only)
-- ---------------------------------------------------------------------------

INSERT INTO insurance_plan (id, tenant_id, name, reimbursement_pct, status) VALUES
 ('13000000-0000-7000-8000-000000000001', '0a000000-0000-7000-8000-000000000001', 'Unimed Curitiba', 70.00, 'ACTIVE'),
 ('13000000-0000-7000-8000-000000000002', '0a000000-0000-7000-8000-000000000001', 'Bradesco Saúde',  50.00, 'ACTIVE'),
 ('13000000-0000-7000-8000-000000000003', '0a000000-0000-7000-8000-000000000001', 'Plano Antigo',     0.00, 'INACTIVE');

INSERT INTO customer_insurance (id, tenant_id, customer_id, insurance_plan_id, member_number) VALUES
 ('14000000-0000-7000-8000-000000000001', '0a000000-0000-7000-8000-000000000001', '10000000-0000-7000-8000-000000000001', '13000000-0000-7000-8000-000000000001', '900123456789'),
 ('14000000-0000-7000-8000-000000000002', '0a000000-0000-7000-8000-000000000001', '10000000-0000-7000-8000-000000000002', '13000000-0000-7000-8000-000000000002', '900987654321');

-- ---------------------------------------------------------------------------
-- Record templates (mechanism B)
-- ---------------------------------------------------------------------------

INSERT INTO record_template (id, tenant_id, name, version, status, cloned_from, requires_module, created_at) VALUES
 ('15000000-0000-7000-8000-000000000001', '0a000000-0000-7000-8000-000000000001', 'Ficha de Fisioterapia', 1, 'PUBLISHED', NULL,                                   NULL,         now() - interval '330 days'),
 ('15000000-0000-7000-8000-000000000002', '0a000000-0000-7000-8000-000000000001', 'Ficha Nutricional',     1, 'PUBLISHED', NULL,                                   NULL,         now() - interval '320 days'),
 ('15000000-0000-7000-8000-000000000003', '0a000000-0000-7000-8000-000000000001', 'Ficha de Fisioterapia', 2, 'DRAFT',     '15000000-0000-7000-8000-000000000001', NULL,         now() - interval '10 days'),
 ('15000000-0000-7000-8000-000000000004', '0a000000-0000-7000-8000-000000000002', 'Ficha Odontológica',    1, 'PUBLISHED', NULL,                                   'odontogram', now() - interval '199 days'),
 ('15000000-0000-7000-8000-000000000005', '0a000000-0000-7000-8000-000000000003', 'Ficha Veterinária',     1, 'PUBLISHED', NULL,                                   NULL,         now() - interval '119 days'),
 ('15000000-0000-7000-8000-000000000006', '0a000000-0000-7000-8000-000000000001', 'Ficha Padrão',          1, 'PUBLISHED', NULL,                                   NULL,         now() - interval '395 days');

INSERT INTO template_section (id, record_template_id, name, sort_order) VALUES
 ('16000000-0000-7000-8000-000000000001', '15000000-0000-7000-8000-000000000001', 'Avaliação',      1),
 ('16000000-0000-7000-8000-000000000002', '15000000-0000-7000-8000-000000000001', 'Evolução',       2),
 ('16000000-0000-7000-8000-000000000003', '15000000-0000-7000-8000-000000000002', 'Antropometria',  1),
 ('16000000-0000-7000-8000-000000000004', '15000000-0000-7000-8000-000000000002', 'Plano Alimentar',2),
 ('16000000-0000-7000-8000-000000000005', '15000000-0000-7000-8000-000000000003', 'Avaliação',      1),
 ('16000000-0000-7000-8000-000000000006', '15000000-0000-7000-8000-000000000004', 'Exame',          1),
 ('16000000-0000-7000-8000-000000000007', '15000000-0000-7000-8000-000000000004', 'Procedimento',   2),
 ('16000000-0000-7000-8000-000000000008', '15000000-0000-7000-8000-000000000005', 'Anamnese',       1),
 ('16000000-0000-7000-8000-000000000009', '15000000-0000-7000-8000-000000000006', 'Anamnese',       1),
 ('16000000-0000-7000-8000-00000000000a', '15000000-0000-7000-8000-000000000006', 'Conduta',        2);

-- field_type is the name of a field factory: SHORT_TEXT, LONG_TEXT, INTEGER,
-- DECIMAL, SCALE, DATE, SINGLE_CHOICE or COMPONENT (which names a component).
INSERT INTO template_field (id, template_section_id, code, label, field_type, component, required, sort_order, options, validation, requires_module) VALUES
 ('17000000-0000-7000-8000-000000000001', '16000000-0000-7000-8000-000000000001', 'dor_escala',        'Dor (0 a 10)',          'SCALE',         NULL,           true,  1, NULL,                                                        '{"min": 0, "max": 10}',    NULL),
 ('17000000-0000-7000-8000-000000000002', '16000000-0000-7000-8000-000000000001', 'regiao',            'Região tratada',        'SINGLE_CHOICE', NULL,           false, 2, '["Cervical", "Lombar", "Joelho", "Ombro"]',                  NULL,                       NULL),
 ('17000000-0000-7000-8000-000000000003', '16000000-0000-7000-8000-000000000001', 'mapa_corporal',     'Mapa corporal',         'COMPONENT',     'BODY_MAP',     false, 3, '["cervical", "lombar", "joelho", "ombro"]',                  NULL,                       'bodymap'),
 ('17000000-0000-7000-8000-000000000004', '16000000-0000-7000-8000-000000000002', 'evolucao',          'Evolução da sessão',    'LONG_TEXT',     NULL,           false, 1, NULL,                                                        NULL,                       NULL),
 ('17000000-0000-7000-8000-000000000005', '16000000-0000-7000-8000-000000000002', 'alta_prevista',     'Alta prevista',         'DATE',          NULL,           false, 2, NULL,                                                        NULL,                       NULL),
 ('17000000-0000-7000-8000-000000000006', '16000000-0000-7000-8000-000000000003', 'peso_kg',           'Peso (kg)',             'DECIMAL',       NULL,           true,  1, NULL,                                                        '{"min": 1, "max": 400}',   NULL),
 ('17000000-0000-7000-8000-000000000007', '16000000-0000-7000-8000-000000000003', 'altura_cm',         'Altura (cm)',           'INTEGER',       NULL,           true,  2, NULL,                                                        '{"min": 30, "max": 250}',  NULL),
 ('17000000-0000-7000-8000-000000000008', '16000000-0000-7000-8000-000000000004', 'objetivo',          'Objetivo',              'SINGLE_CHOICE', NULL,           false, 1, '["Emagrecimento", "Hipertrofia", "Manutenção"]',             NULL,                       NULL),
 ('17000000-0000-7000-8000-000000000009', '16000000-0000-7000-8000-000000000004', 'orientacoes',       'Orientações',           'LONG_TEXT',     NULL,           false, 2, NULL,                                                        NULL,                       NULL),
 ('17000000-0000-7000-8000-00000000000a', '16000000-0000-7000-8000-000000000005', 'dor_escala',        'Dor (0 a 10)',          'SCALE',         NULL,           true,  1, NULL,                                                        '{"min": 0, "max": 10}',    NULL),
 ('17000000-0000-7000-8000-00000000000b', '16000000-0000-7000-8000-000000000005', 'regiao',            'Região tratada',        'SINGLE_CHOICE', NULL,           false, 2, '["Cervical", "Lombar", "Joelho", "Ombro"]',                  NULL,                       NULL),
 ('17000000-0000-7000-8000-00000000000c', '16000000-0000-7000-8000-000000000005', 'sessoes_previstas', 'Sessões previstas',     'INTEGER',       NULL,           false, 3, NULL,                                                        '{"min": 1, "max": 60}',    NULL),
 ('17000000-0000-7000-8000-00000000000d', '16000000-0000-7000-8000-000000000006', 'odontograma',       'Odontograma',           'COMPONENT',     'ODONTOGRAM',   false, 1, NULL,                                                        NULL,                       'odontogram'),
 ('17000000-0000-7000-8000-00000000000e', '16000000-0000-7000-8000-000000000006', 'observacoes',       'Observações do exame',  'LONG_TEXT',     NULL,           false, 2, NULL,                                                        NULL,                       NULL),
 ('17000000-0000-7000-8000-00000000000f', '16000000-0000-7000-8000-000000000007', 'procedimento',      'Procedimento aplicado', 'SHORT_TEXT',    NULL,           true,  1, NULL,                                                        '{"maxLength": 120}',       NULL),
 ('17000000-0000-7000-8000-000000000010', '16000000-0000-7000-8000-000000000008', 'especie',           'Espécie',               'SHORT_TEXT',    NULL,           true,  1, NULL,                                                        '{"maxLength": 40}',        NULL),
 ('17000000-0000-7000-8000-000000000011', '16000000-0000-7000-8000-000000000008', 'peso_kg',           'Peso (kg)',             'DECIMAL',       NULL,           false, 2, NULL,                                                        '{"min": 0, "max": 120}',   NULL),
 ('17000000-0000-7000-8000-000000000012', '16000000-0000-7000-8000-000000000008', 'vacinas',           'Vacinas em dia',        'LONG_TEXT',     NULL,           false, 3, NULL,                                                        NULL,                       NULL),
 ('17000000-0000-7000-8000-000000000013', '16000000-0000-7000-8000-000000000009', 'queixa',            'Queixa principal',      'SHORT_TEXT',    NULL,           true,  1, NULL,                                                        '{"maxLength": 200}',       NULL),
 ('17000000-0000-7000-8000-000000000014', '16000000-0000-7000-8000-000000000009', 'historico',         'Histórico',             'LONG_TEXT',     NULL,           false, 2, NULL,                                                        NULL,                       NULL),
 ('17000000-0000-7000-8000-000000000015', '16000000-0000-7000-8000-00000000000a', 'diagnostico',       'Diagnóstico',           'SHORT_TEXT',    NULL,           false, 1, NULL,                                                        '{"maxLength": 200}',       NULL),
 ('17000000-0000-7000-8000-000000000016', '16000000-0000-7000-8000-00000000000a', 'conduta',           'Conduta',               'LONG_TEXT',     NULL,           false, 2, NULL,                                                        NULL,                       NULL),
 ('17000000-0000-7000-8000-000000000017', '16000000-0000-7000-8000-00000000000a', 'retorno_dias',      'Retorno em (dias)',     'INTEGER',       NULL,           false, 3, NULL,                                                        '{"min": 0, "max": 365}',   NULL);

-- ---------------------------------------------------------------------------
-- Clinic parameters (mechanism C) — only what differs from the default
-- ---------------------------------------------------------------------------

INSERT INTO tenant_parameter (tenant_id, parameter_code, value, updated_at, updated_by) VALUES
 ('0a000000-0000-7000-8000-000000000001', 'role_model',              'SEGREGATED',                             now() - interval '390 days', '0b000000-0000-7000-8000-000000000002'),
 ('0a000000-0000-7000-8000-000000000001', 'default_duration',        '45',                                     now() - interval '380 days', '0b000000-0000-7000-8000-000000000002'),
 ('0a000000-0000-7000-8000-000000000001', 'default_record_template', '15000000-0000-7000-8000-000000000001',    now() - interval '330 days', '0b000000-0000-7000-8000-000000000002'),
 ('0a000000-0000-7000-8000-000000000001', 'reminder_lead_hours',     '48',                                     now() - interval '350 days', '0b000000-0000-7000-8000-000000000002'),
 ('0a000000-0000-7000-8000-000000000001', 'notification_channel',    '["WHATSAPP", "EMAIL"]',                   now() - interval '350 days', '0b000000-0000-7000-8000-000000000002'),
 ('0a000000-0000-7000-8000-000000000001', 'expiry_alert_days',       '45',                                     now() - interval '369 days', '0b000000-0000-7000-8000-000000000002'),
 ('0a000000-0000-7000-8000-000000000001', 'block_expired_batch',     'true',                                   now() - interval '369 days', '0b000000-0000-7000-8000-000000000002'),
 ('0a000000-0000-7000-8000-000000000001', 'late_fee_pct',            '2',                                      now() - interval '340 days', '0b000000-0000-7000-8000-000000000002'),
 ('0a000000-0000-7000-8000-000000000001', 'late_interest_pct',       '1',                                      now() - interval '340 days', '0b000000-0000-7000-8000-000000000002'),
 ('0a000000-0000-7000-8000-000000000001', 'reschedule_window_hours', '12',                                     now() - interval '300 days', '0b000000-0000-7000-8000-000000000002'),
 ('0a000000-0000-7000-8000-000000000002', 'role_model',              'SINGLE',                                 now() - interval '199 days', '0b000000-0000-7000-8000-000000000008'),
 ('0a000000-0000-7000-8000-000000000002', 'default_duration',        '30',                                     now() - interval '199 days', '0b000000-0000-7000-8000-000000000008'),
 ('0a000000-0000-7000-8000-000000000002', 'default_record_template', '15000000-0000-7000-8000-000000000004',    now() - interval '199 days', '0b000000-0000-7000-8000-000000000008'),
 ('0a000000-0000-7000-8000-000000000002', 'reminder_lead_hours',     '24',                                     now() - interval '197 days', '0b000000-0000-7000-8000-000000000008'),
 ('0a000000-0000-7000-8000-000000000003', 'default_duration',        '30',                                     now() - interval '119 days', '0b000000-0000-7000-8000-00000000000b'),
 ('0a000000-0000-7000-8000-000000000003', 'default_record_template', '15000000-0000-7000-8000-000000000005',    now() - interval '119 days', '0b000000-0000-7000-8000-00000000000b');

-- ---------------------------------------------------------------------------
-- Module access per user (Strategy — SEGREGATED profiles).
-- Managers reach every active module without a grant.
-- ---------------------------------------------------------------------------

INSERT INTO module_grant (id, tenant_id, user_id, module_code, granted_at, granted_by) VALUES
 ('26000000-0000-7000-8000-000000000001', '0a000000-0000-7000-8000-000000000001', '0b000000-0000-7000-8000-000000000003', 'dependent',      now() - interval '380 days', '0b000000-0000-7000-8000-000000000002'),
 ('26000000-0000-7000-8000-000000000002', '0a000000-0000-7000-8000-000000000001', '0b000000-0000-7000-8000-000000000003', 'sessionpackage', now() - interval '380 days', '0b000000-0000-7000-8000-000000000002'),
 ('26000000-0000-7000-8000-000000000003', '0a000000-0000-7000-8000-000000000001', '0b000000-0000-7000-8000-000000000003', 'insurance',      now() - interval '360 days', '0b000000-0000-7000-8000-000000000002'),
 ('26000000-0000-7000-8000-000000000004', '0a000000-0000-7000-8000-000000000001', '0b000000-0000-7000-8000-000000000003', 'notification',   now() - interval '350 days', '0b000000-0000-7000-8000-000000000002'),
 ('26000000-0000-7000-8000-000000000005', '0a000000-0000-7000-8000-000000000001', '0b000000-0000-7000-8000-000000000004', 'dependent',      now() - interval '380 days', '0b000000-0000-7000-8000-000000000002'),
 ('26000000-0000-7000-8000-000000000006', '0a000000-0000-7000-8000-000000000001', '0b000000-0000-7000-8000-000000000004', 'inventory',      now() - interval '370 days', '0b000000-0000-7000-8000-000000000002'),
 ('26000000-0000-7000-8000-000000000007', '0a000000-0000-7000-8000-000000000001', '0b000000-0000-7000-8000-000000000004', 'batch',          now() - interval '369 days', '0b000000-0000-7000-8000-000000000002'),
 ('26000000-0000-7000-8000-000000000008', '0a000000-0000-7000-8000-000000000001', '0b000000-0000-7000-8000-000000000004', 'bodymap',        now() - interval '330 days', '0b000000-0000-7000-8000-000000000002'),
 ('26000000-0000-7000-8000-000000000009', '0a000000-0000-7000-8000-000000000001', '0b000000-0000-7000-8000-000000000006', 'inventory',      now() - interval '120 days', '0b000000-0000-7000-8000-000000000002'),
 ('26000000-0000-7000-8000-00000000000a', '0a000000-0000-7000-8000-000000000002', '0b000000-0000-7000-8000-000000000009', 'notification',   now() - interval '190 days', '0b000000-0000-7000-8000-000000000008'),
 ('26000000-0000-7000-8000-00000000000b', '0a000000-0000-7000-8000-000000000002', '0b000000-0000-7000-8000-00000000000a', 'odontogram',     now() - interval '190 days', '0b000000-0000-7000-8000-000000000008');

-- ---------------------------------------------------------------------------
-- Agenda
-- ---------------------------------------------------------------------------

INSERT INTO appointment (id, tenant_id, customer_id, dependent_id, practitioner_id, service_id, starts_at, ends_at, status, reason, source, created_by, created_at) VALUES
 ('18000000-0000-7000-8000-000000000001', '0a000000-0000-7000-8000-000000000001', '10000000-0000-7000-8000-000000000001', NULL, '0d000000-0000-7000-8000-000000000001', '0f000000-0000-7000-8000-000000000001', date_trunc('day', now()) + interval '9 hours',                      date_trunc('day', now()) + interval '10 hours',                     'CONFIRMED', NULL,                                          'RECEPTION', '0b000000-0000-7000-8000-000000000003', now() - interval '5 days'),
 ('18000000-0000-7000-8000-000000000002', '0a000000-0000-7000-8000-000000000001', '10000000-0000-7000-8000-000000000002', NULL, '0d000000-0000-7000-8000-000000000001', '0f000000-0000-7000-8000-000000000001', date_trunc('day', now()) + interval '10 hours',                     date_trunc('day', now()) + interval '11 hours',                     'ARRIVED',   NULL,                                          'RECEPTION', '0b000000-0000-7000-8000-000000000003', now() - interval '4 days'),
 ('18000000-0000-7000-8000-000000000003', '0a000000-0000-7000-8000-000000000001', '10000000-0000-7000-8000-000000000001', NULL, '0d000000-0000-7000-8000-000000000002', '0f000000-0000-7000-8000-000000000002', date_trunc('day', now()) + interval '14 hours',                     date_trunc('day', now()) + interval '14 hours 45 minutes',          'SCHEDULED', NULL,                                          'RECEPTION', '0b000000-0000-7000-8000-000000000003', now() - interval '3 days'),
 ('18000000-0000-7000-8000-000000000004', '0a000000-0000-7000-8000-000000000001', '10000000-0000-7000-8000-000000000003', NULL, '0d000000-0000-7000-8000-000000000001', '0f000000-0000-7000-8000-000000000001', date_trunc('day', now()) - interval '1 day' + interval '9 hours',   date_trunc('day', now()) - interval '1 day' + interval '10 hours',  'COMPLETED', NULL,                                          'RECEPTION', '0b000000-0000-7000-8000-000000000003', now() - interval '8 days'),
 ('18000000-0000-7000-8000-000000000005', '0a000000-0000-7000-8000-000000000001', '10000000-0000-7000-8000-000000000002', NULL, '0d000000-0000-7000-8000-000000000002', '0f000000-0000-7000-8000-000000000002', date_trunc('day', now()) - interval '7 days' + interval '15 hours', date_trunc('day', now()) - interval '7 days' + interval '15 hours 45 minutes', 'CANCELLED', 'Cliente pediu para remarcar',      'RECEPTION', '0b000000-0000-7000-8000-000000000003', now() - interval '14 days'),
 ('18000000-0000-7000-8000-000000000006', '0a000000-0000-7000-8000-000000000001', '10000000-0000-7000-8000-000000000001', NULL, '0d000000-0000-7000-8000-000000000001', '0f000000-0000-7000-8000-000000000001', date_trunc('day', now()) - interval '3 days' + interval '9 hours',  date_trunc('day', now()) - interval '3 days' + interval '10 hours', 'NO_SHOW',   'Cliente não compareceu e não avisou',          'RECEPTION', '0b000000-0000-7000-8000-000000000003', now() - interval '10 days'),
 ('18000000-0000-7000-8000-000000000007', '0a000000-0000-7000-8000-000000000001', '10000000-0000-7000-8000-000000000001', NULL, '0d000000-0000-7000-8000-000000000001', '0f000000-0000-7000-8000-000000000001', date_trunc('day', now()) + interval '1 day 9 hours',                date_trunc('day', now()) + interval '1 day 10 hours',               'SCHEDULED', NULL,                                          'RECEPTION', '0b000000-0000-7000-8000-000000000003', now() - interval '2 days'),
 ('18000000-0000-7000-8000-000000000008', '0a000000-0000-7000-8000-000000000002', '10000000-0000-7000-8000-000000000005', NULL, '0d000000-0000-7000-8000-000000000004', '0f000000-0000-7000-8000-000000000005', date_trunc('day', now()) + interval '8 hours 30 minutes',           date_trunc('day', now()) + interval '9 hours 10 minutes',           'CONFIRMED', NULL,                                          'RECEPTION', '0b000000-0000-7000-8000-000000000009', now() - interval '6 days'),
 ('18000000-0000-7000-8000-000000000009', '0a000000-0000-7000-8000-000000000002', '10000000-0000-7000-8000-000000000006', NULL, '0d000000-0000-7000-8000-000000000004', '0f000000-0000-7000-8000-000000000006', date_trunc('day', now()) - interval '1 day' + interval '10 hours',  date_trunc('day', now()) - interval '1 day' + interval '10 hours 30 minutes', 'COMPLETED', NULL,                                'RECEPTION', '0b000000-0000-7000-8000-000000000009', now() - interval '9 days'),
 ('18000000-0000-7000-8000-00000000000a', '0a000000-0000-7000-8000-000000000002', '10000000-0000-7000-8000-000000000005', NULL, '0d000000-0000-7000-8000-000000000004', '0f000000-0000-7000-8000-000000000005', date_trunc('day', now()) + interval '1 day 9 hours',                date_trunc('day', now()) + interval '1 day 9 hours 40 minutes',     'SCHEDULED', NULL,                                          'RECEPTION', '0b000000-0000-7000-8000-000000000009', now() - interval '1 day'),
 ('18000000-0000-7000-8000-00000000000b', '0a000000-0000-7000-8000-000000000003', '10000000-0000-7000-8000-000000000007', NULL, '0d000000-0000-7000-8000-000000000005', '0f000000-0000-7000-8000-000000000007', date_trunc('day', now()) + interval '11 hours',                     date_trunc('day', now()) + interval '11 hours 30 minutes',          'SCHEDULED', NULL,                                          'RECEPTION', '0b000000-0000-7000-8000-00000000000b', now() - interval '2 days');

INSERT INTO schedule_block (id, tenant_id, practitioner_id, starts_at, ends_at, reason) VALUES
 ('19000000-0000-7000-8000-000000000001', '0a000000-0000-7000-8000-000000000001', '0d000000-0000-7000-8000-000000000001', date_trunc('day', now()) + interval '1 day 12 hours', date_trunc('day', now()) + interval '1 day 13 hours', 'Almoço'),
 ('19000000-0000-7000-8000-000000000002', '0a000000-0000-7000-8000-000000000001', NULL,                                   date_trunc('day', now()) + interval '7 days',         date_trunc('day', now()) + interval '8 days',         'Feriado municipal'),
 ('19000000-0000-7000-8000-000000000003', '0a000000-0000-7000-8000-000000000002', '0d000000-0000-7000-8000-000000000004', date_trunc('day', now()) + interval '12 hours',       date_trunc('day', now()) + interval '13 hours 30 minutes', 'Almoço');

-- ---------------------------------------------------------------------------
-- Encounters (Abstract Factory assembles the record sheet from the template)
-- ---------------------------------------------------------------------------

INSERT INTO encounter (id, tenant_id, appointment_id, customer_id, dependent_id, practitioner_id, service_id, record_template_id, field_values, started_at, completed_at, status, signed_by) VALUES
 ('1a000000-0000-7000-8000-000000000001', '0a000000-0000-7000-8000-000000000001', '18000000-0000-7000-8000-000000000004', '10000000-0000-7000-8000-000000000003', NULL, '0d000000-0000-7000-8000-000000000001', '0f000000-0000-7000-8000-000000000001', '15000000-0000-7000-8000-000000000001', '{"dor_escala": 7, "regiao": "Lombar", "mapa_corporal": [{"region": "lombar", "mark": "dor", "note": "Dor moderada à palpação"}], "evolucao": "Paciente relata melhora após a terceira sessão."}', date_trunc('day', now()) - interval '1 day' + interval '9 hours 5 minutes', date_trunc('day', now()) - interval '1 day' + interval '9 hours 55 minutes', 'COMPLETED', NULL),
 ('1a000000-0000-7000-8000-000000000002', '0a000000-0000-7000-8000-000000000001', NULL,                                   '10000000-0000-7000-8000-000000000002', NULL, '0d000000-0000-7000-8000-000000000002', '0f000000-0000-7000-8000-000000000002', '15000000-0000-7000-8000-000000000002', '{"peso_kg": 78.4, "altura_cm": 175}',                                                                                                                        now() - interval '2 hours',                                                NULL,                                                                       'DRAFT',     NULL),
 ('1a000000-0000-7000-8000-000000000003', '0a000000-0000-7000-8000-000000000001', NULL,                                   '10000000-0000-7000-8000-000000000001', NULL, '0d000000-0000-7000-8000-000000000001', '0f000000-0000-7000-8000-000000000001', '15000000-0000-7000-8000-000000000001', '{"dor_escala": 5, "regiao": "Ombro", "evolucao": "Primeira sessão do pacote."}',                                                                              now() - interval '1 month',                                                now() - interval '1 month' + interval '1 hour',                             'COMPLETED', NULL),
 ('1a000000-0000-7000-8000-000000000004', '0a000000-0000-7000-8000-000000000001', NULL,                                   '10000000-0000-7000-8000-000000000001', NULL, '0d000000-0000-7000-8000-000000000002', '0f000000-0000-7000-8000-000000000002', '15000000-0000-7000-8000-000000000002', '{"peso_kg": 62.0, "altura_cm": 168, "objetivo": "Emagrecimento", "orientacoes": "Plano de 1800 kcal, reavaliação em 30 dias."}',                              now() - interval '10 days',                                                now() - interval '10 days' + interval '45 minutes',                         'COMPLETED', NULL),
 ('1a000000-0000-7000-8000-000000000005', '0a000000-0000-7000-8000-000000000001', NULL,                                   '10000000-0000-7000-8000-000000000001', NULL, '0d000000-0000-7000-8000-000000000001', '0f000000-0000-7000-8000-000000000001', '15000000-0000-7000-8000-000000000001', '{"dor_escala": 8, "regiao": "Cervical", "evolucao": "Tensão cervical importante, orientado alongamento diário."}',                                            now() - interval '20 days',                                                now() - interval '20 days' + interval '1 hour',                             'COMPLETED', NULL),
 ('1a000000-0000-7000-8000-000000000006', '0a000000-0000-7000-8000-000000000001', NULL,                                   '10000000-0000-7000-8000-000000000002', NULL, '0d000000-0000-7000-8000-000000000001', '0f000000-0000-7000-8000-000000000001', '15000000-0000-7000-8000-000000000001', '{"dor_escala": 4, "regiao": "Joelho", "evolucao": "Retorno pós-cirúrgico, boa amplitude."}',                                                                  now() - interval '15 days',                                                now() - interval '15 days' + interval '1 hour',                             'COMPLETED', NULL),
 ('1a000000-0000-7000-8000-000000000007', '0a000000-0000-7000-8000-000000000002', '18000000-0000-7000-8000-000000000009', '10000000-0000-7000-8000-000000000006', NULL, '0d000000-0000-7000-8000-000000000004', '0f000000-0000-7000-8000-000000000006', '15000000-0000-7000-8000-000000000004', '{"odontograma": [{"region": "11", "mark": "higido"}, {"region": "21", "part": "oclusal", "mark": "restaurado", "note": "Resina composta"}], "observacoes": "Aparelho ajustado sem intercorrências.", "procedimento": "Manutenção de aparelho"}',    date_trunc('day', now()) - interval '1 day' + interval '10 hours',          date_trunc('day', now()) - interval '1 day' + interval '10 hours 25 minutes', 'COMPLETED', NULL),
 ('1a000000-0000-7000-8000-000000000008', '0a000000-0000-7000-8000-000000000002', NULL,                                   '10000000-0000-7000-8000-000000000005', NULL, '0d000000-0000-7000-8000-000000000004', '0f000000-0000-7000-8000-000000000005', '15000000-0000-7000-8000-000000000004', '{}',                                                                                                                                                         now() - interval '1 hour',                                                 NULL,                                                                       'DRAFT',     NULL),
 ('1a000000-0000-7000-8000-000000000009', '0a000000-0000-7000-8000-000000000003', NULL,                                   '10000000-0000-7000-8000-000000000007', NULL, '0d000000-0000-7000-8000-000000000005', '0f000000-0000-7000-8000-000000000007', '15000000-0000-7000-8000-000000000005', '{"especie": "Cão", "peso_kg": 32.5}',                                                                                                                        now() - interval '3 hours',                                                NULL,                                                                       'DRAFT',     NULL);

-- ---------------------------------------------------------------------------
-- Billing — one invoice per completed encounter, covering every status
-- ---------------------------------------------------------------------------

INSERT INTO invoice (id, tenant_id, encounter_id, customer_id, insurance_plan_id, gross_amount, discount, net_amount, due_date, status, discount_reason, coverage, created_at) VALUES
 ('1b000000-0000-7000-8000-000000000001', '0a000000-0000-7000-8000-000000000001', '1a000000-0000-7000-8000-000000000001', '10000000-0000-7000-8000-000000000003', NULL, 180.00,   0.00, 180.00, CURRENT_DATE + 6,   'PAID',    NULL,                                     'DIRECT',          now() - interval '1 day'),
 ('1b000000-0000-7000-8000-000000000002', '0a000000-0000-7000-8000-000000000001', '1a000000-0000-7000-8000-000000000003', '10000000-0000-7000-8000-000000000001', NULL, 180.00, 180.00,   0.00, CURRENT_DATE - 23,  'PAID',    'fully covered by SESSION_PACKAGE',       'SESSION_PACKAGE', now() - interval '1 month'),
 ('1b000000-0000-7000-8000-000000000003', '0a000000-0000-7000-8000-000000000001', '1a000000-0000-7000-8000-000000000004', '10000000-0000-7000-8000-000000000001', NULL, 150.00, 105.00,  45.00, CURRENT_DATE - 3,   'PARTIAL', 'Unimed Curitiba cobre 70% do valor',     'INSURANCE',       now() - interval '10 days'),
 ('1b000000-0000-7000-8000-000000000004', '0a000000-0000-7000-8000-000000000001', '1a000000-0000-7000-8000-000000000005', '10000000-0000-7000-8000-000000000001', NULL, 180.00,   0.00, 180.00, CURRENT_DATE - 13,  'OPEN',    NULL,                                     'DIRECT',          now() - interval '20 days'),
 ('1b000000-0000-7000-8000-000000000005', '0a000000-0000-7000-8000-000000000001', '1a000000-0000-7000-8000-000000000006', '10000000-0000-7000-8000-000000000002', NULL, 180.00, 180.00,   0.00, CURRENT_DATE - 8,   'PAID',    'fully covered by SESSION_PACKAGE',       'SESSION_PACKAGE', now() - interval '15 days'),
 ('1b000000-0000-7000-8000-000000000006', '0a000000-0000-7000-8000-000000000002', '1a000000-0000-7000-8000-000000000007', '10000000-0000-7000-8000-000000000006', NULL,  90.00,  10.00,  80.00, CURRENT_DATE + 6,   'OPEN',    'Desconto de fidelidade',                 'DIRECT',          now() - interval '1 day');

INSERT INTO invoice_item (id, invoice_id, service_id, description, quantity, unit_price) VALUES
 ('1c000000-0000-7000-8000-000000000001', '1b000000-0000-7000-8000-000000000001', '0f000000-0000-7000-8000-000000000001', 'Sessão de Fisioterapia', 1, 180.00),
 ('1c000000-0000-7000-8000-000000000002', '1b000000-0000-7000-8000-000000000002', '0f000000-0000-7000-8000-000000000001', 'Sessão de Fisioterapia', 1, 180.00),
 ('1c000000-0000-7000-8000-000000000003', '1b000000-0000-7000-8000-000000000003', '0f000000-0000-7000-8000-000000000002', 'Consulta Nutricional',   1, 150.00),
 ('1c000000-0000-7000-8000-000000000004', '1b000000-0000-7000-8000-000000000004', '0f000000-0000-7000-8000-000000000001', 'Sessão de Fisioterapia', 1, 180.00),
 ('1c000000-0000-7000-8000-000000000005', '1b000000-0000-7000-8000-000000000005', '0f000000-0000-7000-8000-000000000001', 'Sessão de Fisioterapia', 1, 180.00),
 ('1c000000-0000-7000-8000-000000000006', '1b000000-0000-7000-8000-000000000006', '0f000000-0000-7000-8000-000000000006', 'Manutenção de Aparelho', 1,  90.00);

INSERT INTO payment (id, tenant_id, invoice_id, amount, method, paid_at, recorded_by, refunded_at, refund_reason) VALUES
 ('1d000000-0000-7000-8000-000000000001', '0a000000-0000-7000-8000-000000000001', '1b000000-0000-7000-8000-000000000001', 180.00, 'PIX',    now() - interval '1 day',   '0b000000-0000-7000-8000-000000000003', NULL,                       NULL),
 ('1d000000-0000-7000-8000-000000000002', '0a000000-0000-7000-8000-000000000001', '1b000000-0000-7000-8000-000000000003',  20.00, 'CREDIT', now() - interval '9 days',  '0b000000-0000-7000-8000-000000000003', NULL,                       NULL),
 ('1d000000-0000-7000-8000-000000000003', '0a000000-0000-7000-8000-000000000001', '1b000000-0000-7000-8000-000000000004', 180.00, 'CASH',   now() - interval '20 days', '0b000000-0000-7000-8000-000000000003', now() - interval '18 days', 'Atendimento remarcado a pedido do cliente');

-- Commission is a percentage of the invoice's net amount, so the package-covered
-- encounters legitimately earn zero.
INSERT INTO commission (id, tenant_id, encounter_id, practitioner_id, percentage, amount, period, status, closed_at) VALUES
 ('1f000000-0000-7000-8000-000000000001', '0a000000-0000-7000-8000-000000000001', '1a000000-0000-7000-8000-000000000001', '0d000000-0000-7000-8000-000000000001', 30.00, 54.00, date_trunc('month', CURRENT_DATE)::date,                          'OPEN',   NULL),
 ('1f000000-0000-7000-8000-000000000002', '0a000000-0000-7000-8000-000000000001', '1a000000-0000-7000-8000-000000000004', '0d000000-0000-7000-8000-000000000002', 25.00, 11.25, date_trunc('month', CURRENT_DATE)::date,                          'OPEN',   NULL),
 ('1f000000-0000-7000-8000-000000000003', '0a000000-0000-7000-8000-000000000001', '1a000000-0000-7000-8000-000000000005', '0d000000-0000-7000-8000-000000000001', 30.00, 54.00, date_trunc('month', CURRENT_DATE)::date,                          'OPEN',   NULL),
 ('1f000000-0000-7000-8000-000000000004', '0a000000-0000-7000-8000-000000000001', '1a000000-0000-7000-8000-000000000006', '0d000000-0000-7000-8000-000000000001', 30.00,  0.00, date_trunc('month', CURRENT_DATE)::date,                          'OPEN',   NULL),
 ('1f000000-0000-7000-8000-000000000005', '0a000000-0000-7000-8000-000000000001', '1a000000-0000-7000-8000-000000000003', '0d000000-0000-7000-8000-000000000001', 30.00,  0.00, (date_trunc('month', CURRENT_DATE) - interval '1 month')::date,   'CLOSED', now() - interval '25 days'),
 ('1f000000-0000-7000-8000-000000000006', '0a000000-0000-7000-8000-000000000002', '1a000000-0000-7000-8000-000000000007', '0d000000-0000-7000-8000-000000000004', 40.00, 32.00, date_trunc('month', CURRENT_DATE)::date,                          'OPEN',   NULL);

-- ---------------------------------------------------------------------------
-- Inventory and batches (Clínica Vida)
-- ---------------------------------------------------------------------------

INSERT INTO product (id, tenant_id, name, unit, min_stock, batch_controlled, status, on_hand) VALUES
 ('20000000-0000-7000-8000-000000000001', '0a000000-0000-7000-8000-000000000001', 'Luva de Procedimento', 'CX',  5.00, false, 'ACTIVE',   12.00),
 ('20000000-0000-7000-8000-000000000002', '0a000000-0000-7000-8000-000000000001', 'Gel Condutor',         'UN', 10.00, true,  'ACTIVE',    8.00),
 ('20000000-0000-7000-8000-000000000003', '0a000000-0000-7000-8000-000000000001', 'Álcool 70%',           'L',   3.00, true,  'ACTIVE',    6.00),
 ('20000000-0000-7000-8000-000000000004', '0a000000-0000-7000-8000-000000000001', 'Agulha Descartável',   'UN', 20.00, false, 'INACTIVE',  0.00);

INSERT INTO batch (id, tenant_id, product_id, code, expires_on, quantity, manufacturer, status) VALUES
 ('21000000-0000-7000-8000-000000000001', '0a000000-0000-7000-8000-000000000001', '20000000-0000-7000-8000-000000000002', 'GEL-2401', CURRENT_DATE + 120, 5.00, 'Multigel', 'AVAILABLE'),
 ('21000000-0000-7000-8000-000000000002', '0a000000-0000-7000-8000-000000000001', '20000000-0000-7000-8000-000000000002', 'GEL-2312', CURRENT_DATE - 10,  3.00, 'Multigel', 'AVAILABLE'),
 ('21000000-0000-7000-8000-000000000003', '0a000000-0000-7000-8000-000000000001', '20000000-0000-7000-8000-000000000003', 'ALC-2405', CURRENT_DATE + 20,  6.00, 'Farmax',   'AVAILABLE'),
 ('21000000-0000-7000-8000-000000000004', '0a000000-0000-7000-8000-000000000001', '20000000-0000-7000-8000-000000000003', 'ALC-2301', CURRENT_DATE - 60,  0.00, 'Farmax',   'DISCARDED');

INSERT INTO stock_movement (id, tenant_id, product_id, batch_id, encounter_id, movement_type, quantity, reason, recorded_by, recorded_at) VALUES
 ('22000000-0000-7000-8000-000000000001', '0a000000-0000-7000-8000-000000000001', '20000000-0000-7000-8000-000000000001', NULL,                                   NULL,                                   'INBOUND',  20.00, 'Compra inicial',      '0b000000-0000-7000-8000-000000000002', now() - interval '30 days'),
 ('22000000-0000-7000-8000-000000000002', '0a000000-0000-7000-8000-000000000001', '20000000-0000-7000-8000-000000000001', NULL,                                   '1a000000-0000-7000-8000-000000000001', 'OUTBOUND',  8.00, NULL,                  '0b000000-0000-7000-8000-000000000004', now() - interval '1 day'),
 ('22000000-0000-7000-8000-000000000003', '0a000000-0000-7000-8000-000000000001', '20000000-0000-7000-8000-000000000002', '21000000-0000-7000-8000-000000000001', NULL,                                   'INBOUND',   5.00, 'Reposição mensal',    '0b000000-0000-7000-8000-000000000002', now() - interval '25 days'),
 ('22000000-0000-7000-8000-000000000004', '0a000000-0000-7000-8000-000000000001', '20000000-0000-7000-8000-000000000002', '21000000-0000-7000-8000-000000000002', NULL,                                   'INBOUND',   3.00, 'Reposição mensal',    '0b000000-0000-7000-8000-000000000002', now() - interval '90 days'),
 ('22000000-0000-7000-8000-000000000005', '0a000000-0000-7000-8000-000000000001', '20000000-0000-7000-8000-000000000003', '21000000-0000-7000-8000-000000000003', NULL,                                   'INBOUND',   6.00, 'Reposição mensal',    '0b000000-0000-7000-8000-000000000002', now() - interval '20 days'),
 ('22000000-0000-7000-8000-000000000006', '0a000000-0000-7000-8000-000000000001', '20000000-0000-7000-8000-000000000003', '21000000-0000-7000-8000-000000000004', NULL,                                   'INBOUND',   4.00, 'Reposição mensal',    '0b000000-0000-7000-8000-000000000002', now() - interval '120 days'),
 ('22000000-0000-7000-8000-000000000007', '0a000000-0000-7000-8000-000000000001', '20000000-0000-7000-8000-000000000003', '21000000-0000-7000-8000-000000000004', NULL,                                   'DISCARD',   4.00, 'Lote vencido',        '0b000000-0000-7000-8000-000000000002', now() - interval '5 days');

-- ---------------------------------------------------------------------------
-- Session packages (Clínica Vida)
-- ---------------------------------------------------------------------------

INSERT INTO session_package (id, tenant_id, customer_id, service_id, total_sessions, used_sessions, price, expires_on, status) VALUES
 ('23000000-0000-7000-8000-000000000001', '0a000000-0000-7000-8000-000000000001', '10000000-0000-7000-8000-000000000001', '0f000000-0000-7000-8000-000000000001', 10, 1, 1500.00, CURRENT_DATE + 180, 'ACTIVE'),
 ('23000000-0000-7000-8000-000000000002', '0a000000-0000-7000-8000-000000000001', '10000000-0000-7000-8000-000000000002', '0f000000-0000-7000-8000-000000000002',  5, 0,  700.00, CURRENT_DATE + 15,  'ACTIVE'),
 ('23000000-0000-7000-8000-000000000003', '0a000000-0000-7000-8000-000000000001', '10000000-0000-7000-8000-000000000001', '0f000000-0000-7000-8000-000000000003',  3, 0,  600.00, CURRENT_DATE - 5,   'EXPIRED'),
 ('23000000-0000-7000-8000-000000000004', '0a000000-0000-7000-8000-000000000001', '10000000-0000-7000-8000-000000000002', '0f000000-0000-7000-8000-000000000001',  1, 1,  170.00, CURRENT_DATE + 60,  'EXHAUSTED'),
 ('23000000-0000-7000-8000-000000000005', '0a000000-0000-7000-8000-000000000001', '10000000-0000-7000-8000-000000000001', '0f000000-0000-7000-8000-000000000002',  4, 0,  550.00, CURRENT_DATE + 120, 'CANCELLED');

INSERT INTO package_usage (id, tenant_id, session_package_id, encounter_id, used_at) VALUES
 ('24000000-0000-7000-8000-000000000001', '0a000000-0000-7000-8000-000000000001', '23000000-0000-7000-8000-000000000001', '1a000000-0000-7000-8000-000000000003', now() - interval '1 month'),
 ('24000000-0000-7000-8000-000000000002', '0a000000-0000-7000-8000-000000000001', '23000000-0000-7000-8000-000000000004', '1a000000-0000-7000-8000-000000000006', now() - interval '15 days');

-- ---------------------------------------------------------------------------
-- Notifications
-- ---------------------------------------------------------------------------

INSERT INTO notification (id, tenant_id, appointment_id, channel, recipient, sent_at, status, reply, replied_at) VALUES
 ('25000000-0000-7000-8000-000000000001', '0a000000-0000-7000-8000-000000000001', '18000000-0000-7000-8000-000000000007', 'WHATSAPP', '41999990001',        now() - interval '1 hour',  'SENT',    NULL,       NULL),
 ('25000000-0000-7000-8000-000000000002', '0a000000-0000-7000-8000-000000000001', '18000000-0000-7000-8000-000000000001', 'EMAIL',    'ana.prado@mail.com', now() - interval '1 day',   'REPLIED', 'CONFIRMO', now() - interval '20 hours'),
 ('25000000-0000-7000-8000-000000000003', '0a000000-0000-7000-8000-000000000001', '18000000-0000-7000-8000-000000000003', 'SMS',      '41999990001',        NULL,                       'PENDING', NULL,       NULL),
 ('25000000-0000-7000-8000-000000000004', '0a000000-0000-7000-8000-000000000001', '18000000-0000-7000-8000-000000000005', 'WHATSAPP', '41999990002',        now() - interval '7 days',  'FAILED',  NULL,       NULL),
 ('25000000-0000-7000-8000-000000000005', '0a000000-0000-7000-8000-000000000002', '18000000-0000-7000-8000-00000000000a', 'WHATSAPP', '41999990005',        NULL,                       'PENDING', NULL,       NULL);

-- ---------------------------------------------------------------------------
-- Audit trail — readable through GET /api/audit-trail?entity=..&recordId=..
-- ---------------------------------------------------------------------------

INSERT INTO audit_log (id, tenant_id, user_id, entity, record_id, action, old_value, new_value, ip_address, occurred_at) VALUES
 ('28000000-0000-7000-8000-000000000001', '0a000000-0000-7000-8000-000000000001', '0b000000-0000-7000-8000-000000000002', 'Customer',  '10000000-0000-7000-8000-000000000001', 'CREATE',     NULL,                             '{"name": "Ana Prado", "phone": "41999990001"}',  '192.168.0.10', now() - interval '380 days'),
 ('28000000-0000-7000-8000-000000000002', '0a000000-0000-7000-8000-000000000001', '0b000000-0000-7000-8000-000000000003', 'Customer',  '10000000-0000-7000-8000-000000000001', 'UPDATE',     '{"email": null}',                '{"email": "ana.prado@mail.com"}',               '192.168.0.11', now() - interval '10 days'),
 ('28000000-0000-7000-8000-000000000003', '0a000000-0000-7000-8000-000000000001', '0b000000-0000-7000-8000-000000000002', 'Customer',  '10000000-0000-7000-8000-000000000004', 'DEACTIVATE', '{"status": "ACTIVE"}',           '{"status": "INACTIVE"}',                        '192.168.0.10', now() - interval '5 days'),
 ('28000000-0000-7000-8000-000000000004', '0a000000-0000-7000-8000-000000000001', '0b000000-0000-7000-8000-000000000004', 'Encounter', '1a000000-0000-7000-8000-000000000001', 'ACCESS',     NULL,                             NULL,                                            '192.168.0.12', now() - interval '1 day'),
 ('28000000-0000-7000-8000-000000000005', '0a000000-0000-7000-8000-000000000002', '0b000000-0000-7000-8000-000000000008', 'Customer',  '10000000-0000-7000-8000-000000000005', 'CREATE',     NULL,                             '{"name": "Eduarda Lopes"}',                     '192.168.0.20', now() - interval '190 days');

COMMIT;
