package com.yourcompany.cafeteria.ui;

import com.yourcompany.cafeteria.model.Item;
import com.yourcompany.cafeteria.service.ItemsService;
import com.yourcompany.cafeteria.util.DataSourceProvider;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;

import java.util.List;

public class ItemsController {

    @FXML
    private TilePane itemGridPane;

    private ItemsService itemsService;

    @FXML
    public void initialize() {
        try {
            itemsService = new ItemsService(DataSourceProvider.getConnection());
            loadItems();
        } catch (Exception e) {
            e.printStackTrace(); // Show error
        }
    }

    private void loadItems() {
        itemGridPane.getChildren().clear();
        try {
            List<Item> allItems = itemsService.listAll();
            for (Item item : allItems) {
                itemGridPane.getChildren().add(createItemCard(item));
            }
        } catch (Exception e) {
            e.printStackTrace(); // Show error
        }
    }

    private Node createItemCard(Item item) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setStyle("-fx-background-color: -fx-surface; -fx-padding: 10; -fx-border-color: -fx-subtle-border; -fx-border-width: 1; -fx-background-radius: 8; -fx-border-radius: 8;");
        card.setPrefSize(150, 120);

        Label nameLabel = new Label(item.getName());
        nameLabel.setStyle("-fx-font-weight: bold;");
        Label priceLabel = new Label(String.format("Price: %.2f", item.getPrice()));

        Button editButton = new Button("Edit");
        editButton.setOnAction(event -> handleEditItem(item));

        Button deleteButton = new Button("Delete");
        deleteButton.setStyle("-fx-background-color: #EF4444;"); // Red color for delete
        deleteButton.setOnAction(event -> handleDeleteItem(item));

        HBox buttonBox = new HBox(5, editButton, deleteButton);
        buttonBox.setAlignment(Pos.CENTER);

        card.getChildren().addAll(nameLabel, priceLabel, buttonBox);
        return card;
    }

    @FXML
    private void handleAddItem() {
        System.out.println("Add item clicked");
        // Logic to show an add item dialog will go here
    }

    private void handleEditItem(Item item) {
        System.out.println("Edit item clicked: " + item.getName());
        // Logic to show an edit item dialog will go here
    }

    private void handleDeleteItem(Item item) {
        System.out.println("Delete item clicked: " + item.getName());
        // Logic to show a confirmation and delete the item
        try {
            // Show confirmation dialog first
            itemsService.delete(item.getId());
            loadItems(); // Refresh grid
        } catch (Exception e) {
            e.printStackTrace(); // Show error
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
