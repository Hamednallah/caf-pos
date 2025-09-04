package com.yourcompany.cafeteria.ui;

import com.yourcompany.cafeteria.model.User;
import com.yourcompany.cafeteria.service.UsersService;
import com.yourcompany.cafeteria.util.DataSourceProvider;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class UsersController {

    @FXML private TableView<User> usersTable;
    @FXML private TableColumn<User, Integer> idColumn;
    @FXML private TableColumn<User, String> usernameColumn;
    @FXML private TableColumn<User, String> fullNameColumn;
    @FXML private TableColumn<User, Integer> roleIdColumn;
    @FXML private TableColumn<User, Boolean> activeColumn;

    private UsersService usersService;

    @FXML
    public void initialize() {
        try {
            this.usersService = new UsersService(DataSourceProvider.getConnection());
            setupTableColumns();
            loadUsers();
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Database Error", "Failed to connect to the database.");
        }
    }

    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        fullNameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        roleIdColumn.setCellValueFactory(new PropertyValueFactory<>("roleId"));
        activeColumn.setCellValueFactory(new PropertyValueFactory<>("active"));
    }

    private void loadUsers() {
        try {
            List<User> users = usersService.listAll();
            usersTable.setItems(FXCollections.observableArrayList(users));
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Load Error", "Failed to load users from the database.");
        }
    }

    @FXML
    private void handleAddUser() {
        showUserDialog(new User());
    }

    @FXML
    private void handleEditUser() {
        User selectedUser = usersTable.getSelectionModel().getSelectedItem();
        if (selectedUser != null) {
            showUserDialog(selectedUser);
        } else {
            showAlert("No Selection", "Please select a user to edit.");
        }
    }

    @FXML
    private void handleToggleActive() {
        User selectedUser = usersTable.getSelectionModel().getSelectedItem();
        if (selectedUser != null) {
            try {
                usersService.toggleUserStatus(selectedUser.getId());
                loadUsers(); // Refresh the table
            } catch (SQLException e) {
                e.printStackTrace();
                showError("Database Error", "Failed to update user status.");
            }
        } else {
            showAlert("No Selection", "Please select a user to toggle their status.");
        }
    }

    private void showUserDialog(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/UserDialog.fxml"));
            Stage dialogStage = new Stage();
            dialogStage.setTitle(user.getId() == null ? "Add User" : "Edit User");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(usersTable.getScene().getWindow());
            Scene scene = new Scene(loader.load());
            dialogStage.setScene(scene);

            // Setup Dialog Fields
            TextField usernameField = (TextField) scene.lookup("#usernameField");
            TextField fullNameField = (TextField) scene.lookup("#fullNameField");
            PasswordField passwordField = (PasswordField) scene.lookup("#passwordField");
            ComboBox<Integer> roleComboBox = (ComboBox<Integer>) scene.lookup("#roleComboBox");
            CheckBox activeCheckBox = (CheckBox) scene.lookup("#activeCheckBox");

            roleComboBox.setItems(FXCollections.observableArrayList(1, 2)); // 1: Admin, 2: Cashier

            if (user.getId() != null) {
                usernameField.setText(user.getUsername());
                fullNameField.setText(user.getFullName());
                roleComboBox.setValue(user.getRoleId());
                activeCheckBox.setSelected(user.isActive());
                passwordField.setPromptText("Leave blank to keep current password");
            } else {
                activeCheckBox.setSelected(true);
            }

            // Setup Buttons
            Button saveButton = (Button) scene.lookup("#saveButton");
            saveButton.setOnAction(e -> {
                try {
                    user.setUsername(usernameField.getText());
                    user.setFullName(fullNameField.getText());
                    user.setRoleId(roleComboBox.getValue());
                    user.setActive(activeCheckBox.isSelected());

                    String password = passwordField.getText();

                    if (user.getId() == null) {
                        usersService.createUser(user, password);
                    } else {
                        usersService.updateUser(user, password);
                    }
                    loadUsers();
                    dialogStage.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    showError("Save Error", "Could not save user. Check all fields.");
                }
            });

            Button cancelButton = (Button) scene.lookup("#cancelButton");
            cancelButton.setOnAction(e -> dialogStage.close());

            dialogStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            showError("Dialog Error", "Could not open the user dialog.");
        }
    }


    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
