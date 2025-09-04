package com.yourcompany.cafeteria.ui;

import com.yourcompany.cafeteria.model.User;
import com.yourcompany.cafeteria.service.UsersService;
import com.yourcompany.cafeteria.util.DataSourceProvider;
import com.yourcompany.cafeteria.util.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label status;

    @FXML
    public void handleLogin() {
        try (var c = DataSourceProvider.getConnection()) {
            var svc = new UsersService(c);
            User authenticatedUser = svc.authenticate(usernameField.getText(), passwordField.getText());

            if (authenticatedUser != null) {
                if (!authenticatedUser.isActive()) {
                    status.setText("User account is inactive.");
                    return;
                }

                // Store user info in session
                SessionManager.setCurrentUser(authenticatedUser);

                // Load the main application window
                Stage stage = (Stage) usernameField.getScene().getWindow();
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainView.fxml"));
                stage.setScene(new Scene(loader.load(), 1200, 800));
                stage.setTitle("Cafeteria POS");
                stage.setMaximized(true);

            } else {
                status.setText("Invalid username or password.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            status.setText("An error occurred during login.");
        }
    }
}
