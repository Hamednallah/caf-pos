package com.yourcompany.cafeteria.ui;

import com.yourcompany.cafeteria.dao.CategoryDAO;
import com.yourcompany.cafeteria.model.Category;
import com.yourcompany.cafeteria.model.Item;
import com.yourcompany.cafeteria.service.ItemsService;
import com.yourcompany.cafeteria.util.DataSourceProvider;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class ItemsController implements ResourceAwareController {

    @FXML private TableView<Item> itemsTable;
    @FXML private TableColumn<Item, Integer> idColumn;
    @FXML private TableColumn<Item, String> nameColumn;
    @FXML private TableColumn<Item, BigDecimal> priceColumn;
    @FXML private TableColumn<Item, Category> categoryColumn;

    private ItemsService itemsService;
    private CategoryDAO categoryDAO;
    private ResourceBundle resources;

    @Override
    public void setResources(ResourceBundle resources) {
        this.resources = resources;
    }

    @FXML
    public void initialize() {
        try {
            itemsService = new ItemsService(DataSourceProvider.getConnection());
            categoryDAO = new CategoryDAO(DataSourceProvider.getConnection());
            setupTableColumns();
            loadItems();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Initialization Error", "Could not initialize the Items view.");
        }
    }

    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
    }

    private void loadItems() {
        try {
            List<Item> allItems = itemsService.listAll();
            itemsTable.setItems(FXCollections.observableArrayList(allItems));
        } catch (Exception e) {
            e.printStackTrace();
            showError("Load Error", resources.getString("error.load.items"));
        }
    }

    @FXML
    private void handleAddItem() {
        showItemDialog(new Item());
    }

    @FXML
    private void handleEditItem() {
        Item selectedItem = itemsTable.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            showItemDialog(selectedItem);
        } else {
            showAlert(Alert.AlertType.WARNING, resources.getString("dialog.warning"), resources.getString("prompt.select"));
        }
    }

    @FXML
    private void handleDeleteItem() {
        Item selectedItem = itemsTable.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle(resources.getString("dialog.confirmation"));
            confirmation.setHeaderText(String.format(resources.getString("prompt.delete.confirm"), selectedItem.getName()));
            confirmation.showAndWait().filter(r -> r == ButtonType.OK).ifPresent(r -> {
                try {
                    itemsService.delete(selectedItem.getId());
                    loadItems();
                } catch (Exception e) {
                    e.printStackTrace();
                    showError("Delete Error", resources.getString("error.delete.item"));
                }
            });
        } else {
            showAlert(Alert.AlertType.WARNING, resources.getString("dialog.warning"), resources.getString("prompt.select"));
        }
    }

    private void showItemDialog(Item item) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ItemDialog.fxml"), resources);
            Stage dialogStage = new Stage();
            dialogStage.setTitle(item.getId() == 0 ? "Add Item" : "Edit Item");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(itemsTable.getScene().getWindow());
            Scene scene = new Scene(loader.load());
            dialogStage.setScene(scene);

            // Setup Dialog Fields
            TextField nameField = (TextField) scene.lookup("#nameField");
            TextField priceField = (TextField) scene.lookup("#priceField");
            ComboBox<Category> categoryComboBox = (ComboBox<Category>) scene.lookup("#categoryComboBox");

            // Load categories
            categoryComboBox.setItems(FXCollections.observableArrayList(categoryDAO.listAll()));

            if (item.getId() != 0) {
                nameField.setText(item.getName());
                priceField.setText(item.getPrice().toPlainString());
                categoryComboBox.setValue(item.getCategory());
            }

            // Setup Buttons
            Button saveButton = (Button) scene.lookup("#saveButton");
            saveButton.setOnAction(e -> {
                try {
                    item.setName(nameField.getText());
                    item.setPrice(new BigDecimal(priceField.getText()));
                    item.setCategory(categoryComboBox.getValue());
                    item.setCategoryId(categoryComboBox.getValue().getId());
                    if (item.getId() == 0) {
                        itemsService.add(item);
                    } else {
                        itemsService.update(item);
                    }
                    loadItems();
                    dialogStage.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    showError("Save Error", resources.getString("error.save.item"));
                }
            });

            Button cancelButton = (Button) scene.lookup("#cancelButton");
            cancelButton.setOnAction(e -> dialogStage.close());

            Button addCategoryButton = (Button) scene.lookup("#addCategoryButton");
            addCategoryButton.setOnAction(e -> {
                showAddCategoryDialog().ifPresent(newCategory -> {
                    try {
                        categoryComboBox.setItems(FXCollections.observableArrayList(categoryDAO.listAll()));
                        categoryComboBox.setValue(newCategory);
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                });
            });


            dialogStage.showAndWait();

        } catch (IOException | SQLException e) {
            e.printStackTrace();
            showError("Dialog Error", resources.getString("error.dialog.item"));
        }
    }

    private Optional<Category> showAddCategoryDialog() {
        Dialog<Category> dialog = new Dialog<>();
        dialog.setTitle("Add New Category");
        dialog.setHeaderText("Enter details for the new category.");

        ButtonType saveButtonType = new ButtonType(resources.getString("button.save"), ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField nameField = new TextField();
        nameField.setPromptText("Name");
        TextField descriptionField = new TextField();
        descriptionField.setPromptText("Description");

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(descriptionField, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                Category newCategory = new Category();
                newCategory.setName(nameField.getText());
                newCategory.setDescription(descriptionField.getText());
                try {
                    return categoryDAO.addCategory(newCategory);
                } catch (SQLException e) {
                    e.printStackTrace();
                    showError("Database Error", resources.getString("error.save.category"));
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

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
