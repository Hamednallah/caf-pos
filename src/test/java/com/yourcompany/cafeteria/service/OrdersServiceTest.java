package com.yourcompany.cafeteria.service;

import com.yourcompany.cafeteria.model.Item;
import com.yourcompany.cafeteria.model.Order;
import com.yourcompany.cafeteria.model.OrderItem;
import com.yourcompany.cafeteria.util.DataSourceProvider;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class OrdersServiceTest {

    private Connection connection;
    private OrdersService ordersService;
    private ItemsService itemsService;

    @BeforeEach
    void setUp() throws SQLException {
        DataSourceProvider.setURL("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
        connection = DataSourceProvider.getConnection();

        // Clean the database before each test
        try (java.sql.Statement s = connection.createStatement()) {
            s.execute("DROP ALL OBJECTS");
        }

        Flyway flyway = Flyway.configure().dataSource(DataSourceProvider.getDataSource()).load();
        flyway.migrate();
        ordersService = new OrdersService(connection);
        itemsService = new ItemsService(connection);
    }

    @AfterEach
    void tearDown() throws SQLException {
        if (connection != null) {
            connection.close();
        }
    }

    @Test
    void testCreateValidOrder() throws Exception {
        Item item = new Item();
        item.setName("Valid Order Item");
        item.setDescription("Desc");
        item.setPrice(new BigDecimal("10.00"));
        int itemId = itemsService.add(item);

        Order order = new Order();
        order.setUserId(1);
        order.setTotalAmount(new BigDecimal("10.00"));
        OrderItem orderItem = new OrderItem();
        orderItem.setItemId(itemId);
        orderItem.setQuantity(1);
        orderItem.setLineTotal(new BigDecimal("10.00"));
        order.setItems(Collections.singletonList(orderItem));

        int orderId = ordersService.create(order);
        assertTrue(orderId > 0);
    }

    @Test
    void testCreateInvalidOrder() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> ordersService.create(null));

        Order order1 = new Order();
        order1.setItems(new ArrayList<>());
        order1.setTotalAmount(new BigDecimal("10.00"));
        assertThrows(IllegalArgumentException.class, () -> ordersService.create(order1));

        Item item = new Item();
        item.setName("Invalid Order Item");
        item.setDescription("Desc");
        item.setPrice(new BigDecimal("10.00"));
        int itemId = itemsService.add(item);

        Order order2 = new Order();
        OrderItem orderItem = new OrderItem();
        orderItem.setItemId(itemId);
        orderItem.setQuantity(1);
        orderItem.setLineTotal(new BigDecimal("10.00"));
        order2.setItems(Collections.singletonList(orderItem));
        order2.setTotalAmount(BigDecimal.ZERO);
        assertThrows(IllegalArgumentException.class, () -> ordersService.create(order2));
    }
}
