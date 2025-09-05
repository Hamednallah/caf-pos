# Reports Screen Redesign Mockup

## 1. Overview

The `Reports` screen will be redesigned to provide a more organized and powerful way to view sales data. The new design will use a `TabPane` to separate different types of reports.

## 2. Main Layout (`ReportsView.fxml`)

The main view will be a `TabPane` (`fx:id="reportsTabPane"`) containing three tabs.

### Tab 1: Daily Sales

-   **Title:** "Daily Sales"
-   **Content:** A `VBox` container.
    -   An `HBox` at the top with a `Label` "Select Date:" and a `DatePicker` (`fx:id="dailyDatePicker"`).
    -   A `TableView` (`fx:id="dailySalesTable"`) to show the sales for the selected date.
        -   Columns:
            1.  **Item Name:** (`fx:id="dailyItemNameCol"`)
            2.  **Quantity Sold:** (`fx:id="dailyQuantityCol"`)
            3.  **Total Sales:** (`fx:id="dailyTotalCol"`)
    -   An `HBox` at the bottom with a `Button` "Export to CSV" (`fx:id="exportCsvButton"`).

### Tab 2: Date Range Report

-   **Title:** "Date Range Report"
-   **Content:** A `VBox` container.
    -   An `HBox` at the top for date selection:
        -   `Label` "From:", `DatePicker` (`fx:id="fromDatePicker"`)
        -   `Label` "To:", `DatePicker` (`fx:id="toDatePicker"`)
        -   `Button` "Generate Report" (`fx:id="generateReportButton"`)
    -   A `GridPane` to display key performance indicators (KPIs):
        -   Label "Total Sales:", Label (`fx:id="totalSalesLabel"`)
        -   Label "Total Orders:", Label (`fx:id="totalOrdersLabel"`)
    -   A `BarChart` (`fx:id="salesBarChart"`) to visualize sales over the selected period.
        -   X-Axis (`CategoryAxis`): Date
        -   Y-Axis (`NumberAxis`): Total Sales

### Tab 3: Shift Reports

-   **Title:** "Shift Reports"
-   **Content:** A `VBox` container.
    -   An `HBox` at the top with a `Label` "Select Shift:" and a `ComboBox` (`fx:id="shiftComboBox"`).
    -   A `GridPane` (`fx:id="shiftDetailsGrid"`) to display the details of the selected shift report.
        -   Label "Shift ID:", Label (`fx:id="shiftIdLabel"`)
        -   Label "Cashier:", Label (`fx:id="cashierLabel"`)
        -   Label "Start Time:", Label (`fx:id="startTimeLabel"`)
        -   Label "End Time:", Label (`fx:id="endTimeLabel"`)
        -   Label "Starting Float:", Label (`fx:id="startingFloatLabel"`)
        -   Label "Total Sales:", Label (`fx:id="shiftTotalSalesLabel"`)
        -   Label "Cash Sales:", Label (`fx:id="cashSalesLabel"`)
        -   Label "Bank Sales:", Label (`fx:id="bankSalesLabel"`)
        -   Label "Total Expenses:", Label (`fx:id="totalExpensesLabel"`)
        -   Label "Expected Cash:", Label (`fx:id="expectedCashLabel"`)
        -   Label "Actual Cash:", Label (`fx:id="actualCashLabel"`)
        -   Label "Difference:", Label (`fx:id="differenceLabel"`)

## 3. Controller Logic (`ReportsController.java`)

-   `initialize()`:
    -   Populate the `shiftComboBox` with all available shifts.
    -   Set up listeners on the `dailyDatePicker` and `shiftComboBox` to automatically refresh their respective reports when changed.
-   `loadDailySalesReport()`:
    -   Called when `dailyDatePicker` value changes.
    -   Gets the selected date, calls the `ReportsService` to get the data, and populates `dailySalesTable`.
-   `handleGenerateDateRangeReport()`:
    -   Called when the "Generate Report" button is clicked.
    -   Gets the date range, calls the `ReportsService`, and populates the KPI labels and the `salesBarChart`.
-   `loadShiftReport()`:
    -   Called when `shiftComboBox` value changes.
    -   Gets the selected shift, calls the `ReportsService` for the full shift report, and populates all the labels in the `shiftDetailsGrid`.
-   `handleExportCsv()`:
    -   Exports the data currently in `dailySalesTable` to a `.csv` file.
