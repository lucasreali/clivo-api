<!-- cortex:begin -->
## Cortex — decision memory

This project records its technical decisions with cortex (MCP server
`cortex`, tools: `save_decision`, `save_session_summary`,
`get_context`, `get_impact`, `search`, `search_all_projects`).

- Before proposing an approach or changing existing behavior, call
  `get_context` with your intent (or `search` with keywords) — a past
  decision may already govern this code.
- Before reworking code a decision anchors, call `get_impact` with the
  decision id to see everything the change touches.
- When the user confirms a non-obvious decision, save it with
  `save_decision`.
- When the session ends (or a milestone lands), persist an
  "Implemented / Decisions / Open" narrative with `save_session_summary`
  — the "Open" section is how the next session recovers unfinished work.
- Decision files live in `.cortex/decisions/` and are committed with the
  code they explain.
- If semantic search returns nothing useful, embeddings may be missing —
  suggest running `cortex embed --missing`.

More: https://github.com/lucasreali/cortex-cli#how-it-works
<!-- cortex:end -->

## Development — test first

Write the test before the production code. The cycle is red, green, refactor:

1. Write a failing test naming the domain behaviour being added.
2. Run it and watch it fail for the reason you expect. A test that passes
   before the code exists is asserting nothing.
3. Write the least production code that makes it pass.
4. Refactor with the suite green.

- Test names say what the domain does, in the same voice as the commit message:
  `aSettledInvoiceReportsAsPaid`, not `testSettle`. When the behaviour changes,
  rename the test — a name that no longer describes what it asserts is worse
  than no name at all.
- A bug fix starts with a test that reproduces the bug.
- Never bend a test to match code that was just written. Either the test states
  the behaviour that was meant, or the behaviour was wrong. Deciding which is
  the work.
- When a change to production code forces an edit to `ReuseTest` or
  `RecordFactoryExtensionTest`, read it as a design failure rather than test
  maintenance. Those files define field types, validators and policies outside
  the production code precisely to prove the extension points stay open;
  reshape the change until they compile untouched.
- The suite runs against a real PostgreSQL (`clivo_test`) and cleans up in
  `DatabaseTest.discardTestData`. An interrupted run skips that cleanup, so the
  next run inherits rows and fails on constraints in tests that touch nothing
  you changed. Residue is a hypothesis to prove, never an excuse to stop
  reading: run the failing test on its own, and inspect the leftover rows. If
  it passes alone and the tables are dirty, it was residue; wipe and re-run. If
  it fails alone, it is yours.
- Never run two `./gradlew` test tasks at once: they share `build/test-results`
  and the same database, and the result of both is meaningless.

## Committing

Commit as soon as a piece of work is finished, not in a batch at the end of
the session — one commit per change, or per phase when its parts land
together.

- `./gradlew build` must be green before committing. Never commit a red suite.
- Include the `.cortex/decisions/` files written for that work; they are
  committed with the code they explain.

### Message format

Commits and branch names follow
[Conventional Commits v1.0.0](https://www.conventionalcommits.org/en/v1.0.0/):
`<type>[optional scope]: <description>`. The scope is the module the change
lands in (`billing`, `encounter`, `scheduling`, ...), and the description is
written in English, in the imperative, saying what the change does for the
domain — `feat(billing): settle an invoice in full or in part`, not
`feat(billing): add PaymentService`.

Branches use the same type prefixes: `fix/login-race-condition`.

No AI attribution anywhere in commits, branches or pull requests: no
"Generated with" badges, no `Co-Authored-By` trailers for agents, nothing
similar.
