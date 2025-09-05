package com.yourcompany.cafeteria.ui;

import com.yourcompany.cafeteria.model.Expense;
import com.yourcompany.cafeteria.service.ExpenseService;
import com.yourcompany.cafeteria.util.DataSourceProvider;
import com.yourcompany.cafeteria.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.util.Pair;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.Optional;

public class ExpensesController {

    @FXML private Label shiftInfoLabel;
    @FXML private TableView<Expense> expensesTable;
    @FXML private TableColumn<Expense, Integer> idCol;
    @FXML private TableColumn<Expense, String> descriptionCol;
    @FXML private TableColumn<Expense, BigDecimal> amountCol;
    @FXML private TableColumn<Expense, LocalDateTime> dateCol;
    @FXML private Button addExpenseButton;

    private ObservableList<Expense> expenseList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTable();
        if (SessionManager.isShiftActive()) {
            shiftInfoLabel.setText("Managing expenses for Shift #" + SessionManager.getCurrentShiftId());
            shiftInfoLabel.setStyle("-fx-text-fill: green;");
            addExpenseButton.setDisable(false);
            loadExpenses();
        } else {
            shiftInfoLabel.setText("No active shift. Please start a shift to manage expenses.");
            shiftInfoLabel.setStyle("-fx-text-fill: red;");
            addExpenseButton.setDisable(true);
        }
    }

    private void setupTable() {
        expensesTable.setItems(expenseList);
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        descriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        amountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
        dateCol.setCellValueFactory(new PropertyValueFactory<>("recordedAt"));
    }

    private void loadExpenses() {
        expenseList.clear();
        try (var c = DataSourceProvider.getConnection()) {
            ExpenseService expenseService = new ExpenseService(c);
            ResultSet rs = expenseService.getExpensesByShift(SessionManager.getCurrentShiftId());
            while (rs.next()) {
                Expense expense = new Expense();
                expense.setId(rs.getInt("id"));
                expense.setDescription(rs.getString("description"));
                expense.setAmount(rs.getBigDecimal("amount"));
                expense.setRecordedAt(rs.getTimestamp("recorded_at").toLocalDateTime());
                expense.setShiftId(rs.getInt("shift_id"));
                expense.setUserId(rs.getInt("user_id"));
                expenseList.add(expense);
            }
        } catch (Exception e) {
            showError("Database Error", "Failed to load expenses.", e.getMessage());
        }
    }

    @FXML
    private void handleAddExpense() {
        showExpenseDialog().ifPresent(result -> {
            String description = result.getKey();
            BigDecimal amount = result.getValue();

            Expense newExpense = new Expense();
            newExpense.setDescription(description);
            newExpense.setAmount(amount);
            newExpense.setShiftId(SessionManager.getCurrentShiftId());
            if (SessionManager.getCurrentUser() != null) {
                newExpense.setUserId(SessionManager.getCurrentUser().getId());
            }
            newExpense.setRecordedAt(LocalDateTime.now());

            try (var c = DataSourceProvider.getConnection()) {
                ExpenseService expenseService = new ExpenseService(c);
                int id = expenseService.recordExpense(newExpense);
                newExpense.setId(id);
                expenseList.add(newExpense);
                showAlert(Alert.AlertType.INFORMATION, "Success", "Expense recorded successfully.");
            } catch (Exception e) {
                showError("Add Expense Failed", "Could not save the new expense.", e.getMessage());
            }
        });
    }

    private Optional<Pair<String, BigDecimal>> showExpenseDialog() {
        Dialog<Pair<String, BigDecimal>> dialog = new Dialog<>();
        dialog.setTitle("Add New Expense");
        dialog.setHeaderText("Enter expense details:");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField descriptionField = new TextField();
        descriptionField.setPromptText("Description");
        TextField amountField = new TextField();
        amountField.setPromptText("Amount");

        grid.add(new Label("Description:"), 0, 0);
        grid.add(descriptionField, 1, 0);
        grid.add(new Label("Amount:"), 0, 1);
        grid.add(amountField, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    String description = descriptionField.getText();
                    BigDecimal amount = new BigDecimal(amountField.getText());
                    if (description.isEmpty() || amount.compareTo(BigDecimal.ZERO) <= 0) {
                        showError("Invalid Input", "Description cannot be empty and amount must be positive.", "");
                        return null;
                    }
                    return new Pair<>(description, amount);
                } catch (NumberFormatException e) {
                    showError("Invalid Input", "Amount must be a valid number.", "");
                    return null;
                }
            }
            return null;
        });

        return dialog.showAndWait();
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
