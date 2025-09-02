# Developer Guide
Generated: 2025-09-02

## Architecture
- Presentation: JavaFX (FXML + Controllers)
- Services: business logic exposing safe methods to controllers
- DAOs: canonical data access layer (SQL, transactions)
- Utilities: printing, config, DB provider

## Rules for AI agents / contributors
- DAOs are canonical. Never duplicate or re-create DAO classes. Extend behavior through Services.
- Controllers must call Services only (no direct DB access).
- All database schema changes must be done via Flyway migrations in `src/main/resources/db/migration`.
- Tests must use H2 in-memory DB seeded by Flyway migrations (see src/test utilities).

## How to add a new feature
1. Add schema migration (if needed)
2. Implement DAO method(s) if data layer required (coordinate if canonical change)
3. Implement Service method(s) that encapsulate business rules
4. Wire Controller to call Service
5. Add unit tests for DAO and Service
6. Add minimal UI tests (TestFX) if relevant

