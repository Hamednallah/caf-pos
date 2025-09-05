package com.yourcompany.cafeteria.ui;

import com.yourcompany.cafeteria.service.PrinterService;
import com.yourcompany.cafeteria.service.SettingsService;
import com.yourcompany.cafeteria.util.ConfigManager;
import com.yourcompany.cafeteria.util.DataSourceProvider;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class SettingsController implements Initializable {

    @FXML private ComboBox<String> printerCombo;
    @FXML private Button saveButton;
    @FXML private Button testPrintButton;
    @FXML private Label statusLabel;
    @FXML private TextField dbPathField;
    @FXML private Button browseDbPathButton;
    @FXML private Button backupButton;

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
        dbPathField.setText(ConfigManager.getProperty("db.url"));
    }

    @FXML
    private void handleSaveSettings() {
        try {
            settingsService.saveSetting("default_printer", printerCombo.getValue());
            ConfigManager.setProperty("db.url", dbPathField.getText());
            statusLabel.setText("Settings saved successfully. Restart the application for database path changes to take effect.");
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
            printerService.testPrint(selectedPrinter, resources);
            statusLabel.setText("Test print sent to " + selectedPrinter);
        } catch (Exception e) {
            statusLabel.setText("Failed to send test print.");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBackup() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Backup Location");
        File selectedDirectory = directoryChooser.showDialog(backupButton.getScene().getWindow());
        if (selectedDirectory != null) {
            try {
                String backupPath = new File(selectedDirectory, "backup.sql").getAbsolutePath();
                settingsService.backupDatabase(backupPath);
                statusLabel.setText("Backup created successfully at " + backupPath);
            } catch (Exception e) {
                statusLabel.setText("Failed to create backup.");
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleBrowseDbPath() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Database File");
        File selectedFile = fileChooser.showOpenDialog(browseDbPathButton.getScene().getWindow());
        if (selectedFile != null) {
            dbPathField.setText("jdbc:h2:file:" + selectedFile.getAbsolutePath().replace(".mv.db", ""));
        }
    }
}
