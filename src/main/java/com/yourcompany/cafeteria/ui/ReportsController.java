package com.yourcompany.cafeteria.ui;

import com.yourcompany.cafeteria.service.ReportsService;
import com.yourcompany.cafeteria.util.DataSourceProvider;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.time.LocalDate;

public class ReportsController {

    @FXML private DatePicker datePicker;
    @FXML private TableView<DailySalesReportItem> reportTable;
    @FXML private TableColumn<DailySalesReportItem, String> itemNameCol;
    @FXML private TableColumn<DailySalesReportItem, Integer> quantityCol;
    @FXML private TableColumn<DailySalesReportItem, BigDecimal> salesCol;

    private ObservableList<DailySalesReportItem> reportData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTable();
        datePicker.setValue(LocalDate.now()); // Default to today's date
    }

    private void setupTable() {
        reportTable.setItems(reportData);
        itemNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        quantityCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        salesCol.setCellValueFactory(new PropertyValueFactory<>("totalSales"));
    }

    @FXML
    private void handleGenerateReport() {
        LocalDate selectedDate = datePicker.getValue();
        if (selectedDate == null) {
            showAlert(Alert.AlertType.WARNING, "Input Required", "Please select a date to generate the report.");
            return;
        }

        reportData.clear();
        try (var c = DataSourceProvider.getConnection()) {
            ReportsService reportsService = new ReportsService(c);
            ResultSet rs = reportsService.getDailySales(selectedDate);
            while (rs.next()) {
                String name = rs.getString("name");
                int quantity = rs.getInt("qty");
                BigDecimal sales = rs.getBigDecimal("sales");
                reportData.add(new DailySalesReportItem(name, quantity, sales));
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to generate report: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Simple data class to hold aggregated report data for the table view.
     */
    public static class DailySalesReportItem {
        private final SimpleStringProperty name;
        private final SimpleIntegerProperty quantity;
        private final SimpleObjectProperty<BigDecimal> totalSales;

        public DailySalesReportItem(String name, int quantity, BigDecimal totalSales) {
            this.name = new SimpleStringProperty(name);
            this.quantity = new SimpleIntegerProperty(quantity);
            this.totalSales = new SimpleObjectProperty<>(totalSales);
        }

        // Getters are used by PropertyValueFactory
        public String getName() {
            return name.get();
        }

        public int getQuantity() {
            return quantity.get();
        }

        public BigDecimal getTotalSales() {
            return totalSales.get();
        }
    }
}
