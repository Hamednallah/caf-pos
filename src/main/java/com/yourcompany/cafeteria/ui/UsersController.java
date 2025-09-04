package com.yourcompany.cafeteria.ui;

import com.yourcompany.cafeteria.model.User;
import com.yourcompany.cafeteria.service.UsersService;
import com.yourcompany.cafeteria.util.DataSourceProvider;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;
import java.util.List;

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
    private TableColumn<User, Integer> roleIdColumn;
    @FXML
    private TableColumn<User, Boolean> activeColumn;

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
        // TODO: Implement Add User Dialog
    }

    @FXML
    private void handleEditUser() {
        // TODO: Implement Edit User Dialog
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
