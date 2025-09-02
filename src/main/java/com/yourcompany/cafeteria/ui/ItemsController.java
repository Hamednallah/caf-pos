package com.yourcompany.cafeteria.ui;

import com.yourcompany.cafeteria.model.Item;
import com.yourcompany.cafeteria.service.ItemsService;
import com.yourcompany.cafeteria.util.DataSourceProvider;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;

import java.math.BigDecimal;
import java.util.Optional;

public class ItemsController {

    @FXML private TableView<Item> itemsTable;
    @FXML private TableColumn<Item, Integer> idCol;
    @FXML private TableColumn<Item, String> nameCol;
    @FXML private TableColumn<Item, BigDecimal> priceCol;
    @FXML private TableColumn<Item, Integer> categoryCol; // Changed to Integer
    @FXML private TableColumn<Item, String> imagePathCol;

    @FXML private Button editButton;
    @FXML private Button deleteButton;

    private ObservableList<Item> itemsList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTable();
        setupButtonBindings();
        loadItems();
    }

    private void setupTable() {
        itemsTable.setItems(itemsList);
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("categoryId")); // Changed to categoryId
        imagePathCol.setCellValueFactory(new PropertyValueFactory<>("imagePath"));
    }

    private void setupButtonBindings() {
        editButton.disableProperty().bind(itemsTable.getSelectionModel().selectedItemProperty().isNull());
        deleteButton.disableProperty().bind(itemsTable.getSelectionModel().selectedItemProperty().isNull());
    }

    private void loadItems() {
        try (var c = DataSourceProvider.getConnection()) {
            ItemsService itemsService = new ItemsService(c);
            itemsList.setAll(itemsService.listAll());
        } catch (Exception e) {
            showError("Database Error", "Failed to load items.", e.getMessage());
        }
    }

    @FXML
    private void handleAddItem() {
        showItemDialog(new Item()).ifPresent(item -> {
            try (var c = DataSourceProvider.getConnection()) {
                ItemsService itemsService = new ItemsService(c);
                int id = itemsService.add(item);
                item.setId(id);
                itemsList.add(item);
            } catch (Exception e) {
                showError("Add Item Failed", "Could not save the new item.", e.getMessage());
            }
        });
    }

    @FXML
    private void handleEditItem() {
        Item selectedItem = itemsTable.getSelectionModel().getSelectedItem();
        if (selectedItem == null) return;

        showItemDialog(selectedItem).ifPresent(item -> {
            try (var c = DataSourceProvider.getConnection()) {
                ItemsService itemsService = new ItemsService(c);
                itemsService.update(item);
                itemsTable.refresh();
            } catch (Exception e) {
                showError("Update Item Failed", "Could not update the selected item.", e.getMessage());
            }
        });
    }

    @FXML
    private void handleDeleteItem() {
        Item selectedItem = itemsTable.getSelectionModel().getSelectedItem();
        if (selectedItem == null) return;

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Delete Item");
        confirmation.setHeaderText("Are you sure you want to delete this item?");
        confirmation.setContentText(selectedItem.getName());

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try (var c = DataSourceProvider.getConnection()) {
                    ItemsService itemsService = new ItemsService(c);
                    itemsService.delete(selectedItem.getId());
                    itemsList.remove(selectedItem);
                } catch (Exception e) {
                    showError("Delete Item Failed", "Could not delete the selected item.", e.getMessage());
                }
            }
        });
    }

    private Optional<Item> showItemDialog(Item item) {
        Dialog<Item> dialog = new Dialog<>();
        dialog.setTitle(item.getId() == null ? "Add New Item" : "Edit Item");
        dialog.setHeaderText("Enter item details:");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField(item.getName());
        nameField.setPromptText("Name");
        TextField priceField = new TextField(item.getPrice() != null ? item.getPrice().toPlainString() : "");
        priceField.setPromptText("Price");
        TextField categoryIdField = new TextField(item.getCategoryId() != null ? item.getCategoryId().toString() : "");
        categoryIdField.setPromptText("Category ID");
        TextField imagePathField = new TextField(item.getImagePath());
        imagePathField.setPromptText("Image Path (optional)");

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Price:"), 0, 1);
        grid.add(priceField, 1, 1);
        grid.add(new Label("Category ID:"), 0, 2);
        grid.add(categoryIdField, 1, 2);
        grid.add(new Label("Image Path:"), 0, 3);
        grid.add(imagePathField, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    item.setName(nameField.getText());
                    item.setPrice(new BigDecimal(priceField.getText()));
                    if (categoryIdField.getText() != null && !categoryIdField.getText().isEmpty()) {
                        item.setCategoryId(Integer.parseInt(categoryIdField.getText()));
                    }
                    item.setImagePath(imagePathField.getText());
                    return item;
                } catch (NumberFormatException e) {
                    showError("Invalid Input", "Price and Category ID must be valid numbers.", "");
                    return null;
                }
            }
            return null;
        });

        return dialog.showAndWait();
    }

    private void showError(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
