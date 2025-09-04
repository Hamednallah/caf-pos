package com.yourcompany.cafeteria.ui;

import com.yourcompany.cafeteria.model.Order;
import com.yourcompany.cafeteria.model.OrderItem;
import com.yourcompany.cafeteria.model.ReturnedItem;
import com.yourcompany.cafeteria.service.OrdersService;
import com.yourcompany.cafeteria.service.ReturnsService;
import com.yourcompany.cafeteria.util.DataSourceProvider;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.IntegerStringConverter;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ReturnsController {

    @FXML private TextField orderIdField;
    @FXML private Label orderSummaryLabel;
    @FXML private TableView<OrderItem> orderItemsTable;
    @FXML private TableColumn<OrderItem, String> itemNameColumn;
    @FXML private TableColumn<OrderItem, Integer> quantityColumn;
    @FXML private TableColumn<OrderItem, Integer> returnQuantityColumn;
    @FXML private Button processReturnButton;

    private OrdersService ordersService;
    private ReturnsService returnsService;
    private Order currentOrder;

    @FXML
    public void initialize() {
        try {
            ordersService = new OrdersService(DataSourceProvider.getConnection());
            returnsService = new ReturnsService();
            setupTable();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Initialization Error", "Could not initialize returns view.");
        }
    }

    private void setupTable() {
        itemNameColumn.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));

        returnQuantityColumn.setCellValueFactory(cellData -> cellData.getValue().returnQuantityProperty().asObject());
        returnQuantityColumn.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        returnQuantityColumn.setEditable(true);
        orderItemsTable.setEditable(true);
    }

    @FXML
    private void handleFindOrder() {
        clearForm();
        try {
            int orderId = Integer.parseInt(orderIdField.getText());
            currentOrder = ordersService.findById(orderId);
            if (currentOrder != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                orderSummaryLabel.setText("Order #" + currentOrder.getId() + " | Date: " + currentOrder.getCreatedAt().format(formatter));
                orderItemsTable.setItems(FXCollections.observableArrayList(currentOrder.getItems()));
                processReturnButton.setDisable(false);
            } else {
                showAlert(Alert.AlertType.WARNING, "Not Found", "Order with ID " + orderId + " not found.");
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Invalid Input", "Please enter a valid Order ID.");
        } catch (Exception e) {
            e.printStackTrace();
            showError("Find Order Error", "Could not retrieve order details.");
        }
    }

    @FXML
    private void handleProcessReturn() {
        List<ReturnedItem> itemsToReturn = orderItemsTable.getItems().stream()
                .filter(item -> item.getReturnQuantity() > 0)
                .map(item -> {
                    ReturnedItem returnedItem = new ReturnedItem();
                    returnedItem.setOrderItemId(item.getId());
                    returnedItem.setQuantity(item.getReturnQuantity());
                    return returnedItem;
                })
                .collect(Collectors.toList());

        if (itemsToReturn.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "No Items", "Please specify a return quantity for at least one item.");
            return;
        }

        try {
            returnsService.processReturn(currentOrder.getId(), "Customer return", itemsToReturn);
            showAlert(Alert.AlertType.INFORMATION, "Success", "Return processed successfully for Order #" + currentOrder.getId());
            clearForm();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Return Processing Error", "Could not process the return.");
        }
    }

    private void clearForm() {
        currentOrder = null;
        orderSummaryLabel.setText("");
        orderItemsTable.getItems().clear();
        processReturnButton.setDisable(true);
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
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
