package com.yourcompany.cafeteria.ui;

import com.yourcompany.cafeteria.model.Category;
import com.yourcompany.cafeteria.model.Item;
import com.yourcompany.cafeteria.service.ItemsService;
import com.yourcompany.cafeteria.util.DataSourceProvider;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class ItemsController {

    @FXML
    private TilePane itemGridPane;

    private ItemsService itemsService;
    private List<Category> categories;

    @FXML
    public void initialize() {
        try {
            itemsService = new ItemsService(DataSourceProvider.getConnection());
            categories = itemsService.getAllCategories();
            loadItems();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Initialization Error", "Could not load items or categories.");
            e.printStackTrace();
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
            showAlert(Alert.AlertType.ERROR, "Loading Error", "Could not load items.");
            e.printStackTrace();
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
        showItemDialog(null);
    }

    private void handleEditItem(Item item) {
        showItemDialog(item);
    }

    private void handleDeleteItem(Item item) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Delete Item");
        confirmation.setHeaderText("Are you sure you want to delete this item?");
        confirmation.setContentText(item.getName());

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                itemsService.delete(item.getId());
                loadItems(); // Refresh grid
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Delete Error", "Failed to delete the item.");
                e.printStackTrace();
            }
        }
    }

    private void showItemDialog(Item item) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ItemDialog.fxml"));
            DialogPane dialogPane = loader.load();

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle(item == null ? "Add Item" : "Edit Item");

            TextField nameField = (TextField) dialogPane.lookup("#nameField");
            TextArea descriptionArea = (TextArea) dialogPane.lookup("#descriptionArea");
            TextField priceField = (TextField) dialogPane.lookup("#priceField");
            ComboBox<Category> categoryComboBox = (ComboBox<Category>) dialogPane.lookup("#categoryComboBox");
            TextField imagePathField = (TextField) dialogPane.lookup("#imagePathField");
            Button browseButton = (Button) dialogPane.lookup("#browseButton");

            categoryComboBox.setItems(FXCollections.observableArrayList(categories));

            browseButton.setOnAction(e -> {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Select Image");
                fileChooser.getExtensionFilters().addAll(
                        new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif"));
                File selectedFile = fileChooser.showOpenDialog(dialog.getDialogPane().getScene().getWindow());
                if (selectedFile != null) {
                    imagePathField.setText(selectedFile.getAbsolutePath());
                }
            });

            if (item != null) {
                nameField.setText(item.getName());
                descriptionArea.setText(item.getDescription());
                priceField.setText(item.getPrice().toPlainString());
                if (item.getCategoryId() != null) {
                    categories.stream()
                              .filter(c -> c.getId() == item.getCategoryId())
                              .findFirst()
                              .ifPresent(categoryComboBox::setValue);
                }
                imagePathField.setText(item.getImagePath());
            }

            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent() && result.get().getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                Item updatedItem = (item == null) ? new Item() : item;
                updatedItem.setName(nameField.getText());
                updatedItem.setDescription(descriptionArea.getText());
                updatedItem.setPrice(new BigDecimal(priceField.getText()));
                if (categoryComboBox.getValue() != null) {
                    updatedItem.setCategoryId(categoryComboBox.getValue().getId());
                } else {
                    updatedItem.setCategoryId(null);
                }
                updatedItem.setImagePath(imagePathField.getText());

                if (item == null) {
                    itemsService.add(updatedItem);
                } else {
                    itemsService.update(updatedItem);
                }
                loadItems();
            }
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Dialog Error", "Could not load the item dialog.");
            e.printStackTrace();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Save Error", "Failed to save the item. " + e.getMessage());
            e.printStackTrace();
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
