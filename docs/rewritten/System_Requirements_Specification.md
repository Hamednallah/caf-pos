# System Requirements Specification (SRS)
Cafeteria POS — Single-terminal desktop application
Generated: 2025-09-02

## 1. Introduction
Purpose: Provide a complete, single-terminal POS for small cafeterias. Focus on speed, usability (touch-first), bilingual UI (English/Arabic), and reliable receipt printing on 80mm thermal printers.

Scope: Local-only, single-computer installation (application + DB on same machine). Core features: item management, order processing, shift management, expenses, reporting, system settings (branding & printer selection).

Audience: Developers, testers, deployment engineers, and AI agents finishing implementation.

## 2. System Overview
- Architecture: Java 21 + JavaFX frontend, layered architecture: Presentation (UI) → Services (business) → DAOs (data access) → H2 embedded DB. Flyway for migrations.
- Single-terminal deployment: application and DB run on the same Windows machine (localhost).

## 3. Functional Requirements (selected)
- Users: Login with roles CASHIER and ADMIN.
- Items: CRUD with name, description, price (SDG), category, image path.
- Orders: create, modify, discount (fixed amount), finalize, payment methods: CASH or BANK (manual confirmation), receipt printing.
- Shifts: start/pause/end, starting cash float, shift reports for reconciliation.
- Expenses: record operational expenses tied to a shift and recorded_by user.
- Reports: daily/weekly/custom with breakdowns per item, category, cashier.
- Settings: system branding (profile) and default printer selection saved to DB key 'printer.default'.

## 4. Non-functional Requirements
- Passwords stored hashed (bcrypt).
- UI: touch-first, Arabic LTR/RTL support, locale-aware formatting.
- Reliability: offline first, robust error handling for printer/DB failures.
- Maintainability: DAO-Service-Controller separation, unit tests for DAOs.

## 5. Database
- See `src/main/resources/db/migration` for full schema (Flyway SQL files).
- ER diagram included at `docs/er_diagram.png` (simplified).

## 6. Workflow & UX
- Cashier logs in, starts shift, processes orders, prints receipts, records expenses, ends shift.
- Admin can manage items, view reports, set printer and branding settings.

## 7. Packaging
- Packaged with `jpackage` into MSI for Windows.
- Settings persist to the embedded DB (no external config files required).

