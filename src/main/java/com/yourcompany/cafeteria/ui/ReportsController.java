package com.yourcompany.cafeteria.ui;

import com.yourcompany.cafeteria.model.DateRangeReport;
import com.yourcompany.cafeteria.model.Shift;
import com.yourcompany.cafeteria.model.ShiftReport;
import com.yourcompany.cafeteria.service.ReportsService;
import com.yourcompany.cafeteria.service.ShiftService;
import com.yourcompany.cafeteria.util.DataSourceProvider;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;

import java.time.LocalDate;
import java.util.List;

public class ReportsController {

    @FXML private ComboBox<Shift> shiftSelectorComboBox;
    @FXML private Label shiftReportTotalSalesLabel;
    @FXML private Label shiftReportTotalDiscountsLabel;
    @FXML private Label shiftReportCashTotalLabel;
    @FXML private Label shiftReportBankTotalLabel;
    @FXML private Label shiftReportOrdersCountLabel;
    @FXML private Label shiftReportTotalExpensesLabel;

    @FXML private DatePicker dateFromPicker;
    @FXML private DatePicker dateToPicker;
    @FXML private Button generateDateRangeReportButton;
    @FXML private Label dateRangeReportTotalSalesLabel;
    @FXML private Label dateRangeReportOrdersCountLabel;

    private ReportsService reportsService;
    private ShiftService shiftService;

    @FXML
    public void initialize() {
        try {
            this.reportsService = new ReportsService(DataSourceProvider.getConnection());
            this.shiftService = new ShiftService(DataSourceProvider.getConnection());

            // Setup Shift Report Tab
            loadShiftsIntoComboBox();
            shiftSelectorComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    generateShiftReport(newSelection.getId());
                }
            });


            // Setup Date Range Report Tab
            dateToPicker.setValue(LocalDate.now());
            dateFromPicker.setValue(LocalDate.now().minusDays(7));
        } catch (Exception e) {
            showError("Database Error", "Failed to connect to the database.", e.getMessage());
        }
    }

    private void loadShiftsIntoComboBox() {
        try {
            List<Shift> shifts = shiftService.listAllShifts();
            shiftSelectorComboBox.setItems(FXCollections.observableArrayList(shifts));
        } catch (Exception e) {
            showError("Load Error", "Failed to load shifts.", e.getMessage());
        }
    }

    private void generateShiftReport(int shiftId) {
        try {
            ShiftReport report = reportsService.getShiftReport(shiftId);
            shiftReportTotalSalesLabel.setText(String.format("Total Sales: %.2f", report.getTotalSales()));
            shiftReportTotalDiscountsLabel.setText(String.format("Total Discounts: %.2f", report.getTotalDiscounts()));
            shiftReportCashTotalLabel.setText(String.format("Cash Total: %.2f", report.getCashTotal()));
            shiftReportBankTotalLabel.setText(String.format("Bank Total: %.2f", report.getBankTotal()));
            shiftReportOrdersCountLabel.setText(String.format("Orders Count: %d", report.getOrdersCount()));
            shiftReportTotalExpensesLabel.setText(String.format("Total Expenses: %.2f", report.getTotalExpenses()));
        } catch (Exception e) {
            showError("Report Generation Failed", "Could not generate the shift report.", e.getMessage());
        }
    }


    @FXML
    private void handleGenerateDateRangeReport() {
        LocalDate from = dateFromPicker.getValue();
        LocalDate to = dateToPicker.getValue();

        if (from == null || to == null) {
            showAlert(Alert.AlertType.WARNING, "Invalid Input", "Please select both a 'From' and 'To' date.");
            return;
        }

        try {
            DateRangeReport report = reportsService.getDateRangeReport(from, to);
            dateRangeReportTotalSalesLabel.setText(String.format("Total Sales: %.2f", report.getTotalSales()));
            dateRangeReportOrdersCountLabel.setText(String.format("Orders Count: %d", report.getOrdersCount()));
        } catch (Exception e) {
            showError("Report Generation Failed", "Could not generate the date range report.", e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
