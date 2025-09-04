package com.yourcompany.cafeteria.ui;

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
import javafx.stage.FileChooser;

import java.io.File;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.time.LocalDate;

public class ReportsController {

    @FXML private DatePicker datePicker;
    @FXML private TableView<DailySalesReportItem> reportTable;
    @FXML private TableColumn<DailySalesReportItem, String> itemNameCol;
    @FXML private TableColumn<DailySalesReportItem, Integer> quantityCol;
    @FXML private TableColumn<DailySalesReportItem, BigDecimal> salesCol;
    @FXML private Button exportButton;

    private ObservableList<DailySalesReportItem> reportData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTable();
        datePicker.setValue(LocalDate.now());
        exportButton.disableProperty().bind(Bindings.isEmpty(reportTable.getItems()));
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
                reportData.add(new DailySalesReportItem(
                    rs.getString("name"),
                    rs.getInt("qty"),
                    rs.getBigDecimal("sales")
                ));
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to generate report: " + e.getMessage());
        }
    }

    @FXML
    private void handleExportCsv() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Report");
        fileChooser.setInitialFileName("daily_sales_" + datePicker.getValue() + ".csv");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fileChooser.showSaveDialog(reportTable.getScene().getWindow());

        if (file != null) {
            try (PrintWriter writer = new PrintWriter(file)) {
                // Write header
                writer.println("Item Name,Total Quantity Sold,Total Sales");
                // Write data
                for (DailySalesReportItem item : reportData) {
                    writer.printf("\"%s\",%d,%.2f%n",
                        item.getName().replace("\"", "\"\""), // Escape quotes
                        item.getQuantity(),
                        item.getTotalSales()
                    );
                }
                showAlert(Alert.AlertType.INFORMATION, "Export Successful", "Report saved to " + file.getAbsolutePath());
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Export Failed", "Could not save the report to the file: " + e.getMessage());
            }
        }
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
