# Repository Guidelines

## Project Structure & Module Organization
- Main code lives in `src/main/java/cn/jason31416/planetlib/`.
- Key modules are split by package: `command/`, `gui/`, `data/`, `message/`, `util/`, `wrapper/`, `hook/`, `map/`.
- Runtime resources are in `src/main/resources` (including default config/message files).
- Tests live in `src/test/java` and should mirror package paths from `src/main/java`.
- AI-facing docs are in `PlanetLibAIdoc/`.

## Build, Test, and Development Commands
- `mvn -DskipTests compile` — compile main code quickly.
- `mvn test` — run unit tests.
- `mvn -Dtest=ClassName test` — run a single test class.
- `mvn package` — build the shaded jar (via `maven-shade-plugin`).

Example:
```bash
mvn -DskipTests compile
mvn -Dtest=SqlStatementCompileTest test
```

## Coding Style & Naming Conventions
- Java 21, 4-space indentation, UTF-8 source encoding.
- Package names: lowercase (`cn.jason31416.planetlib...`).
- Classes: `PascalCase`; methods/fields: `camelCase`; constants: `UPPER_SNAKE_CASE`.
- Prefer small, focused methods and fluent builders where already used (e.g., statements/GUI builders).
- Keep public API changes backward-compatible when practical; mark legacy paths with `@Deprecated` instead of removing immediately.

## Testing Guidelines
- Framework: JUnit 5 (`junit-jupiter`).
- Test class names: `*Test` (e.g., `MapTreeMergeTest`, `SqlStatementCompileTest`).
- Test method names should describe behavior (`merge_shouldKeepLeftOnConflict`).
- Cover SQL builder output, mapping behavior, and regression cases for utility classes.

## Commit & Pull Request Guidelines
- Existing history uses versioned summaries (e.g., `v1.4.0 commit 3 - ...`). Keep commit messages short and explicit.
- Recommended format: `<scope>: <what changed>` plus optional version tag when preparing releases.
- PRs should include:
  - What changed and why.
  - Affected packages/files.
  - Validation steps and commands run.
  - Any compatibility or migration notes (especially API/config changes).

## Security & Configuration Tips
- Never commit credentials; keep DB auth in external plugin config.
- When adding config keys, also update default `config.yml` in resources and ensure missing-key warnings remain actionable.
