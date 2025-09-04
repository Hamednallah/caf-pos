# Items Screen Mockup

## 1. Overview

The Items screen allows administrators and managers to manage the products sold in the cafeteria. The redesigned screen will follow the new UI style guide, providing a clean, intuitive interface for adding, editing, and deleting items. A key new feature is the ability to assign items to categories and to create new categories directly from the items management view.

## 2. Layout

The layout will be consistent with the other redesigned screens, using a `BorderPane` as the root container.

-   **Top:** A title label "Items Management".
-   **Center:** A `TableView` displaying the list of items.
-   **Bottom:** An `HBox` containing buttons for "Add Item", "Edit Item", and "Delete Item".

## 3. `TableView` Columns

The `TableView` will display the following columns:

-   **ID:** The item's unique identifier.
-   **Name:** The name of the item (e.g., "Espresso", "Croissant").
-   **Price:** The price of the item, formatted as a currency.
-   **Category:** The category the item belongs to (e.g., "Hot Drinks", "Pastries").
-   **Stock:** (Future feature) The current stock level.

## 4. Item Dialog (for Add/Edit)

A dialog will be used for both adding and editing items. It will be a `GridPane` containing the following fields:

-   **Name:** `TextField` for the item's name.
-   **Price:** `TextField` for the item's price (with validation for numeric input).
-   **Category:** `ComboBox` to select an existing category.
    -   Next to the `ComboBox`, there will be a small "Add" (`+`) button. Clicking this button will open a *secondary dialog* to create a new category.

## 5. New Category Dialog

This simple dialog will contain:

-   **Category Name:** `TextField` for the new category's name.
-   **Description:** `TextField` for an optional description.
-   **Save/Cancel Buttons.**

Upon saving, the new category will be added to the database and will be automatically selected in the Item Dialog's category `ComboBox`.

## 6. Workflow

**Editing an Item:**
1.  User selects an item in the `TableView`.
2.  User clicks the "Edit Item" button.
3.  The Item Dialog opens, populated with the selected item's data.
4.  User modifies the data and clicks "Save".
5.  The `TableView` is refreshed to show the updated information.

**Adding a New Item with a New Category:**
1.  User clicks the "Add Item" button.
2.  The Item Dialog opens with empty fields.
3.  User clicks the `+` button next to the category `ComboBox`.
4.  The New Category Dialog opens.
5.  User enters the name for the new category (e.g., "Snacks") and clicks "Save".
6.  The New Category Dialog closes. The "Snacks" category is now available and selected in the Item Dialog's `ComboBox`.
7.  User fills in the rest of the item details (Name, Price) and clicks "Save".
8.  The new item is added to the database and appears in the `TableView`.
