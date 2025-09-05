package com.yourcompany.cafeteria.ui;

import com.yourcompany.cafeteria.model.DateRangeReport;
import com.yourcompany.cafeteria.model.Shift;
import com.yourcompany.cafeteria.model.ShiftReport;
import com.yourcompany.cafeteria.service.ReportsService;
import com.yourcompany.cafeteria.service.ShiftService;
import com.yourcompany.cafeteria.util.DataSourceProvider;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;

import java.io.File;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class ReportsController implements ResourceAwareController {

    // Common Services
    private ReportsService reportsService;
    private ShiftService shiftService;

    // Tab: Daily Sales
    @FXML private DatePicker dailyDatePicker;
    @FXML private TableView<DailySale> dailySalesTable;
    @FXML private TableColumn<DailySale, String> dailyItemNameCol;
    @FXML private TableColumn<DailySale, Integer> dailyQuantityCol;
    @FXML private TableColumn<DailySale, BigDecimal> dailyTotalCol;

    // Tab: Date Range Report
    @FXML private DatePicker fromDatePicker;
    @FXML private DatePicker toDatePicker;
    @FXML private Label totalSalesLabel;
    @FXML private Label totalOrdersLabel;
    @FXML private BarChart<String, Number> salesBarChart;

    // Tab: Shift Reports
    @FXML private ComboBox<Shift> shiftComboBox;
    @FXML private Label shiftIdLabel;
    @FXML private Label cashierLabel;
    @FXML private Label startTimeLabel;
    @FXML private Label endTimeLabel;
    @FXML private Label startingFloatLabel;
    @FXML private Label shiftTotalSalesLabel;
    @FXML private Label totalExpensesLabel;
    @FXML private Label expectedCashLabel;
    @FXML private Label actualCashLabel;
    @FXML private Label differenceLabel;

    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private ResourceBundle resourceBundle;

    @Override
    public void setResources(ResourceBundle resourceBundle) {
        this.resourceBundle = resourceBundle;
        // The UI is already set by the FXML loader, but we could update dynamic text here if needed.
    }

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
            showAlert(Alert.AlertType.ERROR, "Initialization Failed", "Could not initialize reports view: " + e.getMessage());
        }
    }

    // =================================================================
    // Daily Sales Tab Logic
    // =================================================================
    private void setupDailySalesTab() {
        dailyItemNameCol.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        dailyQuantityCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        dailyTotalCol.setCellValueFactory(new PropertyValueFactory<>("totalSales"));

        dailyDatePicker.setValue(LocalDate.now());
        dailyDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> loadDailySalesReport());
        loadDailySalesReport();
    }

    private void loadDailySalesReport() {
        LocalDate selectedDate = dailyDatePicker.getValue();
        if (selectedDate == null) return;

        ObservableList<DailySale> sales = FXCollections.observableArrayList();
        try (ResultSet rs = reportsService.getDailySales(selectedDate)) {
            while (rs.next()) {
                sales.add(new DailySale(
                        rs.getString("name"),
                        rs.getInt("qty"),
                        rs.getBigDecimal("sales")
                ));
            }
            dailySalesTable.setItems(sales);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Load Error", "Failed to load daily sales report.");
        }
    }

    @FXML
    private void handleExportCsv() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Daily Sales Report");
        fileChooser.setInitialFileName("daily_sales_" + dailyDatePicker.getValue() + ".csv");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fileChooser.showSaveDialog(dailySalesTable.getScene().getWindow());

        if (file != null) {
            try (PrintWriter writer = new PrintWriter(file)) {
                writer.println("Item Name,Quantity Sold,Total Sales");
                for (DailySale item : dailySalesTable.getItems()) {
                    writer.printf("\"%s\",%d,%.2f%n", item.getItemName().replace("\"", "\"\""), item.getQuantity(), item.getTotalSales());
                }
                showAlert(Alert.AlertType.INFORMATION, "Success", "Report exported successfully.");
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Export Failed", "Could not save the report: " + e.getMessage());
            }
        }
    }


    // =================================================================
    // Date Range Tab Logic
    // =================================================================
    private void setupDateRangeTab() {
        fromDatePicker.setValue(LocalDate.now().withDayOfMonth(1));
        toDatePicker.setValue(LocalDate.now());
    }

    @FXML
    private void handleGenerateDateRangeReport() {
        LocalDate from = fromDatePicker.getValue();
        LocalDate to = toDatePicker.getValue();

        if (from == null || to == null || from.isAfter(to)) {
            showAlert(Alert.AlertType.WARNING, "Invalid Date Range", "Please select a valid 'from' and 'to' date.");
            return;
        }

        try {
            DateRangeReport report = reportsService.getDateRangeReport(from, to);
            totalSalesLabel.setText(String.format("$%.2f", report.getTotalSales()));
            totalOrdersLabel.setText(String.valueOf(report.getOrdersCount()));

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Sales");
            report.getSalesByDay().forEach((date, total) -> {
                series.getData().add(new XYChart.Data<>(date.toString(), total));
            });
            salesBarChart.getData().clear();
            salesBarChart.getData().add(series);

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Report Error", "Failed to generate date range report.");
        }
    }


    // =================================================================
    // Shift Reports Tab Logic
    // =================================================================
    private void setupShiftReportsTab() throws Exception {
        List<Shift> shifts = shiftService.getAllShifts();
        shiftComboBox.setItems(FXCollections.observableArrayList(shifts));

        shiftComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Shift shift) {
                if (shift == null) return null;
                return String.format("ID: %d (%s)", shift.getId(), shift.getStartTime().format(dateTimeFormatter));
            }

            @Override
            public Shift fromString(String string) {
                return null; // Not needed
            }
        });

        shiftComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                loadShiftReport(newVal.getId());
            }
        });
    }

    private void loadShiftReport(int shiftId) {
        try {
            ShiftReport report = reportsService.getShiftReport(shiftId);
            shiftIdLabel.setText(String.valueOf(report.getShiftId()));
            cashierLabel.setText(report.getCashierName());
            startTimeLabel.setText(report.getStartTime() != null ? report.getStartTime().format(dateTimeFormatter) : "-");
            endTimeLabel.setText(report.getEndTime() != null ? report.getEndTime().format(dateTimeFormatter) : "-");
            startingFloatLabel.setText(String.format("$%.2f", report.getStartingFloat()));
            shiftTotalSalesLabel.setText(String.format("$%.2f", report.getTotalSales()));
            totalExpensesLabel.setText(String.format("$%.2f", report.getTotalExpenses()));
            expectedCashLabel.setText(String.format("$%.2f", report.getExpectedCash()));

            // These might be null if the shift is not ended
            actualCashLabel.setText(report.getActualCash() != null ? String.format("$%.2f", report.getActualCash()) : "-");
            differenceLabel.setText(report.getDifference() != null ? String.format("$%.2f", report.getDifference()) : "-");

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Load Error", "Failed to load shift report.");
        }
    }


    // =================================================================
    // Helper Methods
    // =================================================================
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // =================================================================
    // Inner class for Daily Sales Table
    // =================================================================
    public static class DailySale {
        private final SimpleStringProperty itemName;
        private final SimpleIntegerProperty quantity;
        private final SimpleObjectProperty<BigDecimal> totalSales;

        public DailySale(String itemName, int quantity, BigDecimal totalSales) {
            this.itemName = new SimpleStringProperty(itemName);
            this.quantity = new SimpleIntegerProperty(quantity);
            this.totalSales = new SimpleObjectProperty<>(totalSales);
        }

        public String getItemName() { return itemName.get(); }
        public int getQuantity() { return quantity.get(); }
        public BigDecimal getTotalSales() { return totalSales.get(); }
    }
}
