# Clivo API

Multi-tenant back end for clinic management: customers, practitioners and their
availability, a service catalog, scheduling, encounters with a configurable
record sheet, and billing with invoices and payments.

Every clinic is a tenant, and each tenant decides what the system does for it
through three configuration mechanisms:

- **Module activation** — optional features (dependents, inventory, batches,
  session packages, insurance, notifications, commissions) are turned on per
  tenant. A feature that is off leaves no trace: its endpoints answer `404`,
  never `403`, and the capabilities resource does not list it.
- **Clinic parameters** — behavior that varies by clinic (reschedule window,
  access policy, and so on) is data, not code.
- **Record templates** — the encounter record sheet is assembled from sections
  and fields declared per tenant, including special components.

The core never imports a module. When it needs module behavior it declares an
interface in `common/extension` and injects `List<Interface>`; an inactive
module registers no bean and simply is not in the list.

## Stack

| | |
|---|---|
| Language | Java 25 |
| Framework | Spring Boot 4.1 · Spring Modulith 2.1 |
| Persistence | Spring Data JPA · Hibernate (`ddl-auto: validate`) |
| Database | PostgreSQL 18 |
| Migrations | Flyway |
| Build | Gradle (Kotlin DSL) with wrapper |
| Docs | springdoc-openapi |

The schema relies on PostgreSQL-only features (`EXCLUDE USING gist`, partial
indexes, extensions). There is no H2 fallback — a real PostgreSQL is required
to run and to test.

## Requirements

- JDK 25
- Docker and Docker Compose
- No Gradle install needed — use `./gradlew`

## Getting started

Copy the environment template:

```bash
cp .env.example .env
```

### Everything in Docker

```bash
docker compose up --build
```

The API listens on `http://localhost:8080` with the `docker` profile.

### Local development

Start only the database, then run the app from the workspace:

```bash
docker compose up -d postgres
./gradlew bootRun
```

The default profile is `dev`, which points at `localhost:5432/clivo` and logs
formatted SQL. Flyway applies `src/main/resources/db/migration` on startup.

### API documentation

The OpenAPI document is generated from the code, never written by hand. Every
endpoint declares its own `operationId`, summary and tag; the request and
response schemas come from the records, and their constraints from Bean
Validation.

With the application running:

- Swagger UI — `http://localhost:8080/swagger-ui.html`
- OpenAPI document — `http://localhost:8080/v3/api-docs`

`openapi.json` at the repository root is the same document, committed so a
client can be generated without the API on air. It is written by
`OpenApiSpecificationTest`, so `./gradlew test` refreshes it and a contract
change shows up in the diff of the commit that caused it.

That test also guards the contract: it fails when an endpoint declares no
`operationId`, or when two endpoints answer to the same one. Without that
guard, a generator names its functions after the Java methods, and the
eleventh `findOne` becomes `findOne_10`.

#### Generating a typed client

Point the generator at the committed document. With [Kubb](https://kubb.dev):

```ts
// kubb.config.ts
import { defineConfig } from '@kubb/core'
import { pluginOas } from '@kubb/plugin-oas'
import { pluginTs } from '@kubb/plugin-ts'
import { pluginClient } from '@kubb/plugin-client'

export default defineConfig({
  input: { path: '../clivo-api/openapi.json' },
  output: { path: './src/gen' },
  plugins: [pluginOas(), pluginTs(), pluginClient({ group: { type: 'tag' } })],
})
```

Grouping by tag mirrors the API's own division — `Customers`, `Billing`,
`Encounters`, `Inventory` — so the generated files land one per subject.

Authentication is the session cookie from `POST /api/session`; send requests
with credentials included. Endpoints of an optional module answer `404` while
the clinic has not activated it, so a client should read
`GET /api/capabilities` before offering those features.

## Environment variables

| Variable | Default | Used by |
|---|---|---|
| `POSTGRES_DB` | `clivo` | Compose |
| `POSTGRES_USER` | `clivo` | Compose |
| `POSTGRES_PASSWORD` | `clivo` | Compose |
| `POSTGRES_PORT` | `5432` | Compose |
| `API_PORT` | `8080` | Compose |
| `SPRING_PROFILES_ACTIVE` | `dev` locally, `docker` in Compose | Application |
| `DATASOURCE_URL` | per profile | Application |
| `DATASOURCE_USERNAME` | `clivo` | Application |
| `DATASOURCE_PASSWORD` | `clivo` | Application |

## Build and test

The suite talks to a real database on the `test` profile
(`localhost:5432/clivo_test`). That database is created by the Compose init
script, so bring PostgreSQL up first:

```bash
docker compose up -d postgres
./gradlew build
```

Useful targets:

```bash
./gradlew test                                # full suite
./gradlew test --tests "*ModularityTest"      # module boundaries only
```

`ModularityTest` verifies the Spring Modulith boundaries and generates the
module documentation. A red suite is never committed.

## Project layout

```
com.example.clivoapi
├── common          # open module: tenant, audit, exception, money, time, extension points
├── configuration   # module activation, clinic parameters, record templates, capabilities
├── core            # access, customer, practitioner, catalog, scheduling, encounter, billing
├── patterns        # Chain, Abstract Factory and Strategy implementations
└── modules         # optional features, activated per tenant
```

Dependencies are declared and enforced:

- `common` is an open module and depends on nothing.
- `configuration` and `core` may only see `common`.
- `patterns` sees `common` plus named interfaces of `core` and `configuration`.
- `modules` may see everything; nothing may see `modules`.

Inside every module, only the root package is public API. Repositories,
controllers and implementations live in `internal`.

There is no `reporting` module. The only report the product needs is the
financial one, and it reads nothing but invoices: `BillingReport`,
`BillingTotals` and `ReportPeriod` therefore live in `core/billing`, beside the
data they summarize, and are served by `/api/invoices/report`. A separate module
would own no data of its own, would have to depend on billing to say anything,
and would move the `RoleAccess` check that restricts the financial figures to
management away from the module that enforces it. When a report spans more than
billing, it earns its own module then.

## Conventions

- Code, packages, classes and methods are written in English.
- No comments in code. If a fragment needs one, it needs a better name or to be
  split. The only exception is `package-info.java`, which carries annotations.
- Business rules live in the entity, not in the service. The service
  orchestrates, loads, saves and owns the transaction.
- Hibernate never generates schema. Every change is a Flyway migration.
- Readability over performance. Optimization needs a measurement behind it.
- Commits and branches follow Conventional Commits (`feat(billing): ...`,
  `fix/login-race-condition`).

Commit as soon as a task in `todo.md` is done — one commit per task — with the
task ticked and the decision records written for it.

## Working with coding agents

This repository is set up for agent-assisted development. If you are going to
use an agent (Claude Code, Cursor, Codex CLI, Gemini CLI), install both tools
below before you start. They are what keep an agent from re-deriving the
codebase and from re-litigating decisions that were already made.

### cortex — decision memory

Records the *why* behind technical decisions and makes it searchable. Decision
files live in `.cortex/decisions/` and are committed alongside the code they
explain.

Install (macOS and Linux):

```bash
curl -fsSL https://raw.githubusercontent.com/lucasreali/cortex-cli/main/install.sh | sh
```

Install (Windows, PowerShell):

```powershell
irm https://raw.githubusercontent.com/lucasreali/cortex-cli/main/install.ps1 | iex
```

Then, from the repository root:

```bash
cortex init
cortex install --yes        # registers the MCP server with the detected agents
cortex embed --fetch-model  # downloads the embedding model for semantic search
cortex index                # builds the code index
```

Optionally, reconcile the decision store automatically on branch changes and
commits:

```bash
cortex install --git-hooks
```

If a semantic search comes back empty, embeddings are probably missing — run
`cortex embed --missing`.

### codegraph — code intelligence

Keeps a knowledge graph of every symbol, edge and file, so an agent can ask
where something is and what it affects instead of grepping the tree.

Install (macOS and Linux):

```bash
curl -fsSL https://raw.githubusercontent.com/colbymchenry/codegraph/main/install.sh | sh
```

Install (Windows, PowerShell):

```powershell
irm https://raw.githubusercontent.com/colbymchenry/codegraph/main/install.ps1 | iex
```

Then:

```bash
codegraph install   # configures the MCP server for the detected agents
codegraph init      # creates .codegraph/ and builds the graph
```

The index auto-syncs on file changes afterwards. `codegraph ui` opens a graph
browser at `http://127.0.0.1:4747`.

Both `.cortex/` internals and `.codegraph/` are local caches; only
`.cortex/config` and `.cortex/decisions/` are versioned.

## Roadmap

`todo.md` holds the build backlog by phase and is the source of truth for what
is done and what is next. Phases 0 to 5 (foundation, configuration, core
registries, scheduling, encounter and record sheet, billing) are complete;
phase 6 (optional modules) and phase 7 (reuse and architecture verification)
are in progress.
