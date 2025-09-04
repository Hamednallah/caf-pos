package com.yourcompany.cafeteria.ui;

import com.yourcompany.cafeteria.service.StartupService;
import com.yourcompany.cafeteria.util.DataSourceProvider;
import com.yourcompany.cafeteria.util.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;

public class MainController {

    @FXML
    private BorderPane contentPane;
    @FXML
    private Button usersButton;

    @FXML
    public void initialize() {
        // Perform startup checks before loading any views
        try (var c = DataSourceProvider.getConnection()) {
            StartupService startupService = new StartupService(c);
            startupService.checkAndResumeActiveShift();
        } catch (SQLException e) {
            e.printStackTrace();
            // In a real app, you would show a fatal error dialog and possibly exit.
        }

        // Apply role-based access control
        if (SessionManager.getCurrentUser() != null && SessionManager.getCurrentUser().getRoleId() == 1) {
            usersButton.setVisible(true);
            usersButton.setManaged(true);
        } else {
            usersButton.setVisible(false);
            usersButton.setManaged(false);
        }

        // Load the default view on startup
        showSalesView();
    }

    @FXML
    private void showSalesView() {
        loadView("/fxml/SalesView.fxml");
    }

    @FXML
    private void showItemsView() {
        loadView("/fxml/ItemsView.fxml");
    }

    @FXML
    private void showShiftsView() {
        loadView("/fxml/ShiftsView.fxml");
    }

    @FXML
    private void showExpensesView() {
        loadView("/fxml/ExpensesView.fxml");
    }

    @FXML
    private void showReportsView() {
        loadView("/fxml/ReportsView.fxml");
    }

    @FXML
    private void showUsersView() {
        loadView("/fxml/UsersView.fxml");
    }

    @FXML
    private void showSettingsView() {
        loadView("/fxml/SettingsView.fxml");
    }

    private void loadView(String fxmlPath) {
        try {
            URL url = getClass().getResource(fxmlPath);
            if (url == null) {
                throw new IOException("Cannot find FXML file: " + fxmlPath);
            }
            Parent view = FXMLLoader.load(url);
            contentPane.setCenter(view);
        } catch (IOException e) {
            e.printStackTrace();
            // In a real app, show an error alert
        }
    }
}
