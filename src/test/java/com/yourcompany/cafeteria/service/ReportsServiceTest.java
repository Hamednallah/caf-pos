package com.yourcompany.cafeteria.service;

import com.yourcompany.cafeteria.model.*;
import com.yourcompany.cafeteria.util.DataSourceProvider;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class ReportsServiceTest {

    private Connection connection;
    private ReportsService reportsService;
    private OrdersService ordersService;
    private ItemsService itemsService;
    private UsersService usersService;
    private ShiftService shiftService;
    private ExpenseService expenseService;

    @BeforeEach
    void setUp() throws SQLException {
        DataSourceProvider.setURL("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
        connection = DataSourceProvider.getConnection();

        try (java.sql.Statement s = connection.createStatement()) {
            s.execute("DROP ALL OBJECTS");
        }

        Flyway flyway = Flyway.configure().dataSource(DataSourceProvider.getDataSource()).load();
        flyway.migrate();
        reportsService = new ReportsService(connection);
        ordersService = new OrdersService(connection);
        itemsService = new ItemsService(connection);
        usersService = new UsersService(connection);
        shiftService = new ShiftService(connection);
        expenseService = new ExpenseService(connection);
    }

    @AfterEach
    void tearDown() throws SQLException {
        if (connection != null) {
            connection.close();
        }
    }

    @Test
    void testGetDailySales() throws Exception {
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

        try (ResultSet rs = reportsService.getDailySales(LocalDate.now())) {
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

    @Test
    void testGetShiftReport_calculatesTotalsCorrectly() throws Exception {
        // 1. Arrange
        User cashier = new User();
        cashier.setUsername("reporter");
        cashier.setPasswordHash(BCrypt.hashpw("password", BCrypt.gensalt()));
        cashier.setRoleId(2); // CASHIER
        int cashierId = usersService.createUser(cashier);

        Item coffee = new Item();
        coffee.setName("Report Coffee");
        coffee.setPrice(new BigDecimal("3.50"));
        int coffeeId = itemsService.add(coffee);

        Item sandwich = new Item();
        sandwich.setName("Report Sandwich");
        sandwich.setPrice(new BigDecimal("8.00"));
        int sandwichId = itemsService.add(sandwich);

        int shiftId = shiftService.startShift(cashierId, new BigDecimal("100.00"));

        Order order1 = new Order();
        order1.cashierId = cashierId;
        order1.shiftId = shiftId;
        order1.paymentMethod = "CASH";
        order1.paymentConfirmed = true;
        order1.status = "FINALIZED";
        OrderItem order1Item = new OrderItem();
        order1Item.setItemId(coffeeId);
        order1Item.setQuantity(2); // 2 * 3.50 = 7.00
        order1Item.setPriceAtPurchase(coffee.getPrice());
        order1.items = Collections.singletonList(order1Item);
        order1.totalAmount = new BigDecimal("7.00");
        order1.discountAmount = BigDecimal.ZERO;
        ordersService.create(order1);

        Order order2 = new Order();
        order2.cashierId = cashierId;
        order2.shiftId = shiftId;
        order2.paymentMethod = "BANK";
        order2.paymentConfirmed = true;
        order2.status = "FINALIZED";
        OrderItem order2Item = new OrderItem();
        order2Item.setItemId(sandwichId);
        order2Item.setQuantity(1); // 1 * 8.00 = 8.00
        order2Item.setPriceAtPurchase(sandwich.getPrice());
        order2.items = Collections.singletonList(order2Item);
        order2.totalAmount = new BigDecimal("8.00");
        order2.discountAmount = new BigDecimal("1.00"); // 8.00 - 1.00 = 7.00 net
        ordersService.create(order2);

        Expense expense = new Expense();
        expense.setShiftId(shiftId);
        expense.setAmount(new BigDecimal("10.50"));
        expense.setDescription("Cleaning supplies");
        expense.setRecordedBy(cashierId);
        expenseService.recordExpense(expense);

        // 2. Act
        ShiftReport report = reportsService.getShiftReport(shiftId);

        // 3. Assert
        assertNotNull(report);
        assertEquals(0, new BigDecimal("15.00").compareTo(report.getTotalSales()));
        assertEquals(0, new BigDecimal("1.00").compareTo(report.getTotalDiscounts()));
        assertEquals(0, new BigDecimal("7.00").compareTo(report.getCashTotal()));
        assertEquals(0, new BigDecimal("8.00").compareTo(report.getBankTotal()));
        assertEquals(0, new BigDecimal("10.50").compareTo(report.getTotalExpenses()));
        assertEquals(2, report.getOrdersCount());
    }

    @Test
    void testGetDateRangeReport_calculatesTotalsCorrectly() throws Exception {
        // 1. Arrange
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        LocalDate tomorrow = today.plusDays(1);

        Item item = new Item();
        item.setName("Date Range Item");
        item.setPrice(new BigDecimal("10.00"));
        int itemId = itemsService.add(item);

        // Order from yesterday - should be included
        Order orderYesterday = new Order();
        orderYesterday.createdAt = yesterday.atTime(10, 0);
        orderYesterday.totalAmount = new BigDecimal("10.00");
        orderYesterday.status = "FINALIZED";
        orderYesterday.paymentMethod = "CASH";
        orderYesterday.paymentConfirmed = true;
        OrderItem oi1 = new OrderItem();
        oi1.setItemId(itemId);
        oi1.setQuantity(1);
        oi1.setLineTotal(orderYesterday.totalAmount);
        orderYesterday.items = Collections.singletonList(oi1);
        ordersService.create(orderYesterday);

        // Order from today - should be included
        Order orderToday = new Order();
        orderToday.createdAt = today.atTime(11, 0);
        orderToday.totalAmount = new BigDecimal("20.00");
        orderToday.status = "FINALIZED";
        orderToday.paymentMethod = "BANK";
        orderToday.paymentConfirmed = true;
        OrderItem oi2 = new OrderItem();
        oi2.setItemId(itemId);
        oi2.setQuantity(2);
        oi2.setLineTotal(orderToday.totalAmount);
        orderToday.items = Collections.singletonList(oi2);
        ordersService.create(orderToday);

        // Order from tomorrow - should NOT be included
        Order orderTomorrow = new Order();
        orderTomorrow.createdAt = tomorrow.atTime(12, 0);
        orderTomorrow.totalAmount = new BigDecimal("30.00");
        orderTomorrow.status = "FINALIZED";
        orderTomorrow.paymentMethod = "CASH";
        orderTomorrow.paymentConfirmed = true;
        OrderItem oi3 = new OrderItem();
        oi3.setItemId(itemId);
        oi3.setQuantity(3);
        oi3.setLineTotal(orderTomorrow.totalAmount);
        orderTomorrow.items = Collections.singletonList(oi3);
        ordersService.create(orderTomorrow);

        // 2. Act
        DateRangeReport report = reportsService.getDateRangeReport(yesterday, today);

        // 3. Assert
        assertNotNull(report);
        // Total Sales = 10.00 (yesterday) + 20.00 (today) = 30.00
        assertEquals(0, new BigDecimal("30.00").compareTo(report.getTotalSales()));
        assertEquals(2, report.getOrdersCount());
    }
}
