package com.yourcompany.cafeteria.ui;

import com.yourcompany.cafeteria.model.*;
import com.yourcompany.cafeteria.service.ItemsService;
import com.yourcompany.cafeteria.service.OrdersService;
import com.yourcompany.cafeteria.service.ReturnsService;
import com.yourcompany.cafeteria.util.DataSourceProvider;
import com.yourcompany.cafeteria.util.SessionManager;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class SalesController implements Initializable {

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
    @FXML private Button saveButton;
    @FXML private Button saveAndPrintButton;
    @FXML private Button cancelButton;
    @FXML private TableView<Order> recentOrdersTable;
    @FXML private TableColumn<Order, Integer> orderIdCol;
    @FXML private TableColumn<Order, BigDecimal> orderTotalCol;
    @FXML private TextField orderIdSearchField;
    @FXML private Button orderSearchButton;

    private ItemsService itemsService;
    private OrdersService ordersService;
    private ReturnsService returnsService;
    private ObservableList<Item> allItems = FXCollections.observableArrayList();
    private ObservableList<Category> allCategories = FXCollections.observableArrayList();
    private ObservableList<OrderItem> cart = FXCollections.observableArrayList();
    private ObservableList<Order> recentOrders = FXCollections.observableArrayList();
    private ResourceBundle resources;
    private Integer orderIdToEdit = null;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.resources = resources;
        try {
            itemsService = new ItemsService(DataSourceProvider.getConnection());
            ordersService = new OrdersService(DataSourceProvider.getConnection());
            returnsService = new ReturnsService(DataSourceProvider.getConnection());
            setupCartTable();
            setupRecentOrdersTable();
            loadCategories();
            loadItems();
            loadRecentOrders();
            setupEventListeners();
            updateButtonStates();
            updateTotals();
        } catch (Exception e) {
            showError("Initialization Error", "Failed to initialize sales screen.", e.getMessage());
        }
    }

    private void updateButtonStates() {
        boolean shiftActive = SessionManager.isShiftActive();
        saveButton.setDisable(!shiftActive);
        saveAndPrintButton.setDisable(!shiftActive);
        cancelButton.setDisable(!shiftActive);
    }

    private void setupCartTable() {
        cartTable.setItems(cart);
        cartItemNameCol.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        cartQuantityCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        cartPriceCol.setCellValueFactory(new PropertyValueFactory<>("lineTotal"));
    }

    private void setupRecentOrdersTable() {
        recentOrdersTable.setItems(recentOrders);
        orderIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        orderTotalCol.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
    }

    private void loadCategories() throws Exception {
        allCategories.setAll(itemsService.getAllCategories());
        categoryListView.setItems(allCategories);
    }

    private void loadItems() throws Exception {
        allItems.setAll(itemsService.listAll());
        displayItems(allItems);
    }

    private void loadRecentOrders() throws Exception {
        recentOrders.setAll(ordersService.getRecentOrders(20));
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
                showAlert(Alert.AlertType.WARNING, resources.getString("sales.alert.noActiveShift.title"), resources.getString("sales.alert.noActiveShift.message"));
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
        recentOrdersTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                showOrderDetailsDialog(newVal);
            }
        });
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

        totalLabel.setText(resources.getString("sales.totalLabel") + " " + String.format("%.2f", total.max(BigDecimal.ZERO)));
    }

    @FXML
    private void handleSave() {
        saveOrder(false);
    }

    @FXML
    private void handleSaveAndPrint() {
        saveOrder(true);
    }

    @FXML
    private void handleCancel() {
        clearSale();
    }

    private void saveOrder(boolean printReceipt) {
        if (cart.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, resources.getString("sales.alert.emptyCart.title"), resources.getString("sales.alert.emptyCart.message"));
            return;
        }

        RadioButton selectedPaymentMethod = (RadioButton) paymentMethodToggleGroup.getSelectedToggle();
        if (selectedPaymentMethod == null) {
            showAlert(Alert.AlertType.WARNING, resources.getString("sales.alert.noPaymentMethod.title"), resources.getString("sales.alert.noPaymentMethod.message"));
            return;
        }

        if (SessionManager.getCurrentUser() == null) {
            showError(resources.getString("sales.error.noUser.title"), resources.getString("sales.error.noUser.header"), resources.getString("sales.error.noUser.content"));
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
            if (orderIdToEdit == null) {
                int id = ordersService.create(order);
                order.id = id;
                showAlert(Alert.AlertType.INFORMATION, resources.getString("sales.success.title"), MessageFormat.format(resources.getString("sales.success.message"), id));
            } else {
                order.id = orderIdToEdit;
                ordersService.updateOrder(order);
                showAlert(Alert.AlertType.INFORMATION, "Success", "Order #" + order.id + " updated successfully.");
            }

            if (printReceipt) {
                com.yourcompany.cafeteria.util.ReceiptPrinter.print(order);
            }

            clearSale();
            loadRecentOrders();
        } catch (Exception e) {
            showError(resources.getString("sales.error.saveFailed.title"), resources.getString("sales.error.saveFailed.header"), e.getMessage());
        }
    }

    private void clearSale() {
        cart.clear();
        discountField.clear();
        searchField.clear();
        orderIdToEdit = null;
        updateTotals();
    }

    @FXML
    private void handleOrderSearch() {
        String orderIdText = orderIdSearchField.getText();
        if (orderIdText.isEmpty()) {
            return;
        }
        try {
            int orderId = Integer.parseInt(orderIdText);
            Order order = ordersService.findOrderByIdAndDate(orderId, LocalDate.now());
            if (order != null) {
                showOrderDetailsDialog(order);
            } else {
                showAlert(Alert.AlertType.WARNING, resources.getString("sales.orderSearch.notFound.title"), MessageFormat.format(resources.getString("sales.orderSearch.notFound.message"), orderId));
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, resources.getString("sales.orderSearch.error.title"), "Order ID must be a number.");
        } catch (Exception e) {
            showError(resources.getString("sales.orderSearch.error.title"), resources.getString("sales.orderSearch.error.message"), e.getMessage());
        }
    }

    private void showOrderDetailsDialog(Order order) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(resources.getString("sales.orderDetails.title"));

        ButtonType returnButtonType = new ButtonType(resources.getString("sales.orderDetails.returnButton"));
        ButtonType editButtonType = new ButtonType(resources.getString("sales.orderDetails.editButton"));
        dialog.getDialogPane().getButtonTypes().addAll(returnButtonType, editButtonType, ButtonType.CANCEL);

        VBox vbox = new VBox();
        vbox.getChildren().add(new Label("Order ID: " + order.id));
        vbox.getChildren().add(new Label("Total: " + order.totalAmount));
        vbox.getChildren().add(new Label("Date: " + order.createdAt));
        dialog.getDialogPane().setContent(vbox);

        dialog.showAndWait().ifPresent(response -> {
            if (response == returnButtonType) {
                showReturnDialog(order);
            } else if (response == editButtonType) {
                editOrder(order);
            }
        });
    }

    private void editOrder(Order order) {
        clearSale();
        cart.addAll(order.items);
        discountField.setText(order.discountAmount.toPlainString());
        this.orderIdToEdit = order.id;
    }

    private void showReturnDialog(Order order) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ReturnDialog.fxml"), resources);
            DialogPane dialogPane = loader.load();

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle(resources.getString("sales.returns.dialog.title"));

            TableView<OrderItem> returnItemsTable = (TableView<OrderItem>) dialogPane.lookup("#returnItemsTable");
            TableColumn<OrderItem, String> itemNameCol = (TableColumn<OrderItem, String>) returnItemsTable.getColumns().get(0);
            TableColumn<OrderItem, Integer> quantitySoldCol = (TableColumn<OrderItem, Integer>) returnItemsTable.getColumns().get(1);
            TableColumn<OrderItem, TextField> quantityToReturnCol = (TableColumn<OrderItem, TextField>) returnItemsTable.getColumns().get(2);
            Label refundTotalLabel = (Label) dialogPane.lookup("#refundTotalLabel");

            itemNameCol.setCellValueFactory(new PropertyValueFactory<>("itemName"));
            quantitySoldCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));

            ObservableList<OrderItem> itemsToReturn = FXCollections.observableArrayList(order.items);
            returnItemsTable.setItems(itemsToReturn);

            quantityToReturnCol.setCellValueFactory(cellData -> {
                TextField textField = new TextField("0");
                textField.textProperty().addListener((obs, oldVal, newVal) -> {
                    // Update total refund amount
                    BigDecimal totalRefund = BigDecimal.ZERO;
                    for (OrderItem item : returnItemsTable.getItems()) {
                        TextField tf = (TextField) quantityToReturnCol.getCellObservableValue(item).getValue();
                        int qtyToReturn = 0;
                        try {
                            qtyToReturn = Integer.parseInt(tf.getText());
                        } catch (NumberFormatException e) {
                            // ignore
                        }
                        totalRefund = totalRefund.add(item.getPriceAtPurchase().multiply(new BigDecimal(qtyToReturn)));
                    }
                    refundTotalLabel.setText(resources.getString("sales.returns.dialog.totalRefund") + " " + String.format("%.2f", totalRefund));
                });
                return new SimpleObjectProperty<>(textField);
            });


            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent() && result.get().getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                List<ReturnItem> returnItems = new ArrayList<>();
                BigDecimal totalRefund = BigDecimal.ZERO;

                for (OrderItem item : returnItemsTable.getItems()) {
                    TextField tf = (TextField) quantityToReturnCol.getCellObservableValue(item).getValue();
                    int qtyToReturn = Integer.parseInt(tf.getText());
                    if (qtyToReturn > 0) {
                        ReturnItem returnItem = new ReturnItem();
                        returnItem.setOrderItemId(item.getId());
                        returnItem.setQuantityReturned(qtyToReturn);
                        returnItems.add(returnItem);
                        totalRefund = totalRefund.add(item.getPriceAtPurchase().multiply(new BigDecimal(qtyToReturn)));
                    }
                }

                if (!returnItems.isEmpty()) {
                    Return newReturn = new Return();
                    newReturn.setOrderId(order.id);
                    newReturn.setProcessedByUserId(SessionManager.getCurrentUser().getId());
                    newReturn.setTotalRefundAmount(totalRefund);
                    newReturn.setReturnItems(returnItems);

                    returnsService.processReturn(newReturn);
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Return processed successfully.");
                }
            }

        } catch (Exception e) {
            showError("Error", "Could not open return dialog.", e.getMessage());
        }
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
