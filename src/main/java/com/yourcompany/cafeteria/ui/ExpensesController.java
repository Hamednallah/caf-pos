package com.yourcompany.cafeteria.ui;

import com.yourcompany.cafeteria.model.Expense;
import com.yourcompany.cafeteria.service.ExpenseService;
import com.yourcompany.cafeteria.util.DataSourceProvider;
import com.yourcompany.cafeteria.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;

import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class ExpensesController implements Initializable {

    @FXML private Label shiftInfoLabel;
    @FXML private TableView<Expense> expensesTable;
    @FXML private TableColumn<Expense, Integer> idCol;
    @FXML private TableColumn<Expense, String> descriptionCol;
    @FXML private TableColumn<Expense, BigDecimal> amountCol;
    @FXML private TableColumn<Expense, LocalDateTime> dateCol;
    @FXML private Button addExpenseButton;

    private ExpenseService expenseService;
    private ResourceBundle resources;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.resources = resources;
        try {
            expenseService = new ExpenseService(DataSourceProvider.getConnection());
            setupTable();
            loadExpenses();
            updateUIState();
        } catch (Exception e) {
            e.printStackTrace(); // Handle error
        }
    }

    private void setupTable() {
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        descriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        amountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
        dateCol.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
    }

    private void loadExpenses() throws Exception {
        if (SessionManager.isShiftActive()) {
            List<Expense> expenses = expenseService.getExpensesByShift(SessionManager.getCurrentShiftId());
            expensesTable.setItems(FXCollections.observableArrayList(expenses));
        }
    }

    private void updateUIState() {
        boolean shiftActive = SessionManager.isShiftActive();
        addExpenseButton.setDisable(!shiftActive);
        if (shiftActive) {
            shiftInfoLabel.setText("Shift #" + SessionManager.getCurrentShiftId() + " is active.");
        } else {
            shiftInfoLabel.setText(resources.getString("shifts.noActive"));
        }
    }

    @FXML
    private void handleAddExpense() {
        Dialog<Expense> dialog = new Dialog<>();
        dialog.setTitle("Add Expense");
        dialog.setHeaderText("Enter expense details:");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

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
                Expense newExpense = new Expense();
                newExpense.setDescription(descriptionField.getText());
                newExpense.setAmount(new BigDecimal(amountField.getText()));
                newExpense.setShiftId(SessionManager.getCurrentShiftId());
                newExpense.setRecordedBy(SessionManager.getCurrentUser().getId());
                return newExpense;
            }
            return null;
        });

        Optional<Expense> result = dialog.showAndWait();
        result.ifPresent(expense -> {
            try {
                expenseService.recordExpense(expense);
                loadExpenses();
            } catch (Exception e) {
                e.printStackTrace(); // Handle error
            }
        });
    }
}
