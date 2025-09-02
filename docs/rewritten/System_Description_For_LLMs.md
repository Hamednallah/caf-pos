# System Description for LLM Agents (Knowledge Base)
Generated: 2025-09-02

Rules (must follow):
1. DO NOT recreate DAO classes in `src/main/java/com/yourcompany/cafeteria/dao`. These are canonical.
2. Controllers may only call Services.
3. If a Service is missing behavior, implement it in the Service layer (and add tests).

Canonical DAO method signatures (summary):
- UsersDAO.findByUsername(String): User
- UsersDAO.createUser(String, String, String, int): int
- ItemsDAO.insert(Item): int
- ItemsDAO.listAll(): List<Item>
- ItemsDAO.findById(int): Item
- ItemsDAO.update(Item): void
- ItemsDAO.delete(int): void
- OrdersDAO.createOrderTransactional(Order): int
- OrderItemsDAO.listByOrderId(int): List<OrderItem>
- ShiftsDAO.startShift(int, BigDecimal): int
- ShiftsDAO.endShift(int): void
- ShiftsDAO.getActiveShiftForCashier(int): ResultSet
- ExpensesDAO.insert(Expense): int
- ExpensesDAO.listByShift(int): ResultSet
- SettingsDAO.get(String): String
- SettingsDAO.set(String, String): void
- ReportsDAO.dailySales(LocalDate): ResultSet

Testing & DB:
- Use H2 embedded for dev/test.
- Flyway migrations are in `src/main/resources/db/migration` and are applied on startup.

Printing:
- ReceiptPrinter uses SettingsService to read 'printer.default' and Java PrintService to locate and print to the selected printer.

