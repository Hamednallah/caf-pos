# Reports Screen Mockup

## 1. Overview

The Reports screen provides insights into sales, shifts, and overall cafeteria performance. The redesigned screen will offer a tabbed interface to switch between different report types, providing a clear and organized way to view data. Future enhancements will include graphical charts and options to export data to CSV or PDF.

## 2. Layout

The screen will use a `TabPane` to organize different reports.

-   **Top:** A title label "Reports".
-   **Center:** A `TabPane` with the following tabs:
    -   Daily Sales
    -   Date Range Report
    -   Shift Reports

## 3. Daily Sales Tab

-   **Controls:** A `DatePicker` to select a date.
-   **Display:** A `TableView` showing sales data for the selected date, grouped by item.
    -   **Columns:** "Item Name", "Quantity Sold", "Total Sales".
-   **Summary:** Labels at the bottom of the tab will show "Total Items Sold" and "Total Revenue" for the selected day.

## 4. Date Range Report Tab

-   **Controls:** Two `DatePicker` controls, "Start Date" and "End Date". A "Generate Report" button.
-   **Display:** After clicking "Generate Report", this area will display key performance indicators (KPIs) for the selected range.
    -   **KPIs:**
        -   Total Sales Revenue
        -   Total Orders
        -   Average Order Value
        -   Total Expenses (future enhancement)
        -   Net Profit (future enhancement)
-   **Visualization:** A `BarChart` or `LineChart` showing sales trends over the selected date range.

## 5. Shift Reports Tab

-   **Controls:** A `ComboBox` or `ListView` to select a specific shift (e.g., "Shift #123 - John Doe - 2025-09-03").
-   **Display:** A detailed summary of the selected shift, similar to the end-of-shift dialog.
    -   **Fields:**
        -   Shift ID
        -   Cashier
        -   Start Time / End Time
        -   Starting Float
        -   Total Cash Sales
        -   Total Bank Sales
        -   Total Expenses
        -   Expected Cash in Drawer
        -   Actual Cash (future enhancement for reconciliation)
        -   Difference (future enhancement)
    -   A `TableView` showing all orders processed during that shift.

## 6. Future Enhancements

-   **Export Buttons:** "Export to CSV" and "Export to PDF" buttons on each tab.
-   **Charts:** More advanced charts and visualizations.
-   **Accessibility:** Ensuring all controls and text are accessible.
