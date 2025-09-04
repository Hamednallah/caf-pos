package com.yourcompany.cafeteria.service;

import com.yourcompany.cafeteria.model.Order;
import com.yourcompany.cafeteria.model.OrderItem;
import com.yourcompany.cafeteria.model.Return;
import com.yourcompany.cafeteria.model.ReturnedItem;
import com.yourcompany.cafeteria.util.TestDatabase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ReturnsServiceTest {

    private Connection conn;
    private ReturnsService returnsService;
    private OrdersService ordersService;

    @BeforeEach
    public void setup() throws Exception {
        conn = TestDatabase.open();
        returnsService = new ReturnsService();
        ordersService = new OrdersService(conn);
    }

    @AfterEach
    public void teardown() throws Exception {
        conn.close();
    }

    @Test
    public void testProcessReturn() throws Exception {
        // 1. Create a dummy order to return against
        Order order = new Order();
        order.setCashierId(1);
        order.setShiftId(1);
        order.setCreatedAt(LocalDateTime.now());
        order.setTotalAmount(new BigDecimal("10.00"));
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setStatus("FINALIZED");
        order.setPaymentMethod("CASH");
        order.setPaymentConfirmed(true);

        OrderItem item1 = new OrderItem();
        item1.setItemId(1);
        item1.setQuantity(2);
        item1.setLineTotal(new BigDecimal("10.00"));

        List<OrderItem> items = new ArrayList<>();
        items.add(item1);
        order.setItems(items);

        int orderId = ordersService.create(order);

        // Retrieve the created order to get the order_item ID
        Order createdOrder = ordersService.findById(orderId);
        int orderItemId = createdOrder.getItems().get(0).getId();

        // 2. Prepare the return data
        List<ReturnedItem> itemsToReturn = new ArrayList<>();
        ReturnedItem returnedItem = new ReturnedItem();
        returnedItem.setOrderItemId(orderItemId);
        returnedItem.setQuantity(1);
        itemsToReturn.add(returnedItem);

        String reason = "Customer changed their mind";

        // 3. Process the return
        Return processedReturn = returnsService.processReturn(orderId, reason, itemsToReturn);

        // 4. Verify the return was created correctly
        assertNotNull(processedReturn.getId());
        assertEquals(orderId, processedReturn.getOrderId());
        assertEquals(reason, processedReturn.getReason());

        // 5. Verify the data in the database
        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT * FROM returns WHERE id = " + processedReturn.getId());
            if (rs.next()) {
                assertEquals(orderId, rs.getInt("order_id"));
                assertEquals(reason, rs.getString("reason"));
            }

            rs = stmt.executeQuery("SELECT * FROM returned_items WHERE return_id = " + processedReturn.getId());
            if (rs.next()) {
                assertEquals(orderItemId, rs.getInt("order_item_id"));
                assertEquals(1, rs.getInt("quantity"));
            }
        }
    }
}
