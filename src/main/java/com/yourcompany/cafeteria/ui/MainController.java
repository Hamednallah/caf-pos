package com.yourcompany.cafeteria.ui;

import com.yourcompany.cafeteria.MainApp;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.StackPane;
import javafx.util.StringConverter;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

public class MainController implements ResourceAwareController {

    @FXML private StackPane mainContent;
    @FXML private ComboBox<Locale> languageComboBox;
    @FXML private ToggleButton salesButton;
    @FXML private ToggleButton usersButton;
    @FXML private Label currentUserLabel;
    @FXML private Label shiftStatusLabel;

    private ResourceBundle resourceBundle;
    private MainApp mainApp;

    @FXML
    public void initialize() {
        // Perform startup checks before loading any views
        try (var c = com.yourcompany.cafeteria.util.DataSourceProvider.getConnection()) {
            com.yourcompany.cafeteria.service.StartupService startupService = new com.yourcompany.cafeteria.service.StartupService(c);
            startupService.checkAndResumeActiveShift();
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }

        setupLanguageComboBox();
        updateUserInfo();

        // Select the sales view by default
        if (salesButton != null) {
            salesButton.setSelected(true);
            handleSales(null);
        }
    }

    public void updateUserInfo() {
        // Apply role-based access control
        if (com.yourcompany.cafeteria.util.SessionManager.getCurrentUser() != null) {
            currentUserLabel.setText(com.yourcompany.cafeteria.util.SessionManager.getCurrentUser().getFullName());
            if (com.yourcompany.cafeteria.util.SessionManager.getCurrentUser().getRoleId() == 1) { // ADMIN
                usersButton.setVisible(true);
                usersButton.setManaged(true);
            } else {
                usersButton.setVisible(false);
                usersButton.setManaged(false);
            }
        } else {
            currentUserLabel.setText("Not Logged In");
            usersButton.setVisible(false);
            usersButton.setManaged(false);
        }

        // Update shift status
        if (com.yourcompany.cafeteria.util.SessionManager.isShiftActive()) {
            shiftStatusLabel.setText("Shift #" + com.yourcompany.cafeteria.util.SessionManager.getCurrentShiftId() + " Active");
        } else {
            shiftStatusLabel.setText("Shift: Inactive");
        }
    }

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    @Override
    public void setResources(ResourceBundle resourceBundle) {
        this.resourceBundle = resourceBundle;
    }

    private void setupLanguageComboBox() {
        languageComboBox.getItems().addAll(new Locale("en", "US"), new Locale("ar", "SA"));
        languageComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Locale locale) {
                return locale.getDisplayLanguage(locale);
            }

            @Override
            public Locale fromString(String string) {
                return null; // Not needed
            }
        });

        // Set initial value based on current locale
        if (Locale.getDefault().getLanguage().equals("ar")) {
            languageComboBox.setValue(new Locale("ar", "SA"));
        } else {
            languageComboBox.setValue(new Locale("en", "US"));
        }


        languageComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.equals(oldVal)) {
                mainApp.switchLanguage(newVal);
            }
        });
    }

    @FXML
    private void handleSales(ActionEvent event) {
        loadView("/com/yourcompany/cafeteria/fxml/SalesView.fxml");
    }

    @FXML
    private void handleItems(ActionEvent event) {
        loadView("/com/yourcompany/cafeteria/fxml/ItemsView.fxml");
    }

    @FXML
    private void handleReports(ActionEvent event) {
        loadView("/com/yourcompany/cafeteria/fxml/ReportsView.fxml");
    }

    @FXML
    private void handleShifts(ActionEvent event) {
        loadView("/com/yourcompany/cafeteria/fxml/ShiftsView.fxml");
    }

    @FXML
    private void handleExpenses(ActionEvent event) {
        loadView("/com/yourcompany/cafeteria/fxml/ExpensesView.fxml");
    }

    @FXML
    private void handleUsers(ActionEvent event) {
        loadView("/com/yourcompany/cafeteria/fxml/UsersView.fxml");
    }

    @FXML
    private void handleSettings(ActionEvent event) {
        loadView("/com/yourcompany/cafeteria/fxml/SettingsView.fxml");
    }


    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath), resourceBundle);
            Parent view = loader.load();

            Object controller = loader.getController();
            if (controller instanceof ResourceAwareController) {
                ((ResourceAwareController) controller).setResources(resourceBundle);
            }

            mainContent.getChildren().setAll(view);
        } catch (IOException e) {
            System.err.println("Failed to load view: " + fxmlPath);
            e.printStackTrace();
            // In a real app, show an error alert
        }
    }
}
