# Items Screen Mockup

## 1. Main View (ItemsView.fxml)

The main view for managing items will be similar to the existing `ItemsView.fxml`, but with a more refined UI based on the style guide.

- **Layout**: A `BorderPane` with a `TilePane` in the center to display item cards.
- **Header**: An `HBox` at the top containing a title label "Manage Items" and an "Add New Item" button.
- **Item Cards**: Each item will be displayed as a card in the `TilePane`. The card will contain:
    - Item Name (Label)
    - Price (Label)
    - "Edit" button
    - "Delete" button

## 2. Add/Edit Item Dialog (ItemDialog.fxml)

A dialog will be used for both adding a new item and editing an existing one. This will be a new FXML file, `ItemDialog.fxml`.

- **Layout**: A `DialogPane` with a `GridPane` for the form fields.
- **Fields**:
    - **Name**: `TextField` for the item's name.
    - **Description**: `TextArea` for a longer description of the item.
    - **Price**: `TextField` for the item's price. Should accept only numeric input.
    - **Category**: `ComboBox` to select the item's category. The list of categories will be populated from the database.
    - **Image Path**: `TextField` for the path to the item's image, with a "Browse" button to open a file chooser.
- **Buttons**:
    - **Save**: A `Button` to save the new or edited item.
    - **Cancel**: A `Button` to close the dialog without saving.

## 3. Workflow

1.  **Adding an Item**:
    - Clicking the "Add New Item" button on the main view opens the `ItemDialog`.
    - The user fills in the item details and clicks "Save".
    - The new item is saved to the database, and the `TilePane` in the main view is refreshed to show the new item.

2.  **Editing an Item**:
    - Clicking the "Edit" button on an item card opens the `ItemDialog`, pre-filled with the item's current details.
    - The user modifies the details and clicks "Save".
    - The item's information is updated in the database, and the `TilePane` is refreshed.

3.  **Deleting an Item**:
    - Clicking the "Delete" button on an item card will show a confirmation `Alert`.
    - If the user confirms, the item is deleted from the database, and the `TilePane` is refreshed.
