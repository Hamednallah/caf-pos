package com.yourcompany.cafeteria.ui;

import com.yourcompany.cafeteria.model.ShiftSummary;
import com.yourcompany.cafeteria.service.ReportsService;
import com.yourcompany.cafeteria.service.ShiftService;
import com.yourcompany.cafeteria.util.DataSourceProvider;
import com.yourcompany.cafeteria.util.SessionManager;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.Optional;

public class ShiftsController {

    @FXML private Label shiftStatusLabel;
    @FXML private Button startShiftButton;
    @FXML private Button endShiftButton;

    @FXML
    public void initialize() {
        checkForActiveShift();
        updateUIState();
    }

    private void checkForActiveShift() {
        if (SessionManager.isShiftActive()) {
            return;
        }

        try (var c = DataSourceProvider.getConnection()) {
            ShiftService shiftService = new ShiftService(c);
            Integer cashierId = SessionManager.getCurrentCashierId();
            if (cashierId != null) {
                ResultSet rs = shiftService.getActiveShiftForCashier(cashierId);
                if (rs.next()) {
                    SessionManager.setCurrentShiftId(rs.getInt("id"));
                }
            }
        } catch (Exception e) {
            showError("Database Error", "Failed to check for active shift.", e.getMessage());
        }
    }

    private void updateUIState() {
        boolean shiftActive = SessionManager.isShiftActive();
        shiftStatusLabel.setText(shiftActive ? "Shift #" + SessionManager.getCurrentShiftId() + " is active." : "No active shift.");
        startShiftButton.setDisable(shiftActive);
        endShiftButton.setDisable(!shiftActive);
    }

    @FXML
    private void handleStartShift() {
        TextInputDialog dialog = new TextInputDialog("0.00");
        dialog.setTitle("Start New Shift");
        dialog.setHeaderText("Enter Starting Cash Float");
        dialog.setContentText("Please enter the amount of cash in the drawer:");

        dialog.showAndWait().ifPresent(floatAmountStr -> {
            try {
                BigDecimal startingFloat = new BigDecimal(floatAmountStr);
                if (startingFloat.compareTo(BigDecimal.ZERO) < 0) {
                    showError("Invalid Input", "Starting float cannot be negative.", "");
                    return;
                }
                try (var c = DataSourceProvider.getConnection()) {
                    ShiftService shiftService = new ShiftService(c);
                    int newShiftId = shiftService.startShift(SessionManager.getCurrentCashierId(), startingFloat);
                    SessionManager.setCurrentShiftId(newShiftId);
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Shift #" + newShiftId + " started successfully.");
                    updateUIState();
                } catch (Exception e) {
                    showError("Failed to Start Shift", "Could not start a new shift.", e.getMessage());
                }
            } catch (NumberFormatException e) {
                showError("Invalid Input", "Please enter a valid number for the starting float.", "");
            }
        });
    }

    @FXML
    private void handleEndShift() {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("End Shift");
        confirmation.setHeaderText("Are you sure you want to end the current shift?");
        confirmation.setContentText("This will generate a final report for Shift #" + SessionManager.getCurrentShiftId());

        confirmation.showAndWait().filter(r -> r == ButtonType.OK).ifPresent(r -> {
            try (var c = DataSourceProvider.getConnection()) {
                // 1. Generate Summary
                ReportsService reportsService = new ReportsService(c);
                ShiftSummary summary = reportsService.getShiftSummary(SessionManager.getCurrentShiftId());

                // 2. Display Summary
                showShiftSummaryDialog(summary);

                // 3. End the shift in the database
                ShiftService shiftService = new ShiftService(c);
                shiftService.endShift(SessionManager.getCurrentShiftId());

                showAlert(Alert.AlertType.INFORMATION, "Success", "Shift #" + SessionManager.getCurrentShiftId() + " has been ended.");

                SessionManager.setCurrentShiftId(null);
                updateUIState();
            } catch (Exception e) {
                showError("Failed to End Shift", "Could not generate summary or end the shift.", e.getMessage());
            }
        });
    }

    private void showShiftSummaryDialog(ShiftSummary summary) {
        Alert dialog = new Alert(Alert.AlertType.INFORMATION);
        dialog.setTitle("Shift Summary Report");
        dialog.setHeaderText("Summary for Shift #" + summary.getShiftId());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        grid.add(new Label("Starting Float:"), 0, 0);
        grid.add(new Label(String.format("%.2f", summary.getStartingFloat())), 1, 0);
        grid.add(new Label("Total Cash Sales:"), 0, 1);
        grid.add(new Label(String.format("%.2f", summary.getTotalCashSales())), 1, 1);
        grid.add(new Label("Total Bank Sales:"), 0, 2);
        grid.add(new Label(String.format("%.2f", summary.getTotalBankSales())), 1, 2);
        grid.add(new Label("Total Expenses:"), 0, 3);
        grid.add(new Label(String.format("- %.2f", summary.getTotalExpenses())), 1, 3);
        grid.add(new Label("Expected Cash in Drawer:"), 0, 4);
        Label expectedCashLabel = new Label(String.format("%.2f", summary.getExpectedCashInDrawer()));
        expectedCashLabel.setStyle("-fx-font-weight: bold;");
        grid.add(expectedCashLabel, 1, 4);

        dialog.getDialogPane().setContent(grid);
        dialog.showAndWait();
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
