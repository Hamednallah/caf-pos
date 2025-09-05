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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class ReportsServiceTest {

    private Connection connection;
    private ReportsService reportsService;
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
        reportsService = new ReportsService(connection);
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
    void testGetDailySales() throws Exception {
        // Given: An order created today
        Item item = new Item();
        item.setName("Test Item");
        item.setDescription("Desc");
        item.setPrice(new BigDecimal("12.00"));
        int itemId = itemsService.add(item);
        Order order = new Order();
        order.cashierId = 1;
        order.totalAmount = new BigDecimal("12.00");
        OrderItem orderItem = new OrderItem();
        orderItem.setItemId(itemId);
        orderItem.setQuantity(1);
        orderItem.setLineTotal(new BigDecimal("12.00"));
        order.items = Collections.singletonList(orderItem);
        ordersService.create(order);

        // When
        try (ResultSet rs = reportsService.getDailySales(LocalDate.now())) {
            // Then
            assertTrue(rs.next(), "Should have at least one result for today's sales");
            assertEquals("Test Item", rs.getString("name"));
            assertEquals(1, rs.getInt("qty"));
            assertEquals(0, new BigDecimal("12.00").compareTo(rs.getBigDecimal("sales")));
            assertFalse(rs.next(), "Should only have one item in the report");
        }
    }

    @Test
    void testGetDailySalesForNullDate() {
        assertThrows(IllegalArgumentException.class, () -> {
            reportsService.getDailySales(null);
        });
    }
}
