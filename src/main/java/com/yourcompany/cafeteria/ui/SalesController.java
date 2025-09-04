package com.yourcompany.cafeteria.ui;

import com.yourcompany.cafeteria.model.Category;
import com.yourcompany.cafeteria.model.Item;
import com.yourcompany.cafeteria.model.Order;
import com.yourcompany.cafeteria.model.OrderItem;
import com.yourcompany.cafeteria.service.ItemsService;
import com.yourcompany.cafeteria.service.OrdersService;
import com.yourcompany.cafeteria.util.DataSourceProvider;
import com.yourcompany.cafeteria.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SalesController {

    @FXML private TextField searchField;
    @FXML private ListView<Category> categoryListView;
    @FXML private TilePane itemGridPane;
    @FXML private TableView<OrderItem> cartTable;
    @FXML private TableColumn<OrderItem, String> cartItemNameCol;
    @FXML private TableColumn<OrderItem, Integer> cartQuantityCol;
    @FXML private TableColumn<OrderItem, BigDecimal> cartPriceCol;
    @FXML private TextField discountField;
    @FXML private Label totalLabel;
    @FXML private ToggleGroup paymentMethodToggleGroup;
    @FXML private RadioButton cashRadioButton;
    @FXML private RadioButton bankRadioButton;
    @FXML private Button finalizeButton;
    @FXML private Button removeButton;
    @FXML private Button changeQuantityButton;

    private ItemsService itemsService;
    private OrdersService ordersService;
    private ObservableList<Item> allItems = FXCollections.observableArrayList();
    private ObservableList<Category> allCategories = FXCollections.observableArrayList();
    private ObservableList<OrderItem> cart = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        try {
            itemsService = new ItemsService(DataSourceProvider.getConnection());
            ordersService = new OrdersService(DataSourceProvider.getConnection());
            setupCartTable();
            loadCategories();
            loadItems();
            setupEventListeners();
            finalizeButton.setDisable(!SessionManager.isShiftActive());
        } catch (Exception e) {
            showError("Initialization Error", "Failed to initialize sales screen.", e.getMessage());
        }
    }

    private void setupCartTable() {
        cartTable.setItems(cart);
        cartItemNameCol.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        cartQuantityCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        cartPriceCol.setCellValueFactory(new PropertyValueFactory<>("lineTotal"));
    }

    private void loadCategories() throws Exception {
        allCategories.setAll(itemsService.getAllCategories());
        categoryListView.setItems(allCategories);
    }

    private void loadItems() throws Exception {
        allItems.setAll(itemsService.listAll());
        displayItems(allItems);
    }

    private void displayItems(List<Item> itemsToDisplay) {
        itemGridPane.getChildren().clear();
        for (Item item : itemsToDisplay) {
            itemGridPane.getChildren().add(createItemCard(item));
        }
    }

    private Node createItemCard(Item item) {
        VBox card = new VBox(5);
        card.setAlignment(Pos.CENTER);
        card.setStyle("-fx-background-color: -fx-surface; -fx-padding: 10; -fx-border-color: -fx-subtle-border; -fx-border-width: 1; -fx-background-radius: 8; -fx-border-radius: 8;");
        card.setPrefSize(120, 100);

        Label nameLabel = new Label(item.getName());
        nameLabel.setStyle("-fx-font-weight: bold;");
        Label priceLabel = new Label(String.format("%.2f", item.getPrice()));

        card.getChildren().addAll(nameLabel, priceLabel);

        card.setOnMouseClicked(event -> {
            if (!SessionManager.isShiftActive()) {
                showAlert(Alert.AlertType.WARNING, "No Active Shift", "You must start a shift before making a sale.");
                return;
            }
            addToCart(item);
        });

        return card;
    }

    private void setupEventListeners() {
        cart.addListener((javafx.collections.ListChangeListener.Change<? extends OrderItem> c) -> updateTotals());
        discountField.textProperty().addListener((obs) -> updateTotals());

        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterItems());
        categoryListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> filterItems());
    }

    private void filterItems() {
        Category selectedCategory = categoryListView.getSelectionModel().getSelectedItem();
        String searchText = searchField.getText().toLowerCase().trim();

        List<Item> filteredItems = allItems.stream()
                .filter(item -> {
                    boolean categoryMatch = selectedCategory == null || item.getCategoryId() == selectedCategory.getId();
                    boolean searchMatch = searchText.isEmpty() || item.getName().toLowerCase().contains(searchText);
                    return categoryMatch && searchMatch;
                })
                .toList();

        displayItems(filteredItems);
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

    @FXML
    private void handleRemoveItem() {
        OrderItem selectedItem = cartTable.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            cart.remove(selectedItem);
        } else {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select an item from the cart to remove.");
        }
    }

    @FXML
    private void handleChangeQuantity() {
        OrderItem selectedItem = cartTable.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            TextInputDialog dialog = new TextInputDialog(String.valueOf(selectedItem.getQuantity()));
            dialog.setTitle("Change Quantity");
            dialog.setHeaderText("Enter the new quantity for " + selectedItem.getItemName());
            dialog.setContentText("Quantity:");

            dialog.showAndWait().ifPresent(quantityStr -> {
                try {
                    int newQuantity = Integer.parseInt(quantityStr);
                    if (newQuantity > 0) {
                        selectedItem.setQuantity(newQuantity);
                        cartTable.refresh();
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Invalid Quantity", "Quantity must be greater than zero.");
                    }
                } catch (NumberFormatException e) {
                    showAlert(Alert.AlertType.ERROR, "Invalid Input", "Please enter a valid number.");
                }
            });
        } else {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select an item from the cart to change its quantity.");
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

        totalLabel.setText(String.format("Total: %.2f", total.max(BigDecimal.ZERO)));
    }

    @FXML
    public void handleFinalize() {
        if (cart.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Empty Cart", "Cannot finalize an empty order.");
            return;
        }

        RadioButton selectedPaymentMethod = (RadioButton) paymentMethodToggleGroup.getSelectedToggle();
        if (selectedPaymentMethod == null) {
            showAlert(Alert.AlertType.WARNING, "Payment Method", "Please select a payment method.");
            return;
        }

        if (SessionManager.getCurrentUser() == null) {
            showError("Error", "No user logged in.", "Cannot finalize order.");
            return;
        }

        Order order = new Order();
        order.cashierId = SessionManager.getCurrentUser().getId();
        order.shiftId = SessionManager.getCurrentShiftId();
        order.status = "FINALIZED";
        order.paymentMethod = selectedPaymentMethod.getText().toUpperCase();
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

        try {
            int id = ordersService.create(order);
            showAlert(Alert.AlertType.INFORMATION, "Success", "Order #" + id + " created successfully.");
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
