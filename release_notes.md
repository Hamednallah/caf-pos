# Cafeteria POS - Release Notes v1.0.0

This release completes the initial development of the Cafeteria POS application, including a full user interface and all core features.

## Completed Features

*   **Sales:** Fully functional sales screen with an item catalog, shopping cart, and payment finalization.
*   **Items Management:** Full C.R.U.D. (Create, Read, Update, Delete) support for menu items.
*   **Shifts:** Start and end cashier shifts, with a summary report generated at the end of each shift.
*   **Expenses:** Record expenses against the active shift.
*   **Reports:** Generate a daily sales report and export it to CSV.
*   **Settings:** Configure the default receipt printer and send test prints.
*   **Receipts:** Receipts are now formatted with ESC/POS commands for professional printing.
*   **Navigation:** A unified main window provides easy navigation between all feature screens.
*   **Database:** The application now includes an integration test to ensure all services work together correctly. The database schema is automatically created and migrated on first launch.

## How to Build and Package

The final application installer can be built on a Windows machine with JDK 21 and Maven installed.

1.  **Open a PowerShell terminal** in the root of the project repository.
2.  **Run the build script:**
    ```powershell
    .\scripts\build_and_package.ps1
    ```

This script will first build the application JAR using `mvn package` and then use `jpackage` to create an MSI installer in the `dist/` directory.

**Note:** To add vendor information to the installer, you can edit the `scripts/build_and_package.ps1` file and add the `--vendor "Your Company Name"` flag to the `jpackage` command.

## First-Run & Operational Steps

### 1. Setting the Default Printer
Before printing receipts, you must configure the default printer.
1.  Run the application.
2.  Navigate to the **Settings** screen from the main menu.
3.  Select your receipt printer from the dropdown menu.
4.  Click **Save Settings**.
5.  You can use the **Test Print** button to verify that the printer is working correctly.

### 2. Changing the Admin Password
The application is seeded with a default `admin` user. There is currently no UI for changing user passwords. To change a password, you must do so directly in the database.

*This requires a tool to connect to the H2 database file.*

1.  Locate the database file. By default, it will be created in the directory where the application is run, named `cafeteria.db.mv.db`.
2.  Connect to the database using a tool like DBeaver or the H2 Console.
3.  Execute an SQL update command. You will need to generate a new BCrypt hash for your password.
    ```sql
    -- Example: Update the password for the 'admin' user
    UPDATE "user" SET password_hash = '<your_new_bcrypt_hash>' WHERE username = 'admin';
    ```

### 3. Emergency Database Recovery
If the database becomes corrupted or you need to reset the application to its initial state, you can perform the following steps:
1.  Close the application.
2.  Locate and delete the database files: `cafeteria.db.mv.db` and `cafeteria.db.trace.db`.
3.  Relaunch the application. It will automatically create a new, clean database with the default seed data. **All previous data (orders, items, etc.) will be lost.**
