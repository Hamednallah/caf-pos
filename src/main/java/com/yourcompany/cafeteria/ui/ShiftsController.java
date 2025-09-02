package com.yourcompany.cafeteria.ui;

import com.yourcompany.cafeteria.service.ShiftService;
import com.yourcompany.cafeteria.util.DataSourceProvider;
import com.yourcompany.cafeteria.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;

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
            return; // Already have a shift ID in session
        }

        try (var c = DataSourceProvider.getConnection()) {
            ShiftService shiftService = new ShiftService(c);
            Integer cashierId = SessionManager.getCurrentCashierId();
            if (cashierId != null) {
                ResultSet rs = shiftService.getActiveShiftForCashier(cashierId);
                if (rs.next()) {
                    int activeShiftId = rs.getInt("id");
                    SessionManager.setCurrentShiftId(activeShiftId);
                }
            }
        } catch (Exception e) {
            showError("Database Error", "Failed to check for active shift.", e.getMessage());
        }
    }

    private void updateUIState() {
        boolean shiftActive = SessionManager.isShiftActive();
        if (shiftActive) {
            shiftStatusLabel.setText("Shift #" + SessionManager.getCurrentShiftId() + " is active.");
        } else {
            shiftStatusLabel.setText("No active shift.");
        }
        startShiftButton.setDisable(shiftActive);
        endShiftButton.setDisable(!shiftActive);
    }

    @FXML
    private void handleStartShift() {
        TextInputDialog dialog = new TextInputDialog("0.00");
        dialog.setTitle("Start New Shift");
        dialog.setHeaderText("Enter Starting Cash Float");
        dialog.setContentText("Please enter the amount of cash in the drawer:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(floatAmountStr -> {
            try {
                BigDecimal startingFloat = new BigDecimal(floatAmountStr);
                if (startingFloat.compareTo(BigDecimal.ZERO) < 0) {
                    showError("Invalid Input", "Starting float cannot be negative.", "");
                    return;
                }

                try (var c = DataSourceProvider.getConnection()) {
                    ShiftService shiftService = new ShiftService(c);
                    int cashierId = SessionManager.getCurrentCashierId();
                    int newShiftId = shiftService.startShift(cashierId, startingFloat);
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
        confirmation.setContentText("Shift #" + SessionManager.getCurrentShiftId());

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try (var c = DataSourceProvider.getConnection()) {
                    ShiftService shiftService = new ShiftService(c);
                    shiftService.endShift(SessionManager.getCurrentShiftId());

                    showAlert(Alert.AlertType.INFORMATION, "Success", "Shift #" + SessionManager.getCurrentShiftId() + " has been ended.");

                    SessionManager.setCurrentShiftId(null);
                    updateUIState();
                } catch (Exception e) {
                    showError("Failed to End Shift", "Could not end the current shift.", e.getMessage());
                }
            }
        });
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
