package com.yourcompany.cafeteria.ui;

import com.yourcompany.cafeteria.model.ShiftReport;
import com.yourcompany.cafeteria.service.ReportsService;
import com.yourcompany.cafeteria.service.ShiftService;
import com.yourcompany.cafeteria.util.DataSourceProvider;
import com.yourcompany.cafeteria.util.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.math.BigDecimal;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Optional;
import java.util.ResourceBundle;

public class ShiftsController implements Initializable {

    @FXML private Label shiftStatusLabel;
    @FXML private Button startShiftButton;
    @FXML private Button endShiftButton;

    private ResourceBundle resources;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.resources = resources;
        updateUIState();
    }

    private void updateUIState() {
        boolean shiftActive = SessionManager.isShiftActive();
        if (shiftActive) {
            shiftStatusLabel.setText(MessageFormat.format(resources.getString("shifts.started"), SessionManager.getCurrentShiftId()));
        } else {
            shiftStatusLabel.setText(resources.getString("shifts.noActive"));
        }
        startShiftButton.setDisable(shiftActive);
        endShiftButton.setDisable(!shiftActive);
    }

    @FXML
    private void handleStartShift() {
        TextInputDialog dialog = new TextInputDialog("0.00");
        dialog.setTitle(resources.getString("shifts.dialog.start.title"));
        dialog.setHeaderText(resources.getString("shifts.dialog.start.header"));
        dialog.setContentText(resources.getString("shifts.dialog.start.content"));

        dialog.showAndWait().ifPresent(floatAmountStr -> {
            try {
                BigDecimal startingFloat = new BigDecimal(floatAmountStr);
                if (startingFloat.compareTo(BigDecimal.ZERO) < 0) {
                    showError(resources.getString("shifts.error.invalidFloat"), resources.getString("shifts.error.negativeFloat"), "");
                    return;
                }
                if (SessionManager.getCurrentUser() == null) {
                    showError("Error", resources.getString("shifts.error.noUser"), resources.getString("shifts.error.cannotStart"));
                    return;
                }
                try (var c = DataSourceProvider.getConnection()) {
                    ShiftService shiftService = new ShiftService(c);
                    int newShiftId = shiftService.startShift(SessionManager.getCurrentUser().getId(), startingFloat);
                    SessionManager.setCurrentShiftId(newShiftId);
                    showAlert(Alert.AlertType.INFORMATION, resources.getString("shifts.success"), MessageFormat.format(resources.getString("shifts.started"), newShiftId));
                    updateUIState();
                } catch (Exception e) {
                    showError(resources.getString("shifts.error.failedToStart"), resources.getString("shifts.error.couldNotStart"), e.getMessage());
                }
            } catch (NumberFormatException e) {
                showError(resources.getString("shifts.error.invalidFloat"), "Please enter a valid number for the starting float.", "");
            }
        });
    }

    @FXML
    private void handleEndShift() {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle(resources.getString("shifts.dialog.end.title"));
        confirmation.setHeaderText(resources.getString("shifts.dialog.end.header"));
        confirmation.setContentText(MessageFormat.format(resources.getString("shifts.dialog.end.content"), SessionManager.getCurrentShiftId()));

        confirmation.showAndWait().filter(r -> r == ButtonType.OK).ifPresent(r -> {
            try (var c = DataSourceProvider.getConnection()) {
                ReportsService reportsService = new ReportsService(c);
                ShiftReport summary = reportsService.getShiftReport(SessionManager.getCurrentShiftId());

                showShiftSummaryDialog(summary);

                ShiftService shiftService = new ShiftService(c);
                shiftService.endShift(SessionManager.getCurrentShiftId());

                showAlert(Alert.AlertType.INFORMATION, resources.getString("shifts.success"), MessageFormat.format(resources.getString("shifts.ended"), SessionManager.getCurrentShiftId()));

                SessionManager.setCurrentShiftId(null);
                updateUIState();
            } catch (Exception e) {
                showError(resources.getString("shifts.error.failedToEnd"), resources.getString("shifts.error.couldNotEnd"), e.getMessage());
            }
        });
    }

    private void showShiftSummaryDialog(ShiftReport summary) {
        Alert dialog = new Alert(Alert.AlertType.INFORMATION);
        dialog.setTitle(resources.getString("shifts.summary.title"));
        dialog.setHeaderText(MessageFormat.format(resources.getString("shifts.summary.header"), SessionManager.getCurrentShiftId()));

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        grid.add(new Label(resources.getString("shifts.summary.startingFloat")), 0, 0);
        grid.add(new Label(String.format("%.2f", summary.getStartingFloat())), 1, 0);
        grid.add(new Label(resources.getString("shifts.summary.totalCashSales")), 0, 1);
        grid.add(new Label(String.format("%.2f", summary.getCashTotal())), 1, 1);
        grid.add(new Label(resources.getString("shifts.summary.totalBankSales")), 0, 2);
        grid.add(new Label(String.format("%.2f", summary.getBankTotal())), 1, 2);
        grid.add(new Label(resources.getString("shifts.summary.totalExpenses")), 0, 3);
        grid.add(new Label(String.format("- %.2f", summary.getTotalExpenses())), 1, 3);

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
