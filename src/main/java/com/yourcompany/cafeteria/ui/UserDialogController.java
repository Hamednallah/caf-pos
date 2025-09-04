package com.yourcompany.cafeteria.ui;

import com.yourcompany.cafeteria.model.User;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class UserDialogController {

    @FXML
    private TextField usernameField;
    @FXML
    private TextField fullNameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private ComboBox<String> roleComboBox;
    @FXML
    private CheckBox activeCheckBox;

    public void initialize() {
        roleComboBox.getItems().addAll("Admin", "Cashier");
    }

    public void setUser(User user) {
        usernameField.setText(user.getUsername());
        fullNameField.setText(user.getFullName());
        // Password field is not pre-filled for security
        roleComboBox.getSelectionModel().select(user.getRoleId() == 1 ? "Admin" : "Cashier");
        activeCheckBox.setSelected(user.isActive());
    }

    public void updateUser(User user) {
        user.setUsername(usernameField.getText());
        user.setFullName(fullNameField.getText());
        // Password is handled separately
        user.setRoleId("Admin".equals(roleComboBox.getSelectionModel().getSelectedItem()) ? 1 : 2);
        user.setActive(activeCheckBox.isSelected());
    }

    public String getPassword() {
        return passwordField.getText();
    }
}
