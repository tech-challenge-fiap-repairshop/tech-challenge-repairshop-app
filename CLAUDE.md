# CLAUDE.md — Repair Shop (Tech Challenge Fase 1)

## Project overview

Back-end MVP for a vehicle repair shop management system. Built with Kotlin + Spring Boot + Maven + PostgreSQL.
POSTECH 15SOAT — Group CAO (Alexandre, Caio, Otavio).

## Tech stack

- Kotlin 2.x, Spring Boot 4.x, Maven
- PostgreSQL, Spring Data JPA
- Spring Security + JWT
- SpringDoc OpenAPI (Swagger)
- JUnit 5 for testing

## Git conventions

- **Conventional Commits** format is mandatory for all commits
- Commit messages in **English**
- Types: `feat`, `fix`, `docs`, `style`, `refactor`, `perf`, `test`, `build`, `chore`
- Subject line: imperative mood, max 50 characters, no period
- Body: explain WHY, not just WHAT. Wrap at 72 characters
- **NEVER** include `Co-Authored-By` or any AI/Claude attribution
- **NEVER** run `git push` without explicit user confirmation
- **NEVER** use `--amend` without explicit user confirmation
- **NEVER** use `--no-verify` or skip pre-commit hooks
- **NEVER** use `git add -A` or `git add .` — stage specific files only
- Separate unrelated changes into atomic commits
- Use `/commit` skill for the full commit workflow

## Code conventions

- Language: Kotlin
- Package structure: `com.cao.repairshop`
- Table names prefixed with `tb_` (e.g., `tb_customer`, `tb_service_order`)
- Entity IDs: UUID, named `id_tb_<entity>` (e.g., `id_tb_customer`)
- Timestamps: `created`, `updated` (not `created_at`/`updated_at`)
- Enums: `StatusOs`, `StatusService`

### Null safety — `!!` is forbidden

**NEVER** use the non-null assertion operator `!!` anywhere in the codebase. It crashes at runtime with an opaque `KotlinNullPointerException` and bypasses the compiler's null safety guarantees. Use safe alternatives instead:

| Situation | Use instead of `!!` |
|-----------|-------------------|
| Value must exist or fail | `?: throw SomeException("message")` (elvis + explicit exception) |
| Value must exist (precondition) | `requireNotNull(value) { "message" }` or `checkNotNull(value) { "message" }` |
| Default fallback | `?: defaultValue` (elvis operator) |
| Transform if present | `?.let { ... }` (safe call + let) |
| External call that may fail | `runCatching { ... }.getOrElse { throw SomeException("message") }` |

## Cognitive system

This project uses a cognitive system located in `.claude/` with agents, context, adapters, memory, and skills. **Always read these before any spec, plan, task, or implementation work.**

### Agents (in `.claude/agents/`)
- `ddd/` — Generic DDD knowledge (principles + capabilities)
- `spring-kotlin/` — Generic Spring Boot 4.x + Kotlin 2.x implementation knowledge
- `spec-writer/` — Generic SDD specification writing knowledge
- `spec-planner/` — Generic technical planning knowledge
- `spec-tasker/` — Generic task breakdown knowledge
- `domain-expert/` — Project-specific: extracts domain concepts
- `architect/` — Project-specific: validates boundaries and constraints
- `modeler/` — Project-specific: defines aggregates and invariants
- `reviewer/` — Project-specific: validates model against DDD principles

### Context (in `.claude/context/`)
- `ubiquitous-language.md` — Domain glossary by bounded context
- `event-storming.md` — Domain flows, commands, events, policies
- `er-diagram.md` — Data model (9 tables, relationships, enums)
- `business-rules.md` — Status transitions, validations, invariants

### Other
- `adapters/spec-kit-constitution.md` — Workflow orchestration (pre-loading, specification, planning, implementation)
- `memory/decisions.md` — Architecture decisions (active + historical)
- `skills/commit/` — `/commit` skill for atomic commits
- `skills/domain-reviewer/` — `/domain-reviewer` skill for DDD validation

### SDD pipeline output directories
- `docs/spec/MM-DD-YY-spec/` — Specification documents
- `docs/plan/MM-DD-YY-plan/` — Plan documents (plan.md, data-model.md, contracts/, research.md, quickstart.md)
- `docs/task/MM-DD-YY-task/` — Task breakdown documents

---

## Available agents

When working on specifications, plans, tasks, or implementation, **always consult the relevant agents** by reading their files:

| Agent | Path | Use when |
|-------|------|----------|
| spec-writer | `.claude/agents/spec-writer/` | Writing or reviewing specifications |
| spec-planner | `.claude/agents/spec-planner/` | Creating technical plans from specs |
| spec-tasker | `.claude/agents/spec-tasker/` | Breaking plans into atomic, ordered, executable tasks |
| ddd | `.claude/agents/ddd/` | Modeling domain (aggregates, boundaries, invariants) |
| spring-kotlin | `.claude/agents/spring-kotlin/` | Implementing in Kotlin + Spring Boot 4.x |
| domain-expert | `.claude/agents/domain-expert/` | Extracting domain concepts for this project |
| architect | `.claude/agents/architect/` | Validating bounded context boundaries |
| modeler | `.claude/agents/modeler/` | Defining aggregates and invariants for this project |
| reviewer | `.claude/agents/reviewer/` | Validating model against DDD principles |

Each agent has: `AGENT.md` (role), `principles/` (guidelines), `capabilities/` (how-to guides), `OUTPUT.md` (output format), `EXAMPLES.md` (examples).

## Available skills

| Skill | Path | Invoke with | Use when |
|-------|------|-------------|----------|
| commit | `.claude/skills/commit/` | `/commit` | Committing changes with detailed Conventional Commits |
| domain-reviewer | `.claude/skills/domain-reviewer/` | `/domain-reviewer` | Validating domain model against DDD principles |

**When a new agent or skill is created, it MUST be added to the tables above and to the Cognitive system section.** Failing to do so will cause future sessions to not find them.

The full workflow is defined in `.claude/adapters/spec-kit-constitution.md`.

---

## Key documentation

- `docs/spec/delivery-specs.md` — Full project requirements and implementation plan
- `docs/spec/database-er-diagram.png` — ER diagram (team's source of truth)
- `docs/spec/database-er-diagram.html` — Interactive ER diagram
- `docs/delivery/dictionary-ubiquitous-language.md` — DDD ubiquitous language
- `docs/foundation/15SOAT-fase-1-tech-challenge.pdf` — Original challenge specification
- `docs/miro/event-storming.pdf` — Event Storming documentation
- `docs/miro/storytelling.pdf` — Storytelling documentation
- `docs/data/repairshop-inventory.csv` — Inventory seed data (250 items)
- `docs/prompt/prompt-cognitive-system.md` — Prompt for cognitive system generation
