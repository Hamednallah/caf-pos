package com.yourcompany.cafeteria.ui;

import com.yourcompany.cafeteria.model.DateRangeReport;
import com.yourcompany.cafeteria.model.ShiftReport;
import com.yourcompany.cafeteria.service.ReportsService;
import com.yourcompany.cafeteria.util.DataSourceProvider;
import javafx.beans.binding.Bindings;
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
import java.sql.ResultSet;
import java.time.LocalDate;

public class ReportsController {

    // Daily Sales Report
    @FXML private DatePicker dailyDatePicker;
    @FXML private TableView<DailySalesReportItem> dailyReportTable;
    @FXML private TableColumn<DailySalesReportItem, String> dailyItemNameCol;
    @FXML private TableColumn<DailySalesReportItem, Integer> dailyQuantityCol;
    @FXML private TableColumn<DailySalesReportItem, BigDecimal> dailySalesCol;
    @FXML private Button exportDailyCsvButton;
    private final ObservableList<DailySalesReportItem> dailyReportData = FXCollections.observableArrayList();

    // Shift Report
    @FXML private TextField shiftIdField;
    @FXML private GridPane shiftReportGrid;

    // Date Range Report
    @FXML private DatePicker fromDateDatePicker;
    @FXML private DatePicker toDateDatePicker;
    @FXML private GridPane dateRangeReportGrid;

    private ReportsService reportsService;

    @FXML
    public void initialize() {
        try {
            reportsService = new ReportsService(DataSourceProvider.getConnection());
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to connect to the database.");
            return;
        }

        setupDailySalesTable();
        dailyDatePicker.setValue(LocalDate.now());
        fromDateDatePicker.setValue(LocalDate.now());
        toDateDatePicker.setValue(LocalDate.now());
    }

    private void setupDailySalesTable() {
        dailyReportTable.setItems(dailyReportData);
        dailyItemNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        dailyQuantityCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        dailySalesCol.setCellValueFactory(new PropertyValueFactory<>("totalSales"));
        exportDailyCsvButton.disableProperty().bind(Bindings.isEmpty(dailyReportTable.getItems()));
    }

    @FXML
    private void handleGenerateDailyReport() {
        LocalDate selectedDate = dailyDatePicker.getValue();
        if (selectedDate == null) {
            showAlert(Alert.AlertType.WARNING, "Input Required", "Please select a date.");
            return;
        }

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
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to generate daily sales report: " + e.getMessage());
        }
    }

    @FXML
    private void handleExportDailyCsv() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Daily Sales Report");
        fileChooser.setInitialFileName("daily_sales_" + dailyDatePicker.getValue() + ".csv");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fileChooser.showSaveDialog(dailyReportTable.getScene().getWindow());

        if (file != null) {
            try (PrintWriter writer = new PrintWriter(file)) {
                writer.println("Item Name,Quantity Sold,Total Sales");
                for (DailySalesReportItem item : dailyReportData) {
                    writer.printf("\"%s\",%d,%.2f%n", item.getName(), item.getQuantity(), item.getTotalSales());
                }
                showAlert(Alert.AlertType.INFORMATION, "Export Successful", "Report saved to " + file.getAbsolutePath());
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Export Failed", "Could not save the report: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleGenerateShiftReport() {
        String shiftIdText = shiftIdField.getText();
        if (shiftIdText.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Input Required", "Please enter a Shift ID.");
            return;
        }
        try {
            int shiftId = Integer.parseInt(shiftIdText);
            ShiftReport report = reportsService.getShiftReport(shiftId);
            populateShiftReportGrid(report);
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid Input", "Shift ID must be a number.");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to generate shift report: " + e.getMessage());
        }
    }

    private void populateShiftReportGrid(ShiftReport report) {
        shiftReportGrid.getChildren().clear();
        if (report == null) return;

        shiftReportGrid.add(new Label("Starting Float:"), 0, 0);
        shiftReportGrid.add(new Label(String.format("%.2f", report.getStartingFloat())), 1, 0);
        shiftReportGrid.add(new Label("Total Sales:"), 0, 1);
        shiftReportGrid.add(new Label(String.format("%.2f", report.getTotalSales())), 1, 1);
        shiftReportGrid.add(new Label("Total Discounts:"), 0, 2);
        shiftReportGrid.add(new Label(String.format("%.2f", report.getTotalDiscounts())), 1, 2);
        shiftReportGrid.add(new Label("Cash Total:"), 0, 3);
        shiftReportGrid.add(new Label(String.format("%.2f", report.getCashTotal())), 1, 3);
        shiftReportGrid.add(new Label("Bank Total:"), 0, 4);
        shiftReportGrid.add(new Label(String.format("%.2f", report.getBankTotal())), 1, 4);
        shiftReportGrid.add(new Label("Total Expenses:"), 0, 5);
        shiftReportGrid.add(new Label(String.format("%.2f", report.getTotalExpenses())), 1, 5);
        shiftReportGrid.add(new Label("Orders Count:"), 0, 6);
        shiftReportGrid.add(new Label(String.valueOf(report.getOrdersCount())), 1, 6);
    }

    @FXML
    private void handleGenerateDateRangeReport() {
        LocalDate fromDate = fromDateDatePicker.getValue();
        LocalDate toDate = toDateDatePicker.getValue();
        if (fromDate == null || toDate == null) {
            showAlert(Alert.AlertType.WARNING, "Input Required", "Please select both a 'From' and 'To' date.");
            return;
        }
        try {
            DateRangeReport report = reportsService.getDateRangeReport(fromDate, toDate);
            populateDateRangeReportGrid(report);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to generate date range report: " + e.getMessage());
        }
    }

    private void populateDateRangeReportGrid(DateRangeReport report) {
        dateRangeReportGrid.getChildren().clear();
        if (report == null) return;

        dateRangeReportGrid.add(new Label("Total Sales:"), 0, 0);
        dateRangeReportGrid.add(new Label(String.format("%.2f", report.getTotalSales())), 1, 0);
        dateRangeReportGrid.add(new Label("Orders Count:"), 0, 1);
        dateRangeReportGrid.add(new Label(String.valueOf(report.getOrdersCount())), 1, 1);
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

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
