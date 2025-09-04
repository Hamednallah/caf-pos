# Sales Screen Mockup

**Objective:** A modern, touch-first, and efficient interface for cashiers.

---

## Layout

The screen is a `BorderPane` divided into three main sections: Left, Center, and Right.

### 1. Left Panel (Category & Search)

*   **Component:** `VBox`
*   **Width:** 200px
*   **Contents:**
    *   A "Categories" `Label` with `h2` styling.
    *   A `ListView` (`categoryListView`) displaying all available item categories. Selecting a category filters the center grid.
    *   A `TextField` (`searchField`) for real-time searching of items by name.

### 2. Center Panel (Item Grid)

*   **Component:** `ScrollPane` containing a `TilePane` (`itemGridPane`).
*   **Functionality:** Displays all items matching the current filter (category + search).
*   **Item Card:** Each item is represented by a "card" (`VBox`) with the following properties:
    *   **Size:** 120x100px (large enough for easy touch interaction).
    *   **Style:** White background, subtle border, rounded corners.
    *   **Content:**
        *   Item Name (`Label`, bold).
        *   Item Price (`Label`).
    *   **Interaction:** A single click on the card adds 1 quantity of the item to the cart.

### 3. Right Panel (Cart & Payment)

*   **Component:** `VBox`
*   **Width:** 300px
*   **Style:** White background, acts as a distinct surface.
*   **Contents (from top to bottom):**
    *   A "Cart" `Label` with `h1` styling.
    *   A `TableView` (`cartTable`) showing the current order items.
        *   Columns: Item Name, Quantity (editable), Line Total.
    *   A `HBox` containing:
        *   A "Discount:" `Label`.
        *   A `TextField` (`discountField`) for entering a fixed discount amount.
    *   A `Label` (`totalLabel`) displaying the final calculated total in a large, bold font.
    *   A `VBox` for payment method selection:
        *   A "Payment Method:" `Label`.
        *   A `ToggleGroup` containing two `RadioButton`s: "Cash" (default) and "Bank".
    *   A large, prominent "Confirm Payment" `Button` (`finalizeButton`) that spans the full width of the panel. It uses the primary accent color (`-fx-primary`).

---

## High-Level Flow (Cashier)

1.  Select a category from the left panel (e.g., "Drinks").
2.  The center grid updates to show only drinks.
3.  Tap on the "Coffee" card twice.
4.  Tap on the "Tea" card once.
5.  The cart on the right updates to show:
    *   Coffee | 2 | 7.00
    *   Tea    | 1 | 3.50
6.  The total label updates to "Total: 10.50".
7.  Enter "0.50" in the discount field.
8.  The total label updates to "Total: 10.00".
9.  Select the "Cash" radio button.
10. Click the "Confirm Payment" button.
11. An information alert appears confirming the sale, and the cart is cleared for the next customer.
