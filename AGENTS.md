# Repository Guidelines

## Project Structure & Module Organization

This repository contains a two-service T24R digital-signing MVP. `backend/` is a Java 21 Spring Boot modular monolith; production code lives under `src/main/java/com/bidv/t24r/signing`, grouped by domain (`document`, `ingestion`, `outbox`, `security`, and others). Configuration and Flyway migrations are in `backend/src/main/resources`. Backend tests mirror package paths under `backend/src/test/java`.

`frontend/` is an Angular 21 application. Place components, services, types, styles, and colocated `*.spec.ts` tests in `frontend/src/app`; static files belong in `frontend/public`. Deployment resources are in `deploy/k8s/`, architecture documents in `docs/`, and local orchestration is defined by `docker-compose.yml`. Treat generated PDFs under `backend/data/` as runtime data, not source code.

## Build, Test, and Development Commands

- `cd backend && ./mvnw spring-boot:run` starts the API on port 8080 with H2 and mock integrations.
- `cd backend && ./mvnw test` runs the Spring/JUnit test suite.
- `cd frontend && npm ci` installs the locked frontend dependencies.
- `cd frontend && npm start` serves the Angular console on port 4200.
- `cd frontend && npm run build` creates a production build and enforces bundle budgets.
- `cd frontend && npm run test:ci` runs Vitest once for CI.
- `docker compose up --build` builds and starts both services.

## Coding Style & Naming Conventions

Java uses four-space indentation, package names in lowercase, `PascalCase` classes, and `camelCase` methods and fields. Keep features in their domain package and favor constructor injection. TypeScript, HTML, CSS, and Markdown follow `frontend/.editorconfig`; use two spaces and single quotes in TypeScript. Prettier uses a 100-column width (`cd frontend && npx prettier --check .`). Angular files use lowercase kebab-case where names contain multiple words.

## Testing Guidelines

Use Spring Boot Test/JUnit for backend behavior and Angular TestBed/Vitest for frontend units. Name Java tests `*Tests.java` and frontend tests `*.spec.ts`. Add tests beside the affected domain, covering success, validation, and failure paths. No numeric coverage threshold is configured; preserve or improve coverage for changed behavior.

## Commit & Pull Request Guidelines

Git history is unavailable in this checkout, so use concise imperative commits, optionally scoped, such as `backend: validate reconciliation dates`. Keep commits focused. Pull requests should explain the problem and solution, list verification commands, link the relevant issue, call out configuration or migration changes, and include screenshots for visible UI updates. Never commit real credentials, API keys, customer data, or signing material; use environment variables and sanitized fixtures.
