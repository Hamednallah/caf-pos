package com.yourcompany.cafeteria.ui;

import com.yourcompany.cafeteria.model.User;
import com.yourcompany.cafeteria.service.UsersService;
import com.yourcompany.cafeteria.util.DataSourceProvider;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class UsersController {

    @FXML
    private TableView<User> usersTable;
    @FXML
    private TableColumn<User, String> usernameColumn;
    @FXML
    private TableColumn<User, String> fullNameColumn;
    @FXML
    private TableColumn<User, Integer> roleColumn;
    @FXML
    private TableColumn<User, Boolean> activeColumn;

    private UsersService usersService;

    @FXML
    public void initialize() {
        try {
            usersService = new UsersService(DataSourceProvider.getConnection());
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to connect to the database.");
            return;
        }

        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        fullNameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("roleId"));
        activeColumn.setCellValueFactory(new PropertyValueFactory<>("active"));

        loadUsers();
    }

    private void loadUsers() {
        try {
            List<User> users = usersService.listAllUsers();
            usersTable.setItems(FXCollections.observableArrayList(users));
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load users.");
        }
    }

    @FXML
    private void handleAddUser() {
        showUserDialog(null);
    }

    @FXML
    private void handleEditUser() {
        User selectedUser = usersTable.getSelectionModel().getSelectedItem();
        if (selectedUser != null) {
            showUserDialog(selectedUser);
        } else {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a user to edit.");
        }
    }

    @FXML
    private void handleDeactivateUser() {
        User selectedUser = usersTable.getSelectionModel().getSelectedItem();
        if (selectedUser != null) {
            try {
                usersService.updateUserStatus(selectedUser.getId(), !selectedUser.isActive());
                loadUsers();
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update user status.");
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a user to deactivate/reactivate.");
        }
    }

    private void showUserDialog(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/UserDialog.fxml"));
            DialogPane dialogPane = loader.load();

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle(user == null ? "Add User" : "Edit User");

            TextField usernameField = (TextField) dialogPane.lookup("#usernameField");
            TextField fullNameField = (TextField) dialogPane.lookup("#fullNameField");
            PasswordField passwordField = (PasswordField) dialogPane.lookup("#passwordField");
            ComboBox<String> roleComboBox = (ComboBox<String>) dialogPane.lookup("#roleComboBox");
            CheckBox activeCheckBox = (CheckBox) dialogPane.lookup("#activeCheckBox");

            // Assuming roles: 1=Admin, 2=Cashier. This should be improved later.
            roleComboBox.setItems(FXCollections.observableArrayList("Admin", "Cashier"));

            if (user != null) {
                usernameField.setText(user.getUsername());
                fullNameField.setText(user.getFullName());
                roleComboBox.getSelectionModel().select(user.getRoleId() == 1 ? "Admin" : "Cashier");
                activeCheckBox.setSelected(user.isActive());
                passwordField.setPromptText("Leave empty to keep current password");
            } else {
                activeCheckBox.setSelected(true);
            }

            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent() && result.get().getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                String password = passwordField.getText();
                String passwordHash = (password != null && !password.isEmpty()) ? BCrypt.hashpw(password, BCrypt.gensalt()) : (user != null ? user.getPasswordHash() : null);

                User updatedUser = user == null ? new User() : user;
                updatedUser.setUsername(usernameField.getText());
                updatedUser.setFullName(fullNameField.getText());
                updatedUser.setRoleId(roleComboBox.getSelectionModel().getSelectedItem().equals("Admin") ? 1 : 2);
                updatedUser.setActive(activeCheckBox.isSelected());
                if (passwordHash != null) {
                    updatedUser.setPasswordHash(passwordHash);
                }

                if (user == null) {
                    usersService.createUser(updatedUser.getUsername(), updatedUser.getPasswordHash(), updatedUser.getFullName(), updatedUser.getRoleId());
                } else {
                    usersService.updateUser(updatedUser);
                }

                loadUsers();
            }

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Could not load the user dialog.");
            e.printStackTrace();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to save user.");
            e.printStackTrace();
        }
    }



    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
