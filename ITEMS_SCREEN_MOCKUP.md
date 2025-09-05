# Items Screen Redesign Mockup

## 1. Overview

The `Items` screen will be redesigned from a `TilePane` layout to a more structured and professional `TableView` layout. This allows for better organization, sorting, and management of a large number of items.

## 2. Layout (`ItemsView.fxml`)

The main view will consist of:
- A `VBox` as the root container.
- A `Label` with the text "Manage Items" and the `header-label` style class.
- A `TableView` to list all the items.
  - `fx:id="itemsTable"`
  - Columns:
    1.  **Name:** `fx:id="nameColumn"` - Displays the item's name.
    2.  **Category:** `fx:id="categoryColumn"` - Displays the item's category.
    3.  **Price:** `fx:id="priceColumn"` - Displays the item's price, formatted as currency.
    4.  **Stock Level:** `fx:id="stockColumn"` - Displays the current stock quantity. (Future enhancement, but good to have the column).
- An `HBox` at the bottom for action buttons.
  - `spacing="10"`
  - `alignment="CENTER_RIGHT"`
  - Buttons:
    1.  **Add Item:** `fx:id="addButton"` - Opens the item dialog for creating a new item.
    2.  **Edit Item:** `fx:id="editButton"` - Opens the item dialog for editing the selected item. Should be disabled if no item is selected.
    3.  **Delete Item:** `fx:id="deleteButton"` - Deletes the selected item after a confirmation dialog. Should be disabled if no item is selected.

## 3. Add/Edit Dialog (`ItemDialog.fxml`)

A separate FXML file will define the dialog for creating and editing items.

- Layout: `GridPane` for clean alignment of labels and fields.
- Fields:
  - **Name:** `TextField` (`fx:id="nameField"`)
  - **Category:** `ComboBox` (`fx:id="categoryComboBox"`) - Allows selecting an existing category or typing a new one.
  - **Price:** `TextField` (`fx:id="priceField"`)
  - **Stock:** `TextField` (`fx:id="stockField"`)
- Buttons:
  - **Save:** `Button` (`fx:id="saveButton"`)
  - **Cancel:** `Button` (`fx:id="cancelButton"`)

## 4. Controller Logic (`ItemsController.java`)

- `initialize()`:
  - Set up the `TableView` columns and cell value factories.
  - Load all items from the `ItemsService` and populate the table.
  - Add a listener to the table's selection model to enable/disable the "Edit" and "Delete" buttons.
- `handleAddButton()`:
  - Opens the `ItemDialog.fxml`.
  - If an item is created, refresh the table view.
- `handleEditButton()`:
  - Gets the selected item from the table.
  - Opens the `ItemDialog.fxml`, pre-filled with the selected item's data.
  - If the item is updated, refresh the table view.
- `handleDeleteButton()`:
  - Shows a confirmation alert (`Alert.AlertType.CONFIRMATION`).
  - If confirmed, calls the `ItemsService` to delete the item and removes it from the table.
- `refreshTable()`:
  - A helper method to clear and reload all items into the table.

## 5. Inline Category Creation

- In the `ItemDialog`, when the user types a new value into the `categoryComboBox` and saves, the controller logic should first create the new `Category` via the `ItemsService`, get its ID, and then associate it with the new `Item`.
