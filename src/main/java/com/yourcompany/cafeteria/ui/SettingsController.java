package com.yourcompany.cafeteria.ui;

import com.yourcompany.cafeteria.service.PrinterService;
import com.yourcompany.cafeteria.service.SettingsService;
import com.yourcompany.cafeteria.util.DataSourceProvider;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.ResourceBundle;

public class SettingsController implements Initializable {

    @FXML private ComboBox<String> printerCombo;
    @FXML private Button saveButton;
    @FXML private Button testPrintButton;
    @FXML private Label statusLabel;

    private SettingsService settingsService;
    private PrinterService printerService;
    private ResourceBundle resources;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.resources = resources;
        try {
            settingsService = new SettingsService(DataSourceProvider.getConnection());
            printerService = new PrinterService();
            loadPrinters();
            loadSettings();
        } catch (Exception e) {
            statusLabel.setText("Error initializing settings.");
            e.printStackTrace();
        }
    }

    private void loadPrinters() {
        printerCombo.setItems(FXCollections.observableArrayList(printerService.getAvailablePrinters()));
    }

    private void loadSettings() throws Exception {
        String defaultPrinter = settingsService.getSetting("default_printer");
        if (defaultPrinter != null) {
            printerCombo.setValue(defaultPrinter);
        }
    }

    @FXML
    private void handleSaveSettings() {
        try {
            settingsService.saveSetting("default_printer", printerCombo.getValue());
            statusLabel.setText("Settings saved successfully.");
        } catch (Exception e) {
            statusLabel.setText("Error saving settings.");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleTestPrint() {
        try {
            String selectedPrinter = printerCombo.getValue();
            if (selectedPrinter == null || selectedPrinter.isEmpty()) {
                statusLabel.setText("Please select a printer first.");
                return;
            }
            printerService.testPrint(selectedPrinter);
            statusLabel.setText("Test print sent to " + selectedPrinter);
        } catch (Exception e) {
            statusLabel.setText("Failed to send test print.");
            e.printStackTrace();
        }
    }
}
