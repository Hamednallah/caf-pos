CafeteriaPOS — Final Deliverable v1
Generated: 2025-09-02

Included:
- Full DAOs, Services, Models
- Flyway migrations (V1..V4)
- JavaFX FXML skeletons and controller stubs
- Receipt printing util and printer selection saved in settings
- Rewritten docs (SRS, Developer Guide, Deployment Guide)
- ER diagram (docs/er_diagram.png)
- Prompts for Cursor, DeepSeek, Jules
- JUnit tests for DAOs (H2 + Flyway)

How to run tests:
mvn -DskipTests=false test

How to build:
mvn -DskipTests package
