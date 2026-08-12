# T24R Digital Signing Integration

Source code generated from the original implementation plan and updated to follow `docs/Kiến trúc rút gọn.odt`.

This repository contains a runnable phase-1 MVP for the planned T24R digital signing platform:

- Spring Boot modular monolith with Database Outbox, Signing Orchestrator, Document Generation, mock Digital Signature, Local File Storage, metadata query, download, reconciliation, partner API, API-key auth, dashboard, and audit log.
- Angular operations console for structured metadata search, outbox polling, snapshot comparison, hash-verified downloads, reconciliation, and audit review.
- Local H2 profile for development plus Oracle/Flyway configuration for the target deployment profile.
- Docker and Kubernetes starter assets.

## Repository

```text
backend/      Spring Boot API service
frontend/     Angular operations console
deploy/k8s/   Kubernetes starter manifest
```

## Local Backend

```bash
cd backend
./mvnw spring-boot:run
```

Defaults:

- API: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Admin basic auth: `admin / admin123`
- Partner API key: `dev-api-key-t24r`
- Local database: file-backed H2 with Flyway schema and seeded sample records
- Local database files: `backend/data/database/` (preserved across backend restarts)
- Local file storage root: `backend/data/digital-signing` when running from `backend/`

Useful calls:

```bash
curl -u admin:admin123 http://localhost:8080/api/v1/dashboard/summary
curl -u maker:maker-password -X POST http://localhost:8080/api/v1/ingestions/run \
  -H 'Content-Type: application/json' \
  -d '{"limit": 8}'
curl -u checker:checker-password -X POST http://localhost:8080/api/v1/outbox-events/run \
  -H 'Content-Type: application/json' \
  -d '{"limit": 8}'
curl -u admin:admin123 'http://localhost:8080/api/v1/documents?documentType=ACCOUNT_BALANCE&status=SIGNED'
curl -u admin:admin123 -X POST http://localhost:8080/api/v1/reconciliation/run
curl -H 'X-API-Key: dev-api-key-t24r' \
  http://localhost:8080/external/v1/records/T24R-$(date +%F)-0001
```

## Local Frontend

```bash
cd frontend
npm start
```

Open `http://localhost:4200`. The console is wired to the backend defaults above.

## Users and Permissions

The configured admin account is created in `user_accounts` on first startup. Passwords are encoded and never
returned by the API. Administrators can manage accounts through:

```bash
curl -u admin:admin123 http://localhost:8080/api/v1/users
curl -u admin:admin123 -X POST http://localhost:8080/api/v1/users \
  -H 'Content-Type: application/json' \
  -d '{"username":"viewer","displayName":"Document Viewer","password":"change-me-123","role":"USER"}'
```

Use `PUT /api/v1/users/{id}` to change a display name or role, activate/deactivate an account, or reset its
password. The workflow uses four roles:

- `MAKER` generates source data and submits documents with `POST /api/v1/ingestions/run` for checker approval.
- `CHECKER` reviews the submitted queue and processes it with `POST /api/v1/outbox-events/run`, which signs the documents.
- `ADMIN` manages users, parameters, access, and every internal API.
- `USER` can only search, inspect, compare, and download documents and signed records.

The Angular console presents only the permitted workflow actions for the signed-in role.

## Local Data Persistence

The default development datasource is file-backed H2. When the backend is started from `backend/`,
database files are stored under `backend/data/database/`; Docker Compose mounts the same directory
into the backend container. Restarting the application or recreating the container therefore keeps
documents, users, outbox events, and audit records.

To use a disposable in-memory database for a one-off run, override the datasource URL:

```bash
cd backend
SPRING_DATASOURCE_URL='jdbc:h2:mem:t24r_signing;MODE=Oracle;DATABASE_TO_UPPER=false;DB_CLOSE_DELAY=-1' \
./mvnw spring-boot:run
```

Tests continue to use an isolated in-memory H2 database and never read or modify local development
data. To intentionally reset local data, stop the backend and remove the files under
`backend/data/database/`; Flyway and the demo-data seeder will recreate a clean database at the next
startup.

## Oracle Profile

Run with:

```bash
cd backend
# Local Oracle XE defaults to jdbc:oracle:thin:@//localhost:1521/XEPDB1
# and the portal application user. Override ORACLE_JDBC_URL/ORACLE_USERNAME if needed.
ORACLE_PASSWORD='secret' \
./mvnw spring-boot:run -Dspring-boot.run.profiles=oracle
```

Do not commit the password. For the local `oracle-xe` Docker container, use the
password configured as `APP_USER_PASSWORD`. The application user must be able to
create and alter the objects managed by Flyway.

The migration in `backend/src/main/resources/db/migration` creates `outbox_event`, `digital_document`, dead-letter/archive, audit-log, API-client, and legacy compatibility tables. Real Core Banking and HSM/CA/TSA integrations should replace the mock adapters behind `T24rClient` and `SigningGateway`.

For production-like deployment, set `app.storage.root-path` or `APP_STORAGE_ROOT_PATH` to a mounted path such as `/data/digital-signing`. If more than one backend replica is used, this path must be shared storage such as NAS/SAN/PVC with `ReadWriteMany`; do not expose it directly through NGINX/Apache.

## Verification

```bash
cd backend && ./mvnw test
cd frontend && npm run build
cd frontend && npm run test:ci
```
