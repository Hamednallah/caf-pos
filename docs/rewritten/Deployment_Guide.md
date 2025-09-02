# Deployment Guide
Generated: 2025-09-02

## Prerequisites
- JDK 21 (for runtime and jpackage)
- Maven 3.8+

## Build & Run (developer)
1. mvn -DskipTests package
2. java -jar target/cafeteria-pos-1.0.0-jar-with-dependencies.jar

## Create Windows MSI (production)
Use jpackage from JDK 21
jpackage --name CafeteriaPOS --input target --main-jar cafeteria-pos-1.0.0-jar-with-dependencies.jar --type msi --win-shortcut --app-version 1.0.0 --vendor "YourCompany" --icon installer/icon.ico --dest dist

## Post-install steps
- Run the app, open Settings → select a printer → Save (printer name stored in DB under 'printer.default').
- Create admin user or change default admin password on first run.

