package com.yourcompany.cafeteria;

import com.yourcompany.cafeteria.model.*;
import com.yourcompany.cafeteria.service.*;
import com.yourcompany.cafeteria.util.TestDatabase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;


import java.math.BigDecimal;
import java.sql.Connection;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class OrderFlowIntegrationTest {

    private Connection conn;
    private UsersService usersService;
    private ItemsService itemsService;
    private ShiftService shiftService;
    private OrdersService ordersService;
    private ExpenseService expenseService;
    private ReportsService reportsService;

    @BeforeEach
    public void setup() throws Exception {
        conn = TestDatabase.open();
        usersService = new UsersService(conn);
        itemsService = new ItemsService(conn);
        shiftService = new ShiftService(conn);
        ordersService = new OrdersService(conn);
        expenseService = new ExpenseService(conn);
        reportsService = new ReportsService(conn);
    }

    @AfterEach
    public void teardown() throws Exception {
        conn.close();
    }

    @Test
    public void testFullShiftAndOrderWorkflow() throws Exception {
        // 1. Setup initial data
        User cashier = new User();
        cashier.setUsername("testcashier_integration");
        cashier.setFullName("Test Cashier");
        cashier.setRoleId(2); // Assuming 2 is the roleId for CASHIER
        int cashierId = usersService.createUser(cashier, "password");
        cashier.setId(cashierId);

        Item testCoffee = new Item();
        testCoffee.setName("Integration Test Coffee");
        testCoffee.setPrice(new BigDecimal("2.50"));
        int coffeeId = itemsService.add(testCoffee);

        Item testSandwich = new Item();
        testSandwich.setName("Integration Test Sandwich");
        testSandwich.setPrice(new BigDecimal("5.00"));
        itemsService.add(testSandwich);

        // 2. Start a shift
        BigDecimal startingFloat = new BigDecimal("100.00");
        int shiftId = shiftService.startShift(cashierId, startingFloat);

        // 3. Create an order
        Order order = new Order();
        order.setUserId(cashierId);
        order.setShiftId(shiftId);
        order.setStatus("FINALIZED");
        order.setPaymentMethod("CASH");
        order.setPaymentConfirmed(true);

        OrderItem coffeeItem = new OrderItem();
        coffeeItem.setItemId(coffeeId);
        coffeeItem.setQuantity(2);
        coffeeItem.setPriceAtPurchase(testCoffee.getPrice()); // Price at time of sale

        order.setItems(Collections.singletonList(coffeeItem));
        order.setTotalAmount(coffeeItem.getLineTotal());
        order.setDiscountAmount(BigDecimal.ZERO);

        ordersService.create(order);

        // 4. Record an expense
        Expense expense = new Expense();
        expense.setShiftId(shiftId);
        expense.setUserId(cashierId);
        expense.setDescription("Cleaning Supplies");
        expense.setAmount(new BigDecimal("15.00"));
        expenseService.recordExpense(expense);

        // 5. End the shift and get the summary
        ShiftSummary summary = reportsService.getShiftSummary(shiftId);
        shiftService.endShift(shiftId, summary.getExpectedCashInDrawer());

        // 6. Assertions
        assertEquals(0, new BigDecimal("100.00").compareTo(summary.getStartingFloat()), "Starting float should be 100.00");
        assertEquals(0, new BigDecimal("5.00").compareTo(summary.getTotalCashSales()), "Total cash sales should be 5.00");
        assertEquals(0, new BigDecimal("15.00").compareTo(summary.getTotalExpenses()), "Total expenses should be 15.00");

        // Expected cash = 100.00 (start) + 5.00 (sales) - 15.00 (expenses) = 90.00
        BigDecimal expectedCash = new BigDecimal("90.00");
        assertEquals(0, expectedCash.compareTo(summary.getExpectedCashInDrawer()), "Expected cash in drawer should be 90.00");
    }
}
