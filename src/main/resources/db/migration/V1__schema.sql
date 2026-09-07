CREATE EXTENSION IF NOT EXISTS btree_gist;
CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE TABLE tenant (
    id              UUID         PRIMARY KEY,
    code            VARCHAR(20)  NOT NULL UNIQUE,
    name            VARCHAR(120) NOT NULL,
    legal_name      VARCHAR(160),
    tax_id          VARCHAR(14),
    segment         VARCHAR(60),
    status          VARCHAR(12)  NOT NULL DEFAULT 'ACTIVE'
                    CHECK (status IN ('ACTIVE','SUSPENDED','CLOSED')),
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE module (
    code                VARCHAR(4)   PRIMARY KEY,
    name                VARCHAR(60)  NOT NULL,
    description         TEXT         NOT NULL,
    requires_module     VARCHAR(4)   REFERENCES module(code),
    CHECK (requires_module IS NULL OR requires_module <> code)
);

CREATE TABLE tenant_module (
    tenant_id       UUID         NOT NULL REFERENCES tenant(id),
    module_code     VARCHAR(4)   NOT NULL REFERENCES module(code),
    enabled         BOOLEAN      NOT NULL DEFAULT false,
    enabled_at      TIMESTAMPTZ,
    enabled_by      UUID,
    PRIMARY KEY (tenant_id, module_code)
);

CREATE TABLE parameter (
    code            VARCHAR(40)  PRIMARY KEY,
    name            VARCHAR(80)  NOT NULL,
    data_type       VARCHAR(12)  NOT NULL
                    CHECK (data_type IN ('INTEGER','DECIMAL','BOOLEAN','ENUM','LIST','REF')),
    min_value       NUMERIC,
    max_value       NUMERIC,
    options         JSONB,
    default_value   TEXT         NOT NULL,
    module_code     VARCHAR(4)   REFERENCES module(code)
);

CREATE TABLE tenant_parameter (
    tenant_id       UUID        NOT NULL REFERENCES tenant(id),
    parameter_code  VARCHAR(40) NOT NULL REFERENCES parameter(code),
    value           TEXT        NOT NULL,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_by      UUID,
    PRIMARY KEY (tenant_id, parameter_code)
);

CREATE TABLE app_user (
    id              UUID         PRIMARY KEY,
    tenant_id       UUID         REFERENCES tenant(id),
    name            VARCHAR(120) NOT NULL,
    email           VARCHAR(160) NOT NULL,
    password_hash   VARCHAR(120) NOT NULL,
    user_role       VARCHAR(20)  NOT NULL
                    CHECK (user_role IN ('RECEPTION','PRACTITIONER','ASSISTANT',
                                         'MANAGER','PLATFORM_ADMIN')),
    status          VARCHAR(10)  NOT NULL DEFAULT 'ACTIVE'
                    CHECK (status IN ('ACTIVE','INACTIVE')),
    last_login_at   TIMESTAMPTZ,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CHECK ((user_role = 'PLATFORM_ADMIN') = (tenant_id IS NULL))
);

CREATE UNIQUE INDEX ux_app_user_email
    ON app_user (COALESCE(tenant_id, '00000000-0000-0000-0000-000000000000'::uuid), lower(email));

CREATE TABLE customer (
    id                      UUID         PRIMARY KEY,
    tenant_id               UUID         NOT NULL REFERENCES tenant(id),
    name                    VARCHAR(120) NOT NULL,
    national_id             VARCHAR(11),
    birth_date              DATE,
    phone                   VARCHAR(20)  NOT NULL,
    email                   VARCHAR(160),
    postal_code             VARCHAR(8),
    street                  VARCHAR(160),
    status                  VARCHAR(10)  NOT NULL DEFAULT 'ACTIVE'
                            CHECK (status IN ('ACTIVE','INACTIVE')),
    deactivation_reason     VARCHAR(200),
    created_at              TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at              TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_by              UUID         REFERENCES app_user(id),
    CHECK (status = 'ACTIVE' OR deactivation_reason IS NOT NULL)
);

CREATE UNIQUE INDEX ux_customer_national_id
    ON customer (tenant_id, national_id) WHERE national_id IS NOT NULL;
CREATE INDEX ix_customer_name ON customer USING gin (name gin_trgm_ops);
CREATE INDEX ix_customer_tenant ON customer (tenant_id, status);

CREATE TABLE dependent (
    id                  UUID         PRIMARY KEY,
    tenant_id           UUID         NOT NULL REFERENCES tenant(id),
    customer_id         UUID         NOT NULL REFERENCES customer(id),
    name                VARCHAR(120) NOT NULL,
    dependent_type      VARCHAR(20)  NOT NULL
                        CHECK (dependent_type IN ('ANIMAL','MINOR','ASSISTED')),
    birth_date          DATE,
    attributes          JSONB        NOT NULL DEFAULT '{}'::jsonb,
    status              VARCHAR(10)  NOT NULL DEFAULT 'ACTIVE',
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX ix_dependent_customer ON dependent (tenant_id, customer_id);
CREATE INDEX ix_dependent_attributes ON dependent USING gin (attributes);

CREATE TABLE specialty (
    id          UUID         PRIMARY KEY,
    tenant_id   UUID         NOT NULL REFERENCES tenant(id),
    name        VARCHAR(80)  NOT NULL,
    UNIQUE (tenant_id, name)
);

CREATE TABLE practitioner (
    id              UUID         PRIMARY KEY,
    tenant_id       UUID         NOT NULL REFERENCES tenant(id),
    user_id         UUID         REFERENCES app_user(id),
    name            VARCHAR(120) NOT NULL,
    license_number  VARCHAR(30),
    specialty_id    UUID         REFERENCES specialty(id),
    status          VARCHAR(10)  NOT NULL DEFAULT 'ACTIVE',
    UNIQUE (tenant_id, user_id)
);

CREATE TABLE service (
    id              UUID          PRIMARY KEY,
    tenant_id       UUID          NOT NULL REFERENCES tenant(id),
    name            VARCHAR(120)  NOT NULL,
    duration_min    SMALLINT      NOT NULL CHECK (duration_min BETWEEN 5 AND 480),
    price           NUMERIC(10,2) NOT NULL CHECK (price >= 0),
    specialty_id    UUID          REFERENCES specialty(id),
    status          VARCHAR(10)   NOT NULL DEFAULT 'ACTIVE',
    UNIQUE (tenant_id, name)
);

CREATE TABLE insurance_plan (
    id                  UUID         PRIMARY KEY,
    tenant_id           UUID         NOT NULL REFERENCES tenant(id),
    name                VARCHAR(120) NOT NULL,
    reimbursement_pct   NUMERIC(5,2) CHECK (reimbursement_pct BETWEEN 0 AND 100),
    status              VARCHAR(10)  NOT NULL DEFAULT 'ACTIVE',
    UNIQUE (tenant_id, name)
);

CREATE TABLE customer_insurance (
    tenant_id           UUID        NOT NULL REFERENCES tenant(id),
    customer_id         UUID        NOT NULL REFERENCES customer(id),
    insurance_plan_id   UUID        NOT NULL REFERENCES insurance_plan(id),
    member_number       VARCHAR(40) NOT NULL,
    PRIMARY KEY (customer_id, insurance_plan_id)
);

CREATE TABLE availability (
    id              UUID     PRIMARY KEY,
    tenant_id       UUID     NOT NULL REFERENCES tenant(id),
    practitioner_id UUID     NOT NULL REFERENCES practitioner(id),
    weekday         SMALLINT NOT NULL CHECK (weekday BETWEEN 0 AND 6),
    start_time      TIME     NOT NULL,
    end_time        TIME     NOT NULL,
    CHECK (end_time > start_time)
);

CREATE TABLE schedule_block (
    id              UUID         PRIMARY KEY,
    tenant_id       UUID         NOT NULL REFERENCES tenant(id),
    practitioner_id UUID         REFERENCES practitioner(id),
    starts_at       TIMESTAMPTZ  NOT NULL,
    ends_at         TIMESTAMPTZ  NOT NULL,
    reason          VARCHAR(120) NOT NULL,
    CHECK (ends_at > starts_at)
);

CREATE INDEX ix_schedule_block_period
    ON schedule_block (tenant_id, practitioner_id, starts_at, ends_at);

CREATE TABLE appointment (
    id              UUID         PRIMARY KEY,
    tenant_id       UUID         NOT NULL REFERENCES tenant(id),
    customer_id     UUID         NOT NULL REFERENCES customer(id),
    dependent_id    UUID         REFERENCES dependent(id),
    practitioner_id UUID         NOT NULL REFERENCES practitioner(id),
    service_id      UUID         NOT NULL REFERENCES service(id),
    starts_at       TIMESTAMPTZ  NOT NULL,
    ends_at         TIMESTAMPTZ  NOT NULL,
    status          VARCHAR(20)  NOT NULL DEFAULT 'SCHEDULED'
                    CHECK (status IN ('SCHEDULED','CONFIRMED','ARRIVED',
                                      'IN_PROGRESS','COMPLETED',
                                      'CANCELLED','NO_SHOW')),
    reason          VARCHAR(200),
    source          VARCHAR(20)  NOT NULL DEFAULT 'RECEPTION',
    created_by      UUID         REFERENCES app_user(id),
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CHECK (ends_at > starts_at),
    CHECK (status NOT IN ('CANCELLED','NO_SHOW') OR reason IS NOT NULL)
);

ALTER TABLE appointment ADD CONSTRAINT ux_appointment_no_overlap
    EXCLUDE USING gist (
        tenant_id       WITH =,
        practitioner_id WITH =,
        tstzrange(starts_at, ends_at, '[)') WITH &&
    ) WHERE (status IN ('SCHEDULED','CONFIRMED','ARRIVED','IN_PROGRESS'));

CREATE INDEX ix_appointment_day ON appointment (tenant_id, starts_at)
    WHERE status <> 'CANCELLED';

CREATE TABLE record_template (
    id              UUID         PRIMARY KEY,
    tenant_id       UUID         REFERENCES tenant(id),
    name            VARCHAR(120) NOT NULL,
    version         SMALLINT     NOT NULL DEFAULT 1,
    status          VARCHAR(12)  NOT NULL DEFAULT 'DRAFT'
                    CHECK (status IN ('DRAFT','PUBLISHED','RETIRED')),
    cloned_from     UUID         REFERENCES record_template(id),
    requires_module VARCHAR(4)   REFERENCES module(code),
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX ux_record_template
    ON record_template (COALESCE(tenant_id, '00000000-0000-0000-0000-000000000000'::uuid), name, version);

CREATE TABLE template_section (
    id                  UUID         PRIMARY KEY,
    record_template_id  UUID         NOT NULL REFERENCES record_template(id) ON DELETE CASCADE,
    name                VARCHAR(80)  NOT NULL,
    sort_order          SMALLINT     NOT NULL,
    UNIQUE (record_template_id, sort_order)
);

CREATE TABLE template_field (
    id                  UUID         PRIMARY KEY,
    template_section_id UUID         NOT NULL REFERENCES template_section(id) ON DELETE CASCADE,
    code                VARCHAR(40)  NOT NULL,
    label               VARCHAR(80)  NOT NULL,
    field_type          VARCHAR(20)  NOT NULL
                        CHECK (field_type IN ('SHORT_TEXT','LONG_TEXT','INTEGER','DECIMAL',
                                              'DATE','SINGLE_CHOICE','MULTI_CHOICE',
                                              'BOOLEAN','SCALE','ATTACHMENT','COMPONENT')),
    component           VARCHAR(30),
    required            BOOLEAN      NOT NULL DEFAULT false,
    sort_order          SMALLINT     NOT NULL,
    options             JSONB,
    validation          JSONB,
    requires_module     VARCHAR(4)   REFERENCES module(code),
    UNIQUE (template_section_id, code),
    CHECK ((field_type = 'COMPONENT') = (component IS NOT NULL))
);

CREATE TABLE encounter (
    id                  UUID         PRIMARY KEY,
    tenant_id           UUID         NOT NULL REFERENCES tenant(id),
    appointment_id      UUID         REFERENCES appointment(id),
    customer_id         UUID         NOT NULL REFERENCES customer(id),
    dependent_id        UUID         REFERENCES dependent(id),
    practitioner_id     UUID         NOT NULL REFERENCES practitioner(id),
    record_template_id  UUID         NOT NULL REFERENCES record_template(id),
    field_values        JSONB        NOT NULL DEFAULT '{}'::jsonb,
    started_at          TIMESTAMPTZ  NOT NULL DEFAULT now(),
    completed_at        TIMESTAMPTZ,
    status              VARCHAR(12)  NOT NULL DEFAULT 'DRAFT'
                        CHECK (status IN ('DRAFT','COMPLETED','CANCELLED')),
    signed_by           UUID         REFERENCES app_user(id)
);

CREATE INDEX ix_encounter_values ON encounter USING gin (field_values jsonb_path_ops);
CREATE INDEX ix_encounter_customer ON encounter (tenant_id, customer_id, started_at DESC);

CREATE TABLE attachment (
    id              UUID         PRIMARY KEY,
    tenant_id       UUID         NOT NULL REFERENCES tenant(id),
    encounter_id    UUID         NOT NULL REFERENCES encounter(id),
    file_name       VARCHAR(160) NOT NULL,
    mime_type       VARCHAR(80)  NOT NULL,
    object_key      VARCHAR(255) NOT NULL,
    size_bytes      BIGINT       NOT NULL,
    uploaded_at     TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE invoice (
    id                  UUID          PRIMARY KEY,
    tenant_id           UUID          NOT NULL REFERENCES tenant(id),
    encounter_id        UUID          NOT NULL REFERENCES encounter(id),
    customer_id         UUID          NOT NULL REFERENCES customer(id),
    insurance_plan_id   UUID          REFERENCES insurance_plan(id),
    gross_amount        NUMERIC(10,2) NOT NULL CHECK (gross_amount >= 0),
    discount            NUMERIC(10,2) NOT NULL DEFAULT 0 CHECK (discount >= 0),
    net_amount          NUMERIC(10,2) NOT NULL CHECK (net_amount >= 0),
    due_date            DATE          NOT NULL,
    status              VARCHAR(12)   NOT NULL DEFAULT 'OPEN'
                        CHECK (status IN ('OPEN','PAID','PARTIAL','CANCELLED')),
    created_at          TIMESTAMPTZ   NOT NULL DEFAULT now()
);

CREATE INDEX ix_invoice_status ON invoice (tenant_id, status, due_date);

CREATE TABLE invoice_item (
    id              UUID          PRIMARY KEY,
    invoice_id      UUID          NOT NULL REFERENCES invoice(id) ON DELETE CASCADE,
    service_id      UUID          REFERENCES service(id),
    description     VARCHAR(160)  NOT NULL,
    quantity        NUMERIC(8,2)  NOT NULL DEFAULT 1,
    unit_price      NUMERIC(10,2) NOT NULL
);

CREATE TABLE payment (
    id              UUID          PRIMARY KEY,
    tenant_id       UUID          NOT NULL REFERENCES tenant(id),
    invoice_id      UUID          NOT NULL REFERENCES invoice(id),
    amount          NUMERIC(10,2) NOT NULL CHECK (amount > 0),
    method          VARCHAR(20)   NOT NULL
                    CHECK (method IN ('CASH','PIX','DEBIT','CREDIT','INSURANCE')),
    paid_at         TIMESTAMPTZ   NOT NULL DEFAULT now(),
    recorded_by     UUID          NOT NULL REFERENCES app_user(id)
);

CREATE TABLE session_package (
    id              UUID          PRIMARY KEY,
    tenant_id       UUID          NOT NULL REFERENCES tenant(id),
    customer_id     UUID          NOT NULL REFERENCES customer(id),
    service_id      UUID          NOT NULL REFERENCES service(id),
    total_sessions  SMALLINT      NOT NULL CHECK (total_sessions > 0),
    used_sessions   SMALLINT      NOT NULL DEFAULT 0 CHECK (used_sessions >= 0),
    price           NUMERIC(10,2) NOT NULL,
    expires_on      DATE          NOT NULL,
    status          VARCHAR(12)   NOT NULL DEFAULT 'ACTIVE'
                    CHECK (status IN ('ACTIVE','EXHAUSTED','EXPIRED','CANCELLED')),
    CHECK (used_sessions <= total_sessions)
);

CREATE TABLE package_usage (
    id                  UUID        PRIMARY KEY,
    tenant_id           UUID        NOT NULL REFERENCES tenant(id),
    session_package_id  UUID        NOT NULL REFERENCES session_package(id),
    encounter_id        UUID        NOT NULL REFERENCES encounter(id),
    used_at             TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (encounter_id)
);

CREATE TABLE commission (
    id              UUID          PRIMARY KEY,
    tenant_id       UUID          NOT NULL REFERENCES tenant(id),
    encounter_id    UUID          NOT NULL REFERENCES encounter(id),
    practitioner_id UUID          NOT NULL REFERENCES practitioner(id),
    percentage      NUMERIC(5,2)  NOT NULL CHECK (percentage BETWEEN 0 AND 100),
    amount          NUMERIC(10,2) NOT NULL,
    period          DATE          NOT NULL,
    UNIQUE (encounter_id, practitioner_id)
);

CREATE TABLE product (
    id                  UUID          PRIMARY KEY,
    tenant_id           UUID          NOT NULL REFERENCES tenant(id),
    name                VARCHAR(120)  NOT NULL,
    unit                VARCHAR(10)   NOT NULL,
    min_stock           NUMERIC(10,2) NOT NULL DEFAULT 0,
    batch_controlled    BOOLEAN       NOT NULL DEFAULT false,
    status              VARCHAR(10)   NOT NULL DEFAULT 'ACTIVE',
    UNIQUE (tenant_id, name)
);

CREATE TABLE batch (
    id              UUID          PRIMARY KEY,
    tenant_id       UUID          NOT NULL REFERENCES tenant(id),
    product_id      UUID          NOT NULL REFERENCES product(id),
    code            VARCHAR(40)   NOT NULL,
    expires_on      DATE          NOT NULL,
    quantity        NUMERIC(10,2) NOT NULL CHECK (quantity >= 0),
    manufacturer    VARCHAR(80),
    status          VARCHAR(12)   NOT NULL DEFAULT 'AVAILABLE'
                    CHECK (status IN ('AVAILABLE','RESERVED','DISCARDED')),
    UNIQUE (tenant_id, product_id, code)
);

CREATE INDEX ix_batch_expiry ON batch (tenant_id, product_id, expires_on)
    WHERE status = 'AVAILABLE';

CREATE TABLE stock_movement (
    id              UUID          PRIMARY KEY,
    tenant_id       UUID          NOT NULL REFERENCES tenant(id),
    product_id      UUID          NOT NULL REFERENCES product(id),
    batch_id        UUID          REFERENCES batch(id),
    encounter_id    UUID          REFERENCES encounter(id),
    movement_type   VARCHAR(12)   NOT NULL
                    CHECK (movement_type IN ('INBOUND','OUTBOUND','ADJUSTMENT','DISCARD')),
    quantity        NUMERIC(10,2) NOT NULL CHECK (quantity > 0),
    reason          VARCHAR(160),
    recorded_by     UUID          NOT NULL REFERENCES app_user(id),
    recorded_at     TIMESTAMPTZ   NOT NULL DEFAULT now(),
    CHECK (movement_type <> 'OUTBOUND' OR encounter_id IS NOT NULL OR reason IS NOT NULL)
);

CREATE INDEX ix_stock_movement_encounter ON stock_movement (tenant_id, encounter_id);

CREATE TABLE notification (
    id              UUID         PRIMARY KEY,
    tenant_id       UUID         NOT NULL REFERENCES tenant(id),
    appointment_id  UUID         NOT NULL REFERENCES appointment(id),
    channel         VARCHAR(10)  NOT NULL CHECK (channel IN ('SMS','WHATSAPP','EMAIL')),
    recipient       VARCHAR(160) NOT NULL,
    sent_at         TIMESTAMPTZ,
    status          VARCHAR(12)  NOT NULL DEFAULT 'PENDING'
                    CHECK (status IN ('PENDING','SENT','FAILED','REPLIED')),
    reply           VARCHAR(20),
    replied_at      TIMESTAMPTZ
);

CREATE INDEX ix_notification_pending ON notification (tenant_id, status, sent_at);

CREATE TABLE audit_log (
    id              UUID         PRIMARY KEY,
    tenant_id       UUID         NOT NULL REFERENCES tenant(id),
    user_id         UUID         NOT NULL REFERENCES app_user(id),
    entity          VARCHAR(40)  NOT NULL,
    record_id       UUID         NOT NULL,
    action          VARCHAR(10)  NOT NULL
                    CHECK (action IN ('CREATE','UPDATE','DEACTIVATE','ACCESS')),
    old_value       JSONB,
    new_value       JSONB,
    ip_address      INET,
    occurred_at     TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX ix_audit_log_record
    ON audit_log (tenant_id, entity, record_id, occurred_at DESC);

CREATE TABLE consent (
    id              UUID         PRIMARY KEY,
    tenant_id       UUID         NOT NULL REFERENCES tenant(id),
    customer_id     UUID         NOT NULL REFERENCES customer(id),
    purpose         VARCHAR(60)  NOT NULL,
    granted         BOOLEAN      NOT NULL,
    source          VARCHAR(30)  NOT NULL,
    recorded_at     TIMESTAMPTZ  NOT NULL DEFAULT now()
);

INSERT INTO module (code, name, description, requires_module) VALUES
 ('M01','Dependentes do cliente','Seção de dependentes no cadastro e atendimento em nome do dependente', NULL),
 ('M02','Pacotes de sessões','Venda de pacote e abatimento de saldo por atendimento', NULL),
 ('M03','Controle de estoque','Produtos, saldo e baixa vinculada ao atendimento', NULL),
 ('M04','Lote e validade','Campo de lote na ficha, alerta de vencimento e recusa de baixa', 'M03'),
 ('M05','Convênios','Convênio e carteirinha no cadastro, tabela de repasse', NULL),
 ('M06','Notificações','Lembrete de consulta e confirmação pelo cliente', NULL),
 ('M07','Comissionamento','Percentual por profissional e relatório de fechamento', NULL);

INSERT INTO parameter (code, name, data_type, min_value, max_value, options, default_value, module_code) VALUES
 ('role_model','Modelo de perfis','ENUM',NULL,NULL,'["SINGLE","SEGREGATED"]','SEGREGATED',NULL),
 ('default_duration','Duração padrão do atendimento','INTEGER',15,120,NULL,'30',NULL),
 ('reminder_lead_hours','Antecedência do lembrete (h)','INTEGER',1,72,NULL,'24','M06'),
 ('notification_channel','Canal de notificação','LIST',NULL,NULL,'["SMS","WHATSAPP","EMAIL"]','["WHATSAPP"]','M06'),
 ('reschedule_window_hours','Prazo de reagendamento (h)','INTEGER',0,72,NULL,'24',NULL),
 ('late_interest_pct','Juros de atraso ao mês (%)','DECIMAL',0,10,NULL,'1',NULL),
 ('late_fee_pct','Multa por atraso (%)','DECIMAL',0,10,NULL,'2',NULL),
 ('expiry_alert_days','Antecedência do alerta de validade (dias)','INTEGER',7,180,NULL,'30','M04'),
 ('block_expired_batch','Bloquear aplicação de lote vencido','BOOLEAN',NULL,NULL,NULL,'true','M04'),
 ('default_record_template','Modelo de ficha padrão','REF',NULL,NULL,NULL,'00000000-0000-0000-0000-000000000000',NULL);
