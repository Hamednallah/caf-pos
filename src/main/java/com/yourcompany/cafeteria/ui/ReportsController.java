package com.yourcompany.cafeteria.ui;

import com.yourcompany.cafeteria.model.DateRangeReport;
import com.yourcompany.cafeteria.model.Shift;
import com.yourcompany.cafeteria.model.ShiftSummary;
import com.yourcompany.cafeteria.service.ReportsService;
import com.yourcompany.cafeteria.service.ShiftService;
import com.yourcompany.cafeteria.util.DataSourceProvider;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.PrintWriter;
import java.math.BigDecimal;
import javafx.scene.chart.XYChart;

import java.io.File;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class ReportsController {

    // Common Services
    private ReportsService reportsService;
    private ShiftService shiftService;

    // Daily Sales Tab
    @FXML private DatePicker dailySalesReportDatePicker;
    @FXML private TableView<DailySalesReportItem> dailySalesReportTable;
    @FXML private TableColumn<DailySalesReportItem, String> itemNameColumn;
    @FXML private TableColumn<DailySalesReportItem, Integer> quantitySoldColumn;
    @FXML private TableColumn<DailySalesReportItem, BigDecimal> totalSalesColumn;
    @FXML private Button exportDailySalesButton;
    private ObservableList<DailySalesReportItem> dailyReportData = FXCollections.observableArrayList();

    // Date Range Tab
    @FXML private DatePicker dateRangeStartDatePicker;
    @FXML private DatePicker dateRangeEndDatePicker;
    @FXML private Label totalSalesLabel;
    @FXML private Label totalOrdersLabel;
    @FXML private javafx.scene.chart.BarChart<String, Number> salesChart;

    // Shift Reports Tab
    @FXML private ComboBox<Shift> shiftSelectorComboBox;
    @FXML private GridPane shiftReportGrid;

    @FXML
    public void initialize() {
        try {
            reportsService = new ReportsService(DataSourceProvider.getConnection());
            shiftService = new ShiftService(DataSourceProvider.getConnection());

            setupDailySalesTab();
            setupDateRangeTab();
            setupShiftReportsTab();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Initialization Error", "Could not initialize the reports view.");
        }
    }

    @FXML
    private void handleExportDailySales() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Daily Sales Report");
        fileChooser.setInitialFileName("daily_sales_" + dailySalesReportDatePicker.getValue() + ".csv");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fileChooser.showSaveDialog(dailySalesReportTable.getScene().getWindow());

        if (file != null) {
            try (PrintWriter writer = new PrintWriter(file)) {
                writer.println("Item Name,Quantity Sold,Total Sales");
                for (DailySalesReportItem item : dailyReportData) {
                    writer.printf("\"%s\",%d,%.2f%n",
                            item.getName().replace("\"", "\"\""),
                            item.getQuantity(),
                            item.getTotalSales()
                    );
                }
                showAlert(Alert.AlertType.INFORMATION, "Export Successful", "Report saved to " + file.getAbsolutePath());
            } catch (Exception e) {
                showError("Export Failed", "Could not save the report: " + e.getMessage());
            }
        }
    }

    // --- Daily Sales Tab Implementation ---
    private void setupDailySalesTab() {
        dailySalesReportTable.setItems(dailyReportData);
        itemNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        quantitySoldColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        totalSalesColumn.setCellValueFactory(new PropertyValueFactory<>("totalSales"));

        dailySalesReportDatePicker.setValue(LocalDate.now());
        handleGenerateDailyReport(); // Load initial report for today

        dailySalesReportDatePicker.valueProperty().addListener((obs, oldDate, newDate) -> {
            if (newDate != null) {
                handleGenerateDailyReport();
            }
        });
    }

    private void handleGenerateDailyReport() {
        LocalDate selectedDate = dailySalesReportDatePicker.getValue();
        if (selectedDate == null) return;

        dailyReportData.clear();
        try {
            ResultSet rs = reportsService.getDailySales(selectedDate);
            while (rs.next()) {
                dailyReportData.add(new DailySalesReportItem(
                        rs.getString("name"),
                        rs.getInt("qty"),
                        rs.getBigDecimal("sales")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("Report Error", "Failed to generate daily sales report.");
        }
    }

    // --- Date Range Tab Implementation ---
    private void setupDateRangeTab() {
        dateRangeStartDatePicker.setValue(LocalDate.now().withDayOfMonth(1));
        dateRangeEndDatePicker.setValue(LocalDate.now());
    }

    @FXML
    private void handleGenerateDateRangeReport() {
        LocalDate startDate = dateRangeStartDatePicker.getValue();
        LocalDate endDate = dateRangeEndDatePicker.getValue();

        if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
            showAlert(Alert.AlertType.WARNING, "Invalid Date Range", "Please select a valid start and end date.");
            return;
        }

        try {
            DateRangeReport report = reportsService.getDateRangeReport(startDate, endDate);
            totalSalesLabel.setText(String.format("%.2f", report.getTotalSales()));
            totalOrdersLabel.setText(String.valueOf(report.getOrdersCount()));

            // Populate the chart
            Map<LocalDate, BigDecimal> salesByDay = reportsService.getSalesByDay(startDate, endDate);
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Daily Sales");
            for (Map.Entry<LocalDate, BigDecimal> entry : salesByDay.entrySet()) {
                series.getData().add(new XYChart.Data<>(entry.getKey().toString(), entry.getValue()));
            }
            salesChart.getData().setAll(series);

        } catch (Exception e) {
            e.printStackTrace();
            showError("Report Error", "Failed to generate date range report.");
        }
    }

    // --- Shift Reports Tab Implementation ---
    private void setupShiftReportsTab() throws Exception {
        loadShiftsIntoComboBox();
        shiftSelectorComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldShift, newShift) -> {
            if (newShift != null) {
                displayShiftSummary(newShift);
            }
        });
    }

    private void loadShiftsIntoComboBox() throws Exception {
        List<Shift> shifts = shiftService.getAllShifts();
        shiftSelectorComboBox.setItems(FXCollections.observableArrayList(shifts));
    }

    private void displayShiftSummary(Shift shift) {
        shiftReportGrid.getChildren().clear(); // Clear previous summary
        try {
            ShiftSummary summary = reportsService.getShiftSummary(shift.getId());

            shiftReportGrid.add(new Label("Starting Float:"), 0, 0);
            shiftReportGrid.add(new Label(String.format("%.2f", summary.getStartingFloat())), 1, 0);
            shiftReportGrid.add(new Label("Total Cash Sales:"), 0, 1);
            shiftReportGrid.add(new Label(String.format("%.2f", summary.getTotalCashSales())), 1, 1);
            shiftReportGrid.add(new Label("Total Bank Sales:"), 0, 2);
            shiftReportGrid.add(new Label(String.format("%.2f", summary.getTotalBankSales())), 1, 2);
            shiftReportGrid.add(new Label("Total Expenses:"), 0, 3);
            shiftReportGrid.add(new Label(String.format("- %.2f", summary.getTotalExpenses())), 1, 3);
            shiftReportGrid.add(new Label("Expected Cash:"), 0, 4);
            shiftReportGrid.add(new Label(String.format("%.2f", summary.getExpectedCashInDrawer())), 1, 4);

        } catch (Exception e) {
            e.printStackTrace();
            showError("Shift Summary Error", "Could not load summary for the selected shift.");
        }
    }


    // --- Utility Methods ---
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // --- Inner Class for Daily Sales Table ---
    public static class DailySalesReportItem {
        private final SimpleStringProperty name;
        private final SimpleIntegerProperty quantity;
        private final SimpleObjectProperty<BigDecimal> totalSales;

        public DailySalesReportItem(String name, int quantity, BigDecimal totalSales) {
            this.name = new SimpleStringProperty(name);
            this.quantity = new SimpleIntegerProperty(quantity);
            this.totalSales = new SimpleObjectProperty<>(totalSales);
        }

        public String getName() { return name.get(); }
        public int getQuantity() { return quantity.get(); }
        public BigDecimal getTotalSales() { return totalSales.get(); }
    }
}
