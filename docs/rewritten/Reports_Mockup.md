# Reports Screen Mockup

## 1. Main View (ReportsView.fxml)

The main view for reports will be enhanced to support different types of reports. It will use a `TabPane` to separate the different report views.

- **Layout**: A `BorderPane` with a `TabPane` in the center.
- **Tabs**:
    - **Daily Sales**: The existing daily sales report functionality.
    - **Shift Report**: A new tab to view a report for a specific shift.
    - **Date Range Report**: A new tab to view a report for a custom date range.

## 2. Daily Sales Tab

- **Controls**: A `DatePicker` to select the date. A "Generate" button.
- **Display**: A `TableView` to show the daily sales data (Item Name, Quantity, Total Sales).
- **Actions**: An "Export to CSV" button.

## 3. Shift Report Tab

- **Controls**: A `TextField` to enter the Shift ID. A "Generate" button.
- **Display**: A `VBox` or `GridPane` to display the shift report details:
    - Starting Float
    - Total Sales
    - Total Discounts
    - Cash Total
    - Bank Total
    - Orders Count
    - Total Expenses
- **Actions**: An "Export to PDF" button (future enhancement).

## 4. Date Range Report Tab

- **Controls**: Two `DatePicker` controls for "From" and "To" dates. A "Generate" button.
- **Display**: A `VBox` or `GridPane` to display the date range report details:
    - Total Sales
    - Orders Count
- **Actions**: An "Export to PDF" button (future enhancement).

## 5. Workflow

1.  The user selects a tab for the desired report type.
2.  The user provides the necessary input (date, shift ID, or date range).
3.  The user clicks the "Generate" button.
4.  The application fetches the data using the `ReportsService` and displays it in the corresponding view.
5.  The user can export the report if the functionality is available.
