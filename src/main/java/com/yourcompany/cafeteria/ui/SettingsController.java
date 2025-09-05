package com.yourcompany.cafeteria.ui;

import com.yourcompany.cafeteria.MainApp;
import com.yourcompany.cafeteria.service.SettingsService;
import com.yourcompany.cafeteria.util.DataSourceProvider;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;

public class SettingsController implements ResourceAwareController {

    @FXML private ComboBox<String> themeComboBox;

    private SettingsService settingsService;
    private MainApp mainApp;

    @Override
    public void setResources(java.util.ResourceBundle resourceBundle) {
        // Not currently used, but required by the interface
    }

    @FXML
    public void initialize() {
        try {
            settingsService = new SettingsService(DataSourceProvider.getConnection());
            themeComboBox.setItems(FXCollections.observableArrayList("Light", "Dark"));
            String currentTheme = settingsService.get("ui.theme");
            themeComboBox.setValue(currentTheme != null ? currentTheme : "Light");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    @FXML
    private void handleSaveSettings() {
        try {
            String selectedTheme = themeComboBox.getValue();
            settingsService.set("ui.theme", selectedTheme);

            // Show a confirmation alert
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Settings Saved");
            alert.setHeaderText(null);
            alert.setContentText("Settings have been saved. The theme will be applied on the next application restart.");
            alert.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Could not save settings.");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }
}
