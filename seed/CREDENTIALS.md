# Seed data and credentials

Everything the development database holds after running `seed/seed.sql`. The
script wipes every application table and rebuilds it; the reference data owned
by the migrations (`module`, `parameter`) and Flyway's own history are left
alone.

```bash
docker exec -i clivo-postgres psql -U clivo -d clivo < seed/seed.sql
```

The script is a single transaction and starts with a `TRUNCATE`, so it is safe
to run again whenever you want a clean slate.

## Sign-in

`POST /api/session` opens a session and sets the cookie every other call needs.

```bash
curl -i -c cookies.txt -X POST http://localhost:8080/api/session \
  -H 'Content-Type: application/json' \
  -d '{"email": "maria@mail.com", "password": "12345678"}'

curl -b cookies.txt http://localhost:8080/api/session
```

The clinic is not a header or a parameter: it comes from the user who signed
in, so switching clinics means signing in as one of its users.

### Password

Every seeded user shares the password **`12345678`**.

It is eight digits and not seven: `RawPassword` refuses anything shorter than
eight characters, so `1234567` would be rejected the moment you tried to change
a password through the API. The stored hash is bcrypt cost 10, the same encoder
the application uses.

## Users

| E-mail | Password | Name | Role | Clinic | Notes |
| --- | --- | --- | --- | --- | --- |
| `admin@mail.com` | `12345678` | Administrador da Plataforma | PLATFORM_ADMIN | — | The only account that reaches `/api/platform/**` |
| `maria@mail.com` | `12345678` | Maria Souza | MANAGER | Clínica Vida | Reaches every module active in the clinic |
| `joao@mail.com` | `12345678` | João Lima | RECEPTION | Clínica Vida | Granted: dependent, sessionpackage, insurance, notification |
| `carla@mail.com` | `12345678` | Carla Nunes | PRACTITIONER | Clínica Vida | Also a practitioner; granted: dependent, inventory, batch, bodymap |
| `rafael@mail.com` | `12345678` | Rafael Dias | PRACTITIONER | Clínica Vida | Also a practitioner; no module granted |
| `pedro@mail.com` | `12345678` | Pedro Alves | ASSISTANT | Clínica Vida | Granted: inventory |
| `ines@mail.com` | `12345678` | Inês Barros | RECEPTION | Clínica Vida | **INACTIVE** — sign-in is refused |
| `lucas@mail.com` | `12345678` | Lucas Reali | MANAGER | Odonto Sorriso | |
| `ana@mail.com` | `12345678` | Ana Prado | RECEPTION | Odonto Sorriso | Granted: notification |
| `bruno@mail.com` | `12345678` | Bruno Castro | PRACTITIONER | Odonto Sorriso | Also a practitioner; granted: odontogram |
| `sofia@mail.com` | `12345678` | Sofia Martins | MANAGER | Pet Care | Clinic is SUSPENDED |
| `tiago@mail.com` | `12345678` | Tiago Rocha | PRACTITIONER | Pet Care | Also a practitioner |

## Clinics

| Id | Name | CNPJ | Status | Active modules |
| --- | --- | --- | --- | --- |
| `0a000000-0000-7000-8000-000000000001` | Clínica Vida | 11222333000181 | ACTIVE | dependent, sessionpackage, inventory, batch, insurance, notification, commission, bodymap |
| `0a000000-0000-7000-8000-000000000002` | Odonto Sorriso | 22333444000181 | ACTIVE | odontogram, commission, notification |
| `0a000000-0000-7000-8000-000000000003` | Pet Care | 33444555000181 | SUSPENDED | dependent |

Clínica Vida also carries an `odontogram` row that is switched **off**, plus the
activation and deactivation entries behind it, so
`GET /api/platform/tenants/{id}/modules/history` has something to show and
`/api/encounters` refuses an odontogram field there.

Pet Care is suspended on purpose, to exercise
`PUT /api/platform/tenants/{id}/activation`.

## Clinic parameters

Only the values that differ from the migration defaults are stored; everything
else falls back to the default and still shows up in `GET /api/parameters`.

**Clínica Vida** — `role_model=SEGREGATED`, `default_duration=45`,
`default_record_template=15000000-…-000000000001`, `reminder_lead_hours=48`,
`notification_channel=["WHATSAPP","EMAIL"]`, `expiry_alert_days=45`,
`block_expired_batch=true`, `late_fee_pct=2`, `late_interest_pct=1`,
`reschedule_window_hours=12`.

**Odonto Sorriso** — `role_model=SINGLE`, `default_duration=30`,
`default_record_template=15000000-…-000000000004`, `reminder_lead_hours=24`.

**Pet Care** — `default_duration=30`,
`default_record_template=15000000-…-000000000005`.

## Identifiers

Every id is a literal UUID version 7 shaped `XX000000-0000-7000-8000-0000000000NN`,
where the first byte names the table and the last byte numbers the row.

| Prefix | Table | Prefix | Table |
| --- | --- | --- | --- |
| `0a` | tenant | `1a` | encounter |
| `0b` | app_user | `1b` | invoice |
| `0c` | specialty | `1c` | invoice_item |
| `0d` | practitioner | `1d` | payment |
| `0e` | availability | `1e` | commission_rate |
| `0f` | service | `1f` | commission |
| `10` | customer | `20` | product |
| `11` | dependent | `21` | batch |
| `12` | consent | `22` | stock_movement |
| `13` | insurance_plan | `23` | session_package |
| `14` | customer_insurance | `24` | package_usage |
| `15` | record_template | `25` | notification |
| `16` | template_section | `26` | module_grant |
| `17` | template_field | `27` | tenant_module_history |
| `18` | appointment | `28` | audit_log |
| `19` | schedule_block | | |

So `10000000-0000-7000-8000-000000000001` is the first customer,
`0f000000-0000-7000-8000-000000000002` the second service, and so on.

## Clínica Vida

### Practitioners

| Id | Name | Specialty | User | Availability | Commission |
| --- | --- | --- | --- | --- | --- |
| `0d…01` | Carla Nunes | Fisioterapia | carla@mail.com | every day 08:00–18:00 | 30% |
| `0d…02` | Rafael Dias | Nutrição | rafael@mail.com | every day 08:00–18:00 | 25% |
| `0d…03` | Juliana Moraes | Dermatologia | — | Tuesday and Thursday 13:00–17:00 | none |

Juliana has no login and a narrow schedule on purpose: she is the case where
`GET /api/practitioners/{id}/attendance` answers "no" and where booking outside
her hours is refused. The endpoint takes the weekday by name:

```bash
curl -b cookies.txt \
  'http://localhost:8080/api/practitioners/0d000000-0000-7000-8000-000000000003/attendance?weekday=MONDAY&time=09:00'
```

### Services

| Id | Name | Duration | Price | Status |
| --- | --- | --- | --- | --- |
| `0f…01` | Sessão de Fisioterapia | 60 min | 180.00 | ACTIVE |
| `0f…02` | Consulta Nutricional | 45 min | 150.00 | ACTIVE |
| `0f…03` | Avaliação Dermatológica | 30 min | 220.00 | ACTIVE |
| `0f…04` | Massagem Terapêutica | 50 min | 130.00 | INACTIVE |

### Customers

| Id | Name | CPF | Phone | Status |
| --- | --- | --- | --- | --- |
| `10…01` | Ana Prado | 11144477735 | 41999990001 | ACTIVE |
| `10…02` | Bruno Carvalho | 12345678909 | 41999990002 | ACTIVE |
| `10…03` | Clara Ribeiro | 98765432100 | 41999990003 | ACTIVE |
| `10…04` | Diego Santos | 52998224725 | 41999990004 | INACTIVE |

Every CPF and CNPJ in the seed carries valid check digits, so the records
survive a round trip through `PUT /api/customers/{id}`.

Ana Prado's consent history has three entries — data processing granted,
marketing granted, marketing later revoked — so
`POST /api/customers/{id}/consents` appends to something.

Dependents: Miguel Prado (MINOR, under Ana) and Alzira Carvalho (ASSISTED,
under Bruno).

Insurance: Ana is enrolled in Unimed Curitiba (70%), Bruno in Bradesco Saúde
(50%). A third plan, "Plano Antigo", is INACTIVE and reimburses nothing.

### Record templates

| Id | Name | Version | Status |
| --- | --- | --- | --- |
| `15…01` | Ficha de Fisioterapia | 1 | PUBLISHED |
| `15…02` | Ficha Nutricional | 1 | PUBLISHED |
| `15…03` | Ficha de Fisioterapia | 2 | DRAFT (cloned from `15…01`) |
| `15…06` | Ficha Padrão | 1 | PUBLISHED |

`Ficha de Fisioterapia` carries a `BODY_MAP` component field that only renders
while the `bodymap` module is active. It describes itself and stores its value
the same way the `ODONTOGRAM` does, with the regions declared in the template
standing in for the tooth set. Field types are the names of the field
factories: `SHORT_TEXT`, `LONG_TEXT`, `INTEGER`, `DECIMAL`, `SCALE`, `DATE`,
`SINGLE_CHOICE` and `COMPONENT`.

### Agenda (today, relative to whenever you ran the seed)

| Id | Time | Customer | Practitioner | Status |
| --- | --- | --- | --- | --- |
| `18…01` | today 09:00–10:00 | Ana Prado | Carla | CONFIRMED |
| `18…02` | today 10:00–11:00 | Bruno Carvalho | Carla | ARRIVED |
| `18…03` | today 14:00–14:45 | Ana Prado | Rafael | SCHEDULED |
| `18…04` | yesterday 09:00–10:00 | Clara Ribeiro | Carla | COMPLETED |
| `18…05` | 7 days ago | Bruno Carvalho | Rafael | CANCELLED |
| `18…06` | 3 days ago | Ana Prado | Carla | NO_SHOW |
| `18…07` | tomorrow 09:00–10:00 | Ana Prado | Carla | SCHEDULED |

Blocks: Carla's lunch tomorrow 12:00–13:00, and a clinic-wide holiday in seven
days (no practitioner, so it blocks everyone).

### Encounters

| Id | When | Status | Template |
| --- | --- | --- | --- |
| `1a…01` | yesterday, from appointment `18…04` | COMPLETED | Fisioterapia |
| `1a…02` | two hours ago, walk-in | **DRAFT** | Nutricional |
| `1a…03` | a month ago | COMPLETED | Fisioterapia |
| `1a…04` | 10 days ago | COMPLETED | Nutricional |
| `1a…05` | 20 days ago | COMPLETED | Fisioterapia |
| `1a…06` | 15 days ago | COMPLETED | Fisioterapia |

The clinical sheet inside an encounter is role-dependent: signed in as Maria
(MANAGER) `sheet` comes back `null`, while Carla (PRACTITIONER) sees the fields
and their values. That is the profile access strategy, not missing data.

`1a…02` is the one still open: fill it with `PUT /api/encounters/{id}/record`,
draw supplies with `POST /api/encounters/{id}/supplies`, then close it with
`POST /api/encounters/{id}/completion` and watch the invoice, the commission
and the stock movement fall out of it.

### Invoices

| Id | Encounter | Gross | Discount | Net | Status | Coverage |
| --- | --- | --- | --- | --- | --- | --- |
| `1b…01` | `1a…01` | 180.00 | 0.00 | 180.00 | PAID | DIRECT |
| `1b…02` | `1a…03` | 180.00 | 180.00 | 0.00 | PAID | SESSION_PACKAGE |
| `1b…03` | `1a…04` | 150.00 | 105.00 | 45.00 | PARTIAL (**overdue**) | INSURANCE |
| `1b…04` | `1a…05` | 180.00 | 0.00 | 180.00 | OPEN (**overdue**) | DIRECT |
| `1b…05` | `1a…06` | 180.00 | 180.00 | 0.00 | PAID | SESSION_PACKAGE |

Payments: 180.00 by PIX on `1b…01`; 20.00 by credit card on `1b…03`, leaving
25.00 outstanding; and 180.00 in cash on `1b…04` that was **refunded**, which is
why that invoice is back to OPEN. Both `1b…03` and `1b…04` are past their due
date with a balance still owing, so `overdue` comes back `true` for them.

`GET /api/invoices/report?from=…&to=…` over the last 40 days reports 4 invoices,
690.00 gross, 465.00 discounted, 225.00 net, 20.00 received and 205.00
outstanding.

### Commissions

Carla 30%, Rafael 25%. The current month holds four open entries (54.00, 11.25,
54.00 and 0.00 — the last one because a package-covered invoice nets zero), and
the previous month holds one already CLOSED, so
`POST /api/commissions/closing` has both cases to deal with.

### Inventory

| Id | Product | Unit | Minimum | On hand | Batches |
| --- | --- | --- | --- | --- | --- |
| `20…01` | Luva de Procedimento | CX | 5 | 12 | no |
| `20…02` | Gel Condutor | UN | 10 | **8 (below the minimum)** | yes |
| `20…03` | Álcool 70% | L | 3 | 6 | yes |
| `20…04` | Agulha Descartável | UN | 20 | 0 | INACTIVE |

Batches: `GEL-2401` expires in 120 days, `GEL-2312` **expired 10 days ago and is
still available** (that is what `/api/batches/awaiting-discard` and the expired
batch policy are for), `ALC-2405` expires in 20 days — inside the clinic's
45-day alert window — and `ALC-2301` is already DISCARDED.

`on_hand` reconciles with the stock movements, and with the batch quantities for
the two batch-controlled products.

### Session packages

| Id | Customer | Service | Total | Used | Status |
| --- | --- | --- | --- | --- | --- |
| `23…01` | Ana Prado | Fisioterapia | 10 | 1 | ACTIVE |
| `23…02` | Bruno Carvalho | Nutricional | 5 | 0 | ACTIVE (expires in 15 days) |
| `23…03` | Ana Prado | Dermatológica | 3 | 0 | EXPIRED |
| `23…04` | Bruno Carvalho | Fisioterapia | 1 | 1 | EXHAUSTED |
| `23…05` | Ana Prado | Nutricional | 4 | 0 | CANCELLED |

### Notifications

One SENT, one REPLIED (`CONFIRMO`), one PENDING and one FAILED, so
`/api/notifications/pending` and `/api/notifications/dispatch` both have work.

## Odonto Sorriso

Bruno Castro (`0d…04`, 40% commission) attends every day 08:00–18:00. Services:
Limpeza Dental (`0f…05`, 120.00) and Manutenção de Aparelho (`0f…06`, 90.00).
Customers: Eduarda Lopes (`10…05`) and Felipe Moura (`10…06`).

`Ficha Odontológica` (`15…04`) carries the `ODONTOGRAM` component. The component
describes itself in the assembled record: every permanent tooth in FDI notation
with its arch, quadrant, position and faces, plus the vocabulary of conditions
it accepts. Its value is a list of markings, each naming a tooth, optionally a
face, a condition code and free text. Encounter `1a…07` carries two markings.

Invoice `1b…06` is OPEN for 80.00 after a 10.00 loyalty discount, and encounter
`1a…08` is still a DRAFT.

Because `inventory` and `insurance` are **not** active here, this clinic is the
place to see the module guard answer 404/403 on `/api/products` and
`/api/insurance-plans`.

## Pet Care

Suspended clinic with the `dependent` module active. Tiago Rocha (`0d…05`)
attends every day; Gabriela Nogueira (`10…07`) answers for two animals, Thor
(`11…03`, Golden Retriever) and Mel (`11…04`, cat). One appointment today at
11:00 and one open encounter (`1a…09`) on `Ficha Veterinária`.

## Audit trail

Five entries, readable through `GET /api/audit-trail?entity=Customer&recordId=…`:
the creation, an update and a deactivation of customers in Clínica Vida, one
`ACCESS` on encounter `1a…01`, and one creation in Odonto Sorriso.

## What is deliberately left empty

`attachment` — the table comes from the migrations but no Java code maps it, so
nothing would read a seeded row.

`encounter.dependent_id`, `appointment.dependent_id`, `encounter.signed_by` and
`invoice.insurance_plan_id` are columns the entities do not map either, so they
are left NULL rather than filled with values the API would never show.

Record templates are always bound to a clinic. The schema allows a global one
(`tenant_id IS NULL`), but `RecordTemplate` is `@TenantId`-scoped, so a global
row would be invisible to every query.
