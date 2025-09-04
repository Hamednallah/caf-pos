package com.yourcompany.cafeteria.ui;

import com.yourcompany.cafeteria.model.Order;
import com.yourcompany.cafeteria.model.OrderItem;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

public class ReceiptController {

    @FXML private Label headerLabel;
    @FXML private Label dateTimeLabel;
    @FXML private VBox itemsVBox;
    @FXML private Label totalLabel;
    @FXML private Label footerLabel;

    public void populateReceipt(Order order) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        dateTimeLabel.setText(order.getCreatedAt().format(formatter));

        itemsVBox.getChildren().clear();
        for (OrderItem item : order.getItems()) {
            BigDecimal pricePerItem = item.getLineTotal().divide(new java.math.BigDecimal(item.getQuantity()));
            Label itemLabel = new Label(
                String.format("%d x %s @ %.2f = %.2f",
                    item.getQuantity(),
                    item.getItemName(),
                    pricePerItem,
                    item.getLineTotal()
                )
            );
            itemsVBox.getChildren().add(itemLabel);
        }

        totalLabel.setText(String.format("Total: %.2f", order.getTotalAmount().subtract(order.getDiscountAmount())));
    }
}
