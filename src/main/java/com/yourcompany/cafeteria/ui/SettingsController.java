package com.yourcompany.cafeteria.ui;

import com.yourcompany.cafeteria.service.SettingsService;
import com.yourcompany.cafeteria.util.DataSourceProvider;
import com.yourcompany.cafeteria.util.EscPosBuilder;
import com.yourcompany.cafeteria.util.ReceiptPrinter;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;

public class SettingsController {

    @FXML private ComboBox<String> printerCombo;
    @FXML private Label statusLabel;

    @FXML
    public void initialize() {
        loadPrinters();
        loadCurrentSettings();
    }

    private void loadPrinters() {
        try {
            String[] printers = ReceiptPrinter.listPrinters();
            if (printers == null || printers.length == 0) {
                // Headless CI or no printers installed: provide a virtual entry so UI remains testable
                printers = new String[]{"Virtual Test Printer"};
            }
            printerCombo.setItems(FXCollections.observableArrayList(printers));
        } catch (Exception e) {
            showError("Error Loading Printers", "Could not retrieve the list of system printers.", e.getMessage());
        }
    }

    private void loadCurrentSettings() {
        try (var c = DataSourceProvider.getConnection()) {
            SettingsService settingsService = new SettingsService(c);
            String defaultPrinter = settingsService.get("printer.default");
            if (defaultPrinter != null && !defaultPrinter.isEmpty()) {
                printerCombo.setValue(defaultPrinter);
            }
        } catch (Exception e) {
            showError("Error Loading Settings", "Could not load current application settings.", e.getMessage());
        }
    }

    @FXML
    public void handleSaveSettings() {
        String selectedPrinter = printerCombo.getValue();
        if (selectedPrinter == null || selectedPrinter.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "No Printer Selected", "Please select a printer before saving.");
            return;
        }

        try (var c = DataSourceProvider.getConnection()) {
            SettingsService settingsService = new SettingsService(c);
            settingsService.set("printer.default", selectedPrinter);
            showAlert(Alert.AlertType.INFORMATION, "Settings Saved", "Default printer has been set to: " + selectedPrinter);
            statusLabel.setText("Saved successfully.");
        } catch (Exception e) {
            showError("Error Saving Settings", "Could not save the default printer setting.", e.getMessage());
        }
    }

    @FXML
    public void handleTestPrint() {
        try {
            EscPosBuilder builder = new EscPosBuilder();
            byte[] testReceipt = builder
                .alignCenter()
                .bold(true)
                .append("CAFETERIA POS")
                .feedLine()
                .bold(false)
                .append("This is a test print.")
                .feedLine()
                .append("------------------------------------------")
                .feedLine()
                .alignLeft()
                .append("Left aligned text.")
                .feedLine()
                .alignCenter()
                .append("Center aligned text.")
                .feedLine()
                .alignRight()
                .append("Right aligned text.")
                .feedLine()
                .underline(true)
                .append("Underlined text.")
                .underline(false)
                .feedLine()
                .feedLines(3)
                .cut()
                .getBytes();

            ReceiptPrinter.print(testReceipt);
            showAlert(Alert.AlertType.INFORMATION, "Test Print Sent", "A test print job has been sent to the default printer.");
            statusLabel.setText("Test print sent successfully.");
        } catch (Exception e) {
            showError("Test Print Failed", "Could not send the test print.", e.getMessage());
            statusLabel.setText("Print failed: " + e.getMessage());
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
