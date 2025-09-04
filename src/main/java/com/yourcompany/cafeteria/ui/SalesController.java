package com.yourcompany.cafeteria.ui;

import com.yourcompany.cafeteria.model.Item;
import com.yourcompany.cafeteria.model.Order;
import com.yourcompany.cafeteria.model.OrderItem;
import com.yourcompany.cafeteria.service.ItemsService;
import com.yourcompany.cafeteria.service.OrdersService;
import com.yourcompany.cafeteria.util.DataSourceProvider;
import com.yourcompany.cafeteria.util.ReceiptPrinter;
import com.yourcompany.cafeteria.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class SalesController {

    @FXML private TextField searchField;
    @FXML private ListView<Item> itemCatalogView;
    @FXML private TableView<OrderItem> cartTable;
    @FXML private TableColumn<OrderItem, String> cartItemNameCol;
    @FXML private TableColumn<OrderItem, Integer> cartQuantityCol;
    @FXML private TableColumn<OrderItem, BigDecimal> cartPriceCol;
    @FXML private TextField discountField;
    @FXML private Label totalLabel;
    @FXML private Button finalizeButton;

    private ObservableList<Item> itemCatalogMasterList = FXCollections.observableArrayList();
    private FilteredList<Item> filteredItemCatalog;
    private ObservableList<OrderItem> cart = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupCatalog();
        setupCartTable();
        setupSearchFilter();
        setupEventListeners();

        // Disable finalize button if no shift is active
        finalizeButton.setDisable(!SessionManager.isShiftActive());

        loadCatalog();
    }

    private void setupCatalog() {
        filteredItemCatalog = new FilteredList<>(itemCatalogMasterList, p -> true);
        itemCatalogView.setItems(filteredItemCatalog);
        itemCatalogView.setCellFactory(listView -> new ListCell<Item>() {
            @Override
            protected void updateItem(Item item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getName() + " (" + item.getPrice() + ")");
                }
            }
        });
    }

    private void setupCartTable() {
        cartTable.setItems(cart);
        cartItemNameCol.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        cartQuantityCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        cartPriceCol.setCellValueFactory(new PropertyValueFactory<>("lineTotal"));
    }

    private void setupSearchFilter() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredItemCatalog.setPredicate(item -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                String lowerCaseFilter = newValue.toLowerCase();
                if (item.getName() == null) return false;
                return item.getName().toLowerCase().contains(lowerCaseFilter);
            });
        });
    }

    private void setupEventListeners() {
        itemCatalogView.setOnMouseClicked(event -> {
            if (!SessionManager.isShiftActive()) {
                showAlert(Alert.AlertType.WARNING, "No Active Shift", "You must start a shift before making a sale.");
                return;
            }
            Item selectedItem = itemCatalogView.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                addToCart(selectedItem);
                itemCatalogView.getSelectionModel().clearSelection();
            }
        });

        cart.addListener((javafx.collections.ListChangeListener.Change<? extends OrderItem> c) -> updateTotals());
        discountField.textProperty().addListener((obs) -> updateTotals());
    }

    private void loadCatalog() {
        try (var c = DataSourceProvider.getConnection()) {
            ItemsService itemsService = new ItemsService(c);
            itemCatalogMasterList.setAll(itemsService.listAll());
        } catch (Exception e) {
            showError("Database Error", "Failed to load item catalog.", e.getMessage());
        }
    }

    private void addToCart(Item item) {
        Optional<OrderItem> existingCartItem = cart.stream()
                .filter(orderItem -> orderItem.getItemId() == item.getId())
                .findFirst();

        if (existingCartItem.isPresent()) {
            OrderItem orderItem = existingCartItem.get();
            orderItem.setQuantity(orderItem.getQuantity() + 1);
            cartTable.refresh();
        } else {
            OrderItem newOrderItem = new OrderItem();
            newOrderItem.setItemId(item.getId());
            newOrderItem.setItemName(item.getName());
            newOrderItem.setQuantity(1);
            newOrderItem.setPriceAtPurchase(item.getPrice());
            cart.add(newOrderItem);
        }
    }

    private void updateTotals() {
        BigDecimal total = cart.stream()
                .map(OrderItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        try {
            BigDecimal discount = new BigDecimal(discountField.getText());
            total = total.subtract(discount);
        } catch (NumberFormatException e) {
            // Ignore if discount is not a valid number
        }

        totalLabel.setText(String.format("%.2f", total.max(BigDecimal.ZERO)));
    }

    @FXML
    public void handleFinalize() {
        if (cart.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Empty Cart", "Cannot finalize an empty order.");
            return;
        }

        List<String> paymentChoices = Arrays.asList("CASH", "BANK");
        ChoiceDialog<String> dialog = new ChoiceDialog<>("CASH", paymentChoices);
        dialog.setTitle("Payment Confirmation");
        dialog.setHeaderText("Finalize Sale");
        dialog.setContentText("Choose payment method:");
        Optional<String> result = dialog.showAndWait();

        if (result.isEmpty()) {
            return; // User cancelled
        }

        if (SessionManager.getCurrentUser() == null) {
            showError("Error", "No user logged in.", "Cannot finalize order.");
            return;
        }

        Order order = new Order();
        order.cashierId = SessionManager.getCurrentUser().getId();
        order.shiftId = SessionManager.getCurrentShiftId();
        order.status = "FINALIZED";
        order.paymentMethod = result.get();
        order.paymentConfirmed = true;
        order.items = new ArrayList<>(cart);
        order.createdAt = LocalDateTime.now();

        order.totalAmount = cart.stream()
            .map(OrderItem::getLineTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        try {
             order.discountAmount = new BigDecimal(discountField.getText());
        } catch (NumberFormatException e) {
             order.discountAmount = BigDecimal.ZERO;
        }

        try (var c = DataSourceProvider.getConnection()) {
            OrdersService ordersService = new OrdersService(c);
            int id = ordersService.create(order);
            order.id = id;

            showAlert(Alert.AlertType.INFORMATION, "Success", "Order #" + id + " created successfully.");

            ReceiptPrinter.print(order);

            clearSale();

        } catch (Exception e) {
            showError("Failed to Save Order", "There was an error saving the order to the database.", e.getMessage());
        }
    }

    private void clearSale() {
        cart.clear();
        discountField.clear();
        searchField.clear();
        updateTotals();
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
