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

## Committing

Commit as soon as a task from `todo.md` is finished, not in a batch at the
end of the session — one commit per task, or per phase when its tasks land
together.

- `./gradlew build` must be green before committing. Never commit a red suite.
- Tick the task `[X]` in `todo.md` in the same commit as the code.
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
