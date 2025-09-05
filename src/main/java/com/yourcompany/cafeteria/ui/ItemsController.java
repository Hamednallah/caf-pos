package com.yourcompany.cafeteria.ui;

import com.yourcompany.cafeteria.model.Category;
import com.yourcompany.cafeteria.model.Item;
import com.yourcompany.cafeteria.service.ItemsService;
import com.yourcompany.cafeteria.util.DataSourceProvider;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;

public class ItemsController implements ResourceAwareController {

    @FXML
    private TableView<Item> itemsTable;
    @FXML
    private TableColumn<Item, String> nameColumn;
    @FXML
    private TableColumn<Item, String> categoryColumn;
    @FXML
    private TableColumn<Item, BigDecimal> priceColumn;
    @FXML
    private Button editButton;
    @FXML
    private Button deleteButton;

    private ItemsService itemsService;
    private ResourceBundle resourceBundle;

    @Override
    public void setResources(ResourceBundle resourceBundle) {
        this.resourceBundle = resourceBundle;
    }

    @FXML
    public void initialize() {
        try {
            itemsService = new ItemsService(DataSourceProvider.getConnection());
            setupTableColumns();
            loadItems();

            // Disable edit/delete buttons when no item is selected
            editButton.disableProperty().bind(itemsTable.getSelectionModel().selectedItemProperty().isNull());
            deleteButton.disableProperty().bind(itemsTable.getSelectionModel().selectedItemProperty().isNull());

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Initialization Failed", "Could not initialize the items view: " + e.getMessage());
        }
    }

    private void setupTableColumns() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));

        // For category, we need to get the category name from the category object
        categoryColumn.setCellValueFactory(cellData -> {
            Item item = cellData.getValue();
            if (item.getCategory() != null) {
                return new SimpleStringProperty(item.getCategory().getName());
            } else {
                return new SimpleStringProperty("");
            }
        });
    }

    private void loadItems() {
        try {
            List<Item> allItems = itemsService.listAll();
            itemsTable.setItems(FXCollections.observableArrayList(allItems));
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Loading Failed", "Could not load items from the database.");
        }
    }

    @FXML
    private void handleAddItem() {
        showItemDialog(null);
    }

    @FXML
    private void handleEditItem() {
        Item selectedItem = itemsTable.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            showItemDialog(selectedItem);
        }
    }

    @FXML
    private void handleDeleteItem() {
        Item selectedItem = itemsTable.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle(resourceBundle.getString("items.dialog.confirm.delete.title"));
            confirmation.setHeaderText(resourceBundle.getString("items.dialog.confirm.delete.header"));
            confirmation.setContentText(resourceBundle.getString("items.dialog.confirm.delete.content"));

            Optional<ButtonType> result = confirmation.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    itemsService.delete(selectedItem.getId());
                    itemsTable.getItems().remove(selectedItem);
                } catch (Exception e) {
                    e.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Delete Failed", "Could not delete the item: " + e.getMessage());
                }
            }
        }
    }

    private void showItemDialog(Item item) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/yourcompany/cafeteria/fxml/ItemDialog.fxml"), resourceBundle);
            DialogPane dialogPane = loader.load();

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle(item == null ? resourceBundle.getString("items.dialog.add.title") : resourceBundle.getString("items.dialog.edit.title"));

            // Get controller and set up dialog
            ItemDialogController controller = loader.getController();
            controller.setDialog(dialog, item, itemsService);


            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent() && result.get().getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                loadItems(); // Reload items from DB to reflect changes
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Dialog Error", "Could not open the item dialog: " + e.getMessage());
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

class ItemDialogController {

    @FXML private TextField nameField;
    @FXML private ComboBox<Category> categoryComboBox;
    @FXML private TextField priceField;
    @FXML private Button saveButton;

    private Dialog<ButtonType> dialog;
    private Item currentItem;
    private ItemsService itemsService;

    @FXML
    public void initialize() {
        // Validation logic can be added here if needed
    }

    public void setDialog(Dialog<ButtonType> dialog, Item item, ItemsService itemsService) {
        this.dialog = dialog;
        this.currentItem = item;
        this.itemsService = itemsService;

        loadCategories();
        setupCategoryComboBox();

        if (item != null) {
            // Editing existing item
            nameField.setText(item.getName());
            priceField.setText(item.getPrice().toString());
            if (item.getCategory() != null) {
                categoryComboBox.setValue(item.getCategory());
            }
        }

        // Find the save button in the dialog pane and add an action event handler
        this.saveButton = (Button) dialog.getDialogPane().lookupButton(dialog.getDialogPane().getButtonTypes().stream().filter(bt -> bt.getButtonData() == ButtonBar.ButtonData.OK_DONE).findFirst().get());
        this.saveButton.setOnAction(event -> {
            if (isInputValid()) {
                handleSave();
            } else {
                // Prevent dialog from closing if validation fails
                event.consume();
            }
        });
    }

    private void loadCategories() {
        try {
            categoryComboBox.setItems(FXCollections.observableArrayList(itemsService.getAllCategories()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupCategoryComboBox() {
        // This allows the ComboBox to display the category name, but the value is the Category object itself.
        categoryComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Category category) {
                return category == null ? "" : category.getName();
            }

            @Override
            public Category fromString(String string) {
                // This part is crucial for inline creation of categories
                Optional<Category> existing = categoryComboBox.getItems().stream()
                        .filter(c -> c.getName().equalsIgnoreCase(string))
                        .findFirst();
                if (existing.isPresent()) {
                    return existing.get();
                } else {
                    // It's a new category
                    Category newCategory = new Category();
                    newCategory.setName(string);
                    return newCategory;
                }
            }
        });
    }

    private boolean isInputValid() {
        // Simple validation
        if (nameField.getText() == null || nameField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Invalid Input", "Item name cannot be empty.");
            return false;
        }
        if (priceField.getText() == null || !priceField.getText().matches("\\d+(\\.\\d{1,2})?")) {
            showAlert(Alert.AlertType.ERROR, "Invalid Input", "Please enter a valid price (e.g., 10.50).");
            return false;
        }
        if (categoryComboBox.getValue() == null && (categoryComboBox.getEditor().getText() == null || categoryComboBox.getEditor().getText().trim().isEmpty())) {
            showAlert(Alert.AlertType.ERROR, "Invalid Input", "Category cannot be empty.");
            return false;
        }
        return true;
    }

    private void handleSave() {
        try {
            Category selectedCategory = categoryComboBox.getValue();

            // Check if the category is new (doesn't have an ID) and needs to be created
            if (selectedCategory != null && selectedCategory.getId() == 0) {
                Category createdCategory = itemsService.createCategory(selectedCategory);
                selectedCategory.setId(createdCategory.getId());
            }


            if (currentItem == null) {
                // Creating a new item
                currentItem = new Item();
            }

            currentItem.setName(nameField.getText());
            currentItem.setPrice(new BigDecimal(priceField.getText()));
            currentItem.setCategory(selectedCategory);

            if (currentItem.getId() == 0) {
                itemsService.add(currentItem);
            } else {
                itemsService.update(currentItem);
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Save Failed", "Could not save the item: " + e.getMessage());
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
