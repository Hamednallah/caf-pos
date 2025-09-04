package com.yourcompany.cafeteria.ui;

import com.yourcompany.cafeteria.model.User;
import com.yourcompany.cafeteria.service.UsersService;
import com.yourcompany.cafeteria.util.DataSourceProvider;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class UsersController {

    @FXML
    private TableView<User> usersTable;
    @FXML
    private TableColumn<User, Integer> idColumn;
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
            this.usersService = new UsersService(DataSourceProvider.getConnection());
            setupTableColumns();
            loadUsers();
        } catch (Exception e) {
            e.printStackTrace(); // Replace with proper error handling
        }
    }

    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        fullNameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("roleId")); // You might want to map this to 'ADMIN'/'CASHIER' string
        activeColumn.setCellValueFactory(new PropertyValueFactory<>("active"));
    }

    private void loadUsers() {
        try {
            List<User> users = usersService.listAll();
            usersTable.setItems(FXCollections.observableArrayList(users));
        } catch (Exception e) {
            e.printStackTrace(); // Replace with proper error handling
        }
    }

    @FXML
    private void handleAddUser() {
        showUserDialog(null).ifPresent(newUser -> {
            try {
                // The password is part of the result from the dialog
                String password = (String) newUser.get("password");
                User user = (User) newUser.get("user");
                usersService.createUser(user, password);
                loadUsers(); // Refresh the table
            } catch (Exception e) {
                e.printStackTrace(); // Show error alert
            }
        });
    }

    @FXML
    private void handleEditUser() {
        User selectedUser = usersTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a user to edit.");
            return;
        }

        showUserDialog(selectedUser).ifPresent(updatedUserInfo -> {
            try {
                String password = (String) updatedUserInfo.get("password");
                User userToUpdate = (User) updatedUserInfo.get("user");
                usersService.updateUser(userToUpdate, password);
                loadUsers(); // Refresh the table
            } catch (Exception e) {
                e.printStackTrace(); // Show error alert
            }
        });
    }

    @FXML
    private void handleDeactivateUser() {
        User selectedUser = usersTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a user to deactivate/reactivate.");
            return;
        }

        String action = selectedUser.isActive() ? "Deactivate" : "Reactivate";
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle(action + " User");
        confirmation.setHeaderText("Are you sure you want to " + action.toLowerCase() + " the user '" + selectedUser.getUsername() + "'?");

        confirmation.showAndWait().filter(r -> r == ButtonType.OK).ifPresent(r -> {
            try {
                usersService.toggleUserStatus(selectedUser.getId());
                loadUsers(); // Refresh the table
            } catch (Exception e) {
                e.printStackTrace(); // Show error alert
            }
        });
    }

    private Optional<java.util.Map<String, Object>> showUserDialog(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/UserDialog.fxml"));
            DialogPane dialogPane = loader.load();

            UserDialogController controller = loader.getController();
            if (user != null) {
                controller.setUser(user);
            }

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle(user == null ? "Add User" : "Edit User");

            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent() && result.get().getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                User updatedUser = (user == null) ? new User() : user;
                controller.updateUser(updatedUser);

                java.util.Map<String, Object> resultMap = new java.util.HashMap<>();
                resultMap.put("user", updatedUser);
                resultMap.put("password", controller.getPassword());
                return Optional.of(resultMap);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return Optional.empty();
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
